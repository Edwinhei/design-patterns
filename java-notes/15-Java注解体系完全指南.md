# Java 注解体系完全指南

> 📍 来源：一路学来遇到的 `@Override` / `@With` / Spring `@Autowired` 等注解的"黑魔法"疑问
> 🎯 回答的核心问题："**注解到底是什么？它怎么工作？Spring / Lombok 的注解魔法是怎么实现的？我自己能写吗？**"

---

## 🤔 场景问题

从你学习过程中冒出的真实疑问：

1. `@Override` 是"语言层实现"吗？
2. Lombok `@With` / `@Builder` 怎么做到"一行生成一堆代码"？
3. Spring `@Autowired` 怎么知道给哪个字段注入？
4. `@Transactional` 为什么加一行就能自动事务？
5. 我自己能写一个注解吗？怎么让它生效？

一次讲透。

---

## 🧠 核心结论

> **注解本身不执行任何逻辑，它只是代码上的"元数据"标签**。
>
> **真正干活的是"处理注解的代码"**，有三类：
>
> 1. **编译器内置处理**（`@Override` 等 JDK 注解）
> 2. **APT 注解处理器**（编译期生成代码，如 Lombok）
> 3. **运行时反射 + 框架**（运行期扫描 + 处理，如 Spring）

---

## 📋 第一步：什么是注解？

### 语法上：一个特殊的接口

```java
public @interface MyAnnotation {           // 注意是 @interface 不是 interface
    String value();                         // 有"属性"，像方法签名
    int count() default 1;                  // 可以有默认值
}
```

### 使用：带 `@` 前缀挂在代码上

```java
@MyAnnotation(value = "hello", count = 3)
public class Foo {
    @MyAnnotation("hi")
    public void bar() {}
}
```

### 本质：给代码打标签

注解**不会执行任何代码**。它就是**一条附加在元素上的元数据**。

编译后，注解会**存储在 class 文件的特定区域**，等着有人（编译器、APT、运行时反射）来读取并做处理。

---

## 🎯 第二步：元注解（注解的注解）

要自己定义一个注解，你必须指定**两个元注解**：

### `@Target` —— 我能挂在什么上面

```java
@Target(ElementType.METHOD)                 // 只能挂方法
@Target({ElementType.TYPE, ElementType.FIELD})  // 类和字段都能
public @interface MyAnnotation {}
```

`ElementType` 枚举的常用值：

| 值 | 含义 |
|----|------|
| `TYPE` | 类 / 接口 / 枚举 |
| `METHOD` | 方法 |
| `FIELD` | 字段 |
| `PARAMETER` | 方法参数 |
| `CONSTRUCTOR` | 构造器 |
| `LOCAL_VARIABLE` | 局部变量 |
| `ANNOTATION_TYPE` | 注解（元注解用）|
| `PACKAGE` | 包 |
| `TYPE_PARAMETER` | 泛型参数（Java 8+）|
| `TYPE_USE` | 任何类型使用处（Java 8+）|

