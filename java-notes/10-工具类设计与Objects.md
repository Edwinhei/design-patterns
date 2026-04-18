# Java 工具类设计 & Objects 详解

> 📍 来源：建造者课 · `Objects.requireNonNull(bread, "面包不能为 null")`
> 🎯 回答的核心问题："**`Objects` 是什么？为什么 `Objects` 能不 new 就用？自己怎么写工具类？**"

---

## 🤔 场景问题

建造者课 Builder 构造器里这行代码：

```java
this.bread = Objects.requireNonNull(bread, "面包不能为 null");
//               ↑
//       这 Objects 到底是什么东西？为什么不用 new 就能直接调方法？
```

---

## 🧠 核心结论

> **Java 的"工具类"遵循一套固定设计模式：`final class` + `private` 构造器 + 全 `static` 方法。**
>
> **"不让继承、不让实例化、直接用类名调方法"** —— 这就是工具类的通用形态。

---

## 🏗 工具类的"三件套"设计

所有 Java 工具类都长这样：

```java
public final class 工具类名 {              // ① final → 不能继承
    private 工具类名() {}                  // ② private 构造器 → 不能 new
    public static 返回类型 方法名(...) {}   // ③ 全 static → 直接类名调
}
```

**JDK 源码验证**（`Objects` 的真实结构）：

```java
package java.util;

public final class Objects {              // ① final
    private Objects() {                   // ② private
        throw new AssertionError("No java.util.Objects instances for you!");
    }

    public static <T> T requireNonNull(T obj, String message) {    // ③ static
        if (obj == null) throw new NullPointerException(message);
        return obj;
    }
    // ... 一堆其他 static 方法
}
```

三件套缺一个，工具类就"不纯"了。

---

## 🌍 JDK 经典工具类家族

| 工具类 | 所在包 | 用途 | 常用方法 |
|--------|--------|------|---------|
| `Objects` | `java.util` | 对象/空值工具 | `requireNonNull`, `equals`, `hash` |
| `Arrays` | `java.util` | 数组工具 | `sort`, `asList`, `stream`, `toString` |
| `Collections` | `java.util` | 集合工具 | `sort`, `emptyList`, `unmodifiableList` |
| `Math` | `java.lang` | 数学工具 | `max`, `min`, `abs`, `random`, `sqrt` |
| `Optional` | `java.util` | 可选值工具 | `of`, `empty`, `ofNullable` |
| `Collectors` | `java.util.stream` | Stream 收集器 | `toList`, `groupingBy`, `joining` |
| `Files` | `java.nio.file` | 文件工具 | `readAllBytes`, `write`, `exists` |
| `Executors` | `java.util.concurrent` | 线程池工厂 | `newFixedThreadPool`, `newCachedThreadPool` |

**识别规律**：
- 类名多为**复数**（`Objects`, `Arrays`, `Collections`, `Files`）
- 或带"工具"色彩词（`Math`, `Collectors`, `Executors`）
- **类名和对象名区分**：`Object`（首字母大写单数）是所有类的父类；`Objects`（复数）是工具类

---

## 📌 静态方法 vs 实例方法

### 对比表

| 对比项 | 静态方法 | 实例方法 |
|--------|---------|---------|
| 关键字 | `static` | 无 |
| 所属 | **类** | **对象** |
| 调用方式 | `类名.方法()` | `对象.方法()` |
| 能访问实例字段吗 | ❌ | ✅ |
| 能用 `this` 吗 | ❌ | ✅ |
| 何时加载 | 类加载时 | 对象创建时 |
| 多态吗 | ❌（静态绑定） | ✅（动态分派） |

### 代码对比

```java
class Calculator {
    private int memory;                          // 实例字段

    // 实例方法：可用 this，可访问实例字段
    public void add(int x) {
        this.memory += x;
    }

    // 静态方法：没有 this，不能访问实例字段
    public static int multiply(int a, int b) {
        return a * b;
    }
}

// 使用
Calculator c = new Calculator();
c.add(5);                            // 实例方法：对象.方法
Calculator.multiply(3, 4);           // 静态方法：类名.方法
```

### 什么时候用静态方法？

**静态方法适用于"纯函数"**：
- 不依赖对象状态
- 同样输入产生同样输出
- 无副作用

**典型场景**：数学计算、空值校验、字符串处理、数据转换

**不要把应该是实例方法的东西写成静态**：如果方法需要访问"某个对象的状态"，就应该是实例方法。

---

## 🔧 Objects 常用 API 速查

### 1. `requireNonNull` —— 空值校验 + 抛异常

```java
this.bread = Objects.requireNonNull(bread, "面包不能为 null");

// 等价于：
if (bread == null) throw new NullPointerException("面包不能为 null");
this.bread = bread;
```

### 2. `isNull` / `nonNull` —— 空值检查（不抛异常）

```java
Objects.isNull(obj);    // obj == null
Objects.nonNull(obj);   // obj != null

// 常用在 Stream 过滤
list.stream().filter(Objects::nonNull).collect(toList());
```

### 3. `equals` —— null 安全的 equals

```java
// 老写法（可能 NPE）
a.equals(b)                       // 如果 a 是 null，NPE

// 新写法（null 安全）
Objects.equals(a, b)              // a、b 任一为 null 也不抛异常
```

**实现**：

```java
public static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
}
```

### 4. `hash` —— 多字段组合生成 hashCode