### `@Retention` —— 我存活多久（**关键**）

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {}
```

`RetentionPolicy` 三个值：

| 值 | 含义 | 例子 |
|----|------|------|
| `SOURCE` | **只在源码里**，编译完就丢 | `@Override` / `@SuppressWarnings` |
| `CLASS` | **保留到 class 文件**，但**运行时读不到**（默认值） | 很少用 |
| `RUNTIME` | **运行时可反射读取** | `@Autowired` / `@Component` / `@Entity` |

**这是注解最重要的元注解**。决定了你的注解**能不能在运行时被反射读到**。

### 其他元注解（了解即可）

```java
@Documented    // 注解会出现在 Javadoc
@Inherited     // 子类自动继承父类上的注解
@Repeatable    // 同一个地方可以多次使用
```

---

## 🧬 第三步：三种处理方式（最核心）

**注解自己不做事**。谁来做事？取决于你怎么"处理"它。

### 方式 1：编译器内置处理（JDK 自带的几个注解）

```java
@Override           // 编译器看到这个 → 检查是否真的重写了父类方法
@Deprecated         // 编译器看到这个 → 调用处给出"已过时"警告
@SuppressWarnings   // 编译器看到这个 → 不报指定的警告
@FunctionalInterface // 编译器看到这个 → 检查接口是否只有一个抽象方法
```

**特点**：
- **Java 编译器（javac）内置理解这些注解**
- 你没法扩展这类（除非修改 JDK）
- 注解保留级别通常是 `SOURCE`（编译完丢）

**这就是 `@Override` 的真相**：
- 它是 JDK 内置的 `java.lang.Override` 注解
- 但"实现"的是 `javac` 编译器 —— 它看到 `@Override` 就多做一次"是否真重写"的检查
- **不是什么"语言层原生关键字"**，本质就是一个特殊注解 + 编译器特殊处理

### 方式 2：APT 注解处理器（编译期生成代码）

**APT = Annotation Processing Tool**。这是 Java 提供的**官方编译期扩展机制**。

**典型代表**：Lombok

```java
@Data                   // Lombok 注解
public class User {
    private Long id;
    private String name;
}
```

编译时，Lombok 的 APT 处理器**读到 `@Data`** → **生成 getter/setter/equals/hashCode/toString** → **插入到编译结果**。

编译后的 class 文件就像你手写了那一堆方法。

**特点**：
- 在**编译期**工作（`javac` 会调用你注册的 Processor）
- 可以**生成新代码**（生成字段、方法、甚至新类）
- **性能开销为零**（运行时没有额外的反射）
- 实现复杂：需要继承 `AbstractProcessor`，操作 **AST（抽象语法树）**

**实现 APT 的门槛较高**（涉及 JSR-269 API），这是单独一个大话题。

### 方式 3：运行时反射（最常见）

**典型代表**：Spring、JPA、Jackson、JUnit

```java
@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

**工作原理**：
1. 注解保留级别是 `RUNTIME`
2. 程序启动时，框架**扫描类路径**找所有打了 `@Component` 的类
3. 框架**用反射实例化这些类**
4. 对每个实例，**扫描字段上的 `@Autowired`**，**用反射注入对应的 Bean**

**这就是 Spring 启动时"魔法"发生的核心**。

---

## 🛠 第四步：自己实现一个注解（实战）

现在你懂了原理。自己写一个"运行时反射"的注解 —— 这是**自己能实现的最常见方式**。

### 目标：实现一个 `@LogExecutionTime` 注解

加在方法上，自动打印方法执行耗时。

### 步骤 1：定义注解

```java
import java.lang.annotation.*;

@Target(ElementType.METHOD)             // 只能挂方法
@Retention(RetentionPolicy.RUNTIME)     // 运行时可见（必须！）
public @interface LogExecutionTime {
    String value() default "";
}
```

### 步骤 2：写"处理器"（自己实现反射逻辑）

```java
public class AnnotationProcessor {

    // 手动版"AOP"：扫描对象的方法，对打了注解的包一层
    public static Object invokeWithLogging(Object target, String methodName, Object... args)
            throws Exception {
        Method method = target.getClass().getMethod(methodName);

        if (method.isAnnotationPresent(LogExecutionTime.class)) {
            LogExecutionTime anno = method.getAnnotation(LogExecutionTime.class);
            long start = System.nanoTime();

            Object result = method.invoke(target, args);     // 反射调用

            long costNs = System.nanoTime() - start;
            System.out.printf("⏱ [%s] %s#%s 耗时 %.3f ms%n",
                anno.value(), target.getClass().getSimpleName(), methodName, costNs / 1_000_000.0);
            return result;
        } else {
            return method.invoke(target, args);
        }
    }
}
```

### 步骤 3：使用

```java
class OrderService {
    @LogExecutionTime("下单")
    public void placeOrder() {
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        System.out.println("订单已创建");
    }

    public void normalMethod() {
        System.out.println("普通方法");
    }
}

public class Demo {
    public static void main(String[] args) throws Exception {
        OrderService service = new OrderService();

        AnnotationProcessor.invokeWithLogging(service, "placeOrder");
        // 输出：订单已创建
        //      ⏱ [下单] OrderService#placeOrder 耗时 100.234 ms

        AnnotationProcessor.invokeWithLogging(service, "normalMethod");
        // 输出：普通方法
        // (没耗时日志，因为没打注解)
    }
}
```

**这就是一个最朴素的"手动 AOP"**！Spring 的 `@Transactional`、`@Async`、`@Cacheable` 本质上是这套思路 + 动态代理自动化了调用过程。

---

## 🌍 第五步：真实世界的注解是怎么工作的

回到你开头的疑问，一个个拆开：

| 注解 | 来源 | Retention | 处理方式 |
|------|------|-----------|---------|
| `@Override` | JDK | SOURCE | **编译器内置检查**（只做语法检查，不生成代码） |
| `@Deprecated` | JDK | **RUNTIME** | 编译器警告 + 运行时可查 |
| `@FunctionalInterface` | JDK | SOURCE | 编译器检查 |
| `@Data` / `@With` / `@Builder` | Lombok | SOURCE | **APT 编译期生成代码**（关键！）|
| `@Component` / `@Service` | Spring | RUNTIME | **运行时反射扫描 + 实例化** |
| `@Autowired` | Spring | RUNTIME | 运行时反射 + 依赖查找注入 |
| `@Transactional` | Spring | RUNTIME | 运行时 **动态代理 + AOP** |
| `@RequestMapping` | Spring MVC | RUNTIME | 运行时扫描 + 路由注册 |
| `@Entity` / `@Column` | JPA | RUNTIME | 运行时反射 + ORM 映射 |
| `@JsonProperty` | Jackson | RUNTIME | 运行时反射 + JSON 序列化 |
| `@Test` / `@Before` | JUnit | RUNTIME | 运行时反射 + 测试执行 |

---

## 🔬 深入：三种处理方式的对比

| 方面 | 编译器内置 | APT 注解处理器 | 运行时反射 |
|------|----------|--------------|----------|
| 发生时机 | 编译时 | 编译时 | 运行时 |
| 能否生成代码 | ❌ | **✅（核心能力）** | ❌ |
| 能否改已有代码 | ❌ | ❌（只能生成新的） | ❌（但可用代理"包"一层） |
| 性能开销 | 0 | 0 | 每次调用都有反射开销 |
| 实现难度 | 不可扩展 | **高**（操作 AST） | **低**（普通反射 API） |
| 代表案例 | `@Override` | Lombok / MapStruct / Dagger | Spring / JPA / Jackson |

### 进一步：运行时代理的"第四种方式"

Spring `@Transactional` 其实是**运行时反射 + 动态代理** 组合：

```
启动时：
  ① 扫描 @Service 类 → 实例化 bean
  ② 发现 bean 的方法上有 @Transactional
  ③ 用动态代理（CGLIB / JDK Proxy）创建一个"代理对象"
  ④ 代理对象拦截所有方法调用：
      - 调用前：开启事务
      - 调用实际方法
      - 调用后：提交 / 回滚
  ⑤ Spring 容器里保存的是【代理对象】，不是原对象

你调 userService.save() →
  实际调到代理 → 代理先开事务 → 再调真实方法 → 提交事务
```

**这就是 "AOP"（Aspect-Oriented Programming）**，Java 里主要通过 **JDK 动态代理** 或 **CGLIB / ByteBuddy 字节码增强** 实现。

详细讲解可以单开一篇（等到学**代理模式**时会再回到这个主题）。

---

## 🎁 你最想问的 `@With` 是怎么实现的？

Lombok 的 `@With` 是 **APT 编译期生成代码**。

### 你写的：

```java
@With
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
```

### Lombok APT 处理器在编译期做了什么

看到 `@With` → 扫描所有字段 → 为每个字段生成一个 `withXxx` 方法：

```java
// 生成后（你在 class 文件里能反编译出来）
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) { ... }

    public Point withX(int x) {                // 👈 生成
        return this.x == x ? this : new Point(x, this.y);
    }

    public Point withY(int y) {                // 👈 生成
        return this.y == y ? this : new Point(this.x, y);
    }
}
```

**运行时没有任何反射开销**。就像你手写了这些方法。

### 自己实现一个"简化版 Lombok"？

可以，但**难度很大**：
- 需要实现 `javax.annotation.processing.Processor`
- 需要操作 AST（抽象语法树）
- Lombok 还用了"黑魔法"：动态修改 javac 内部的 AST（这不是官方 API）