```java
class User {
    private String name;
    private int age;
    private String email;

    @Override
    public int hashCode() {
        return Objects.hash(name, age, email);   // 一行搞定
    }
}
```

### 5. `toString` —— null 安全的 toString

```java
Objects.toString(obj);                    // obj 为 null 时返回 "null"
Objects.toString(obj, "默认值");          // obj 为 null 时返回 "默认值"
```

---

## 🎯 `this` 关键字的三大用法

### 用法 1：区分字段和同名参数（最常见）

```java
class User {
    private String name;

    public User(String name) {
        this.name = name;
        //  ↑         ↑
        //  字段      参数
    }
}
```

**关键**：**如果方法参数和字段重名**，`this.xxx` 指字段，纯 `xxx` 指参数。

### 用法 2：显式调用本对象的方法/字段

```java
class User {
    public void save() {
        validate();              // 隐式：this.validate()
        this.writeToDatabase();  // 显式：更清晰
    }
}
```

大多数时候可以省略 `this.`，写上更清晰（特别是代码较长时）。

### 用法 3：构造器互相调用

```java
class User {
    private String name;
    private int age;

    public User() {
        this("unknown", 0);      // 👈 调用另一个构造器
    }

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

**规则**：`this(...)` 调用其他构造器必须在**第一行**。

---

## 🎭 工具类 vs 单例：区别在哪

两者看着像，核心差异：**是否有实例状态**。

| | 单例 | 工具类 |
|--|------|------|
| 构造器私有 | ✅ | ✅ |
| 推荐禁止继承 | ✅ | ✅ |
| 如何使用 | `X.getInstance().foo()` | `X.foo()` |
| **有实例状态** | **✅（唯一实例持有状态）** | **❌（纯函数）** |
| 典型场景 | 配置、日志、连接池 | 计算、转换、校验 |

### 例子对比

**单例：有状态**
```java
// AppConfig 是单例，里面有配置 Map（状态）
AppConfig.INSTANCE.set("db.url", "...");
String url = AppConfig.INSTANCE.get("db.url");
```

**工具类：无状态**
```java
// Math 无状态，只是纯计算
int max = Math.max(1, 2);
double pi = Math.PI;                  // 常量也是无状态的
```

### 选择指南

- 有"全程序共享的状态" → 单例
- 纯计算 / 转换 / 校验 → 工具类

---

## 📋 自己写工具类的 checklist

```java
public final class StringUtils {              // ① final：禁止继承
    private StringUtils() {                    // ② private 构造：禁止 new
        throw new AssertionError("工具类禁止实例化");
    }

    public static boolean isEmpty(String s) { // ③ 全 static：直接调
        return s == null || s.isEmpty();
    }

    public static String reverse(String s) {
        return s == null ? null : new StringBuilder(s).reverse().toString();
    }
}
```

**完整 checklist**：

- [ ] 类加 `final`
- [ ] 构造器 `private` 且抛异常（防反射）
- [ ] 所有方法 `public static`
- [ ] 方法是**纯函数**（不依赖全局状态）
- [ ] 方法名清晰（`isXxx`、`toXxx`、`requireXxx`）
- [ ] 方法参数的 null 检查（用 Objects.requireNonNull）

---

## ⚠️ 常见误区

### 误区 1："`Objects` 就是 `Object`"
**完全不一样**。
- `Object`（单数）：**所有 Java 类的根父类**
- `Objects`（复数）：**对象工具类**，里面是 static 方法

### 误区 2："工具类方法越多越好"
**错**。工具类方法应该精挑细选，只放"真正通用"的。什么都往 `StringUtils` 塞会变成垃圾桶。

### 误区 3："我的工具类不需要 final"
**要加**。虽然"工具类没人会继承"，但加 `final` 是明确的设计声明，防止未来有人犯傻。

### 误区 4："可以 new Math()"
**不行**。`Math` 的构造器是 private，new 编译错误。你试试就知道了。

### 误区 5："工具类里可以有实例字段？"
**技术上可以，但没意义**。工具类的使命是提供"无状态静态函数"。有实例字段意味着你应该重新考虑是否该用类（可能是单例）。

---

## 🎁 经典设计：为什么 `Math.PI` 是 `public static final`？

```java
public final class Math {
    public static final double PI = 3.14159265358979323846;
    // ...
}

// 使用
Math.PI       // 3.14159...
```

- `static` → 不用 new，直接用
- `final` → 值不可变（π 值固定）
- `public` → 让所有人都能用
- 没有 setter → 绝对不能改

**这是"公开常量"的标准写法**：`public static final`，简称 `PSF`。

你自己定义常量也应该：

```java
public final class Constants {
    public static final int MAX_RETRIES = 3;
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final Duration TIMEOUT = Duration.ofSeconds(30);
}
```

---

## 🔗 相关深入

- **Lombok `@UtilityClass`** —— 注解自动生成工具类三件套
- **函数式接口 + Lambda** —— 更灵活的"纯函数"表达（Java 8+）
- **Kotlin 的 `object` 关键字** —— 原生支持"单例/静态工具类"

---

## 📌 一句话总结

> **Java 工具类 = `final class` + `private` 构造器 + 全 `static` 方法。JDK 里 Objects、Math、Arrays、Collections 都是这个套路。看到一个类名带"s"复数、没法 new、方法都用类名调 —— 九成是工具类。**