**一般人不会这么写**。如果真有需求，下面这些库可以考虑：
- **JavaPoet**（Square）：优雅生成 .java 文件
- **KSP**（Kotlin）：Kotlin 官方符号处理器
- **Google Auto**：注解驱动代码生成

**简单场景**：**运行时反射**就够用了（见前面的 `@LogExecutionTime` 例子）。

---

## ⚠️ 常见误区

### 误区 1：注解自己执行逻辑
**错**。注解只是元数据。没有"处理代码"，注解等于没写。

### 误区 2：所有注解都能运行时反射读到
**错**。只有 `@Retention(RUNTIME)` 的才能。`@Override` 是 SOURCE 级别，运行时读不到。

### 误区 3：Lombok 是运行时魔法
**错**。Lombok 是**编译期**魔法，生成代码。运行时什么都不做。

### 误区 4：Spring 所有注解都是反射
**主要对**，但 `@Transactional` / `@Async` 这类还依赖**动态代理**。

### 误区 5：自己写注解一定要用 APT
**错**。大多数场景用运行时反射就够了，APT 是"生成代码"场景的高级手段。

### 误区 6：@Override 是关键字
**错**。它只是一个 `@interface` 注解。只是被 javac 特殊对待。

---

## 🎯 回到你的三个问题

### 1. `@Override` 是"语言层实现"吗？
**更准确的说法**：它是 JDK 内置的**注解**，被 javac 编译器**特殊处理**。不是语言级关键字。

### 2. `@With` 是怎么实现的？
**Lombok APT 注解处理器**在**编译期**读到它 → 生成 `withXxx` 方法 → 插入到编译结果。运行时没有任何额外开销。

### 3. Spring 的 `@Autowired` 怎么知道注入哪个？
**运行时反射**：
1. Spring 扫描 `@Component` 类 → 放进 IoC 容器
2. 扫描字段上的 `@Autowired` → 查容器里对应类型的 Bean
3. 反射赋值到字段

---

## 📌 一张图总结

```
注解的三种生命周期 + 对应的处理方式：

@Retention(SOURCE)
    ├── 典型：@Override, @SuppressWarnings
    └── 处理：编译器内置或 APT 在编译期读取（class 文件就丢了）
              ↓ 能生成新代码？
              ├── APT 可以（Lombok, MapStruct, Dagger）
              └── 编译器内置不能（只做检查）

@Retention(CLASS)
    ├── 典型：很少用
    └── 处理：在 class 文件，但运行时反射读不到

@Retention(RUNTIME)
    ├── 典型：@Autowired, @Component, @Entity, @Test
    └── 处理：运行时反射读取
              ↓ 怎么做事？
              ├── 单纯反射（Spring @Autowired）
              └── 反射 + 动态代理（Spring @Transactional）
```

---

## 🙋 这是大议题 —— 你想深入哪块？

本篇是"**总览 + 运行时反射实战**"。如果你想深入某块：

| 主题 | 拆分建议 | 难度 |
|------|---------|------|
| APT 注解处理器实战（自己写 Lombok） | 单独一篇 | ★★★★ |
| 动态代理 + AOP 实现 | 单独一篇（结合代理模式学） | ★★★ |
| Spring IoC 容器原理 | 单独一篇 | ★★★ |
| 字节码增强（ASM / ByteBuddy） | 单独一篇 | ★★★★★ |

**我的建议**：
- 先把本篇吃透 → 运行时反射处理注解
- 等后面学**代理模式**时，我开一篇"**动态代理 + AOP 实战**"
- 等你有需要自己写代码生成器时，再学 APT

---

## 📚 相关笔记

- [07 Override vs Overload](./07-Override-vs-Overload.md) —— `@Override` 首次出现
- [13 Record 与现代数据类](./13-Record与现代数据类.md) —— Lombok `@With` 的对比
- [10 Objects 工具类 & 工具类设计](./10-工具类设计与Objects.md) —— 工具类和注解的对比

---

## 📌 一句话总结

> **注解是代码上的元数据标签，自己不执行任何逻辑**。真正干活的是：**编译器内置处理**（`@Override`）、**APT 编译期生成代码**（Lombok）、**运行时反射**（Spring）。自己实现注解最简单的方式是 **`@Retention(RUNTIME)` + 反射读取**。想像 Lombok 那样"生成代码"则需要 APT（高阶话题）。
