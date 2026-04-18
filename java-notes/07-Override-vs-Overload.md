# Override vs Overload · 重写 vs 重载

> 📍 来源：设计模式学习 **工厂方法 · ChicagoCheesePizza.cut()**
> 🎯 回答的核心问题："**`@Override` 是什么？它和方法重载是一回事吗？**"

---

## 🤔 场景问题

在工厂方法的 `FactoryMethodDemo.java` 里看到这段代码：

```java
class ChicagoCheesePizza extends Pizza {
    @Override
    public void cut() {
        System.out.println("  🔪 切成方块: " + name);
    }
}
```

疑问：
1. `@Override` 这个 `@` 符号开头的是什么？
2. 中文"重写"和"重载"是同一件事吗？

---

## 🧠 核心结论

> **Override（重写）** 和 **Overload（重载）** 是**两个完全不同**的概念。
> **`@Override` 是一个注解**，用来标记"我是故意重写父类方法的"，让编译器帮你做校验。

---

## 📊 Override vs Overload 完整对比

| 对比项 | **Override 重写** | **Overload 重载** |
|--------|------------------|------------------|
| 中文 | 重写 / 覆盖 | 重载 |
| 发生在哪 | **父类 vs 子类**之间 | **同一个类**里 |
| 方法签名 | **必须完全相同**（名 + 参数 + 返回） | 名字相同，**参数必须不同** |
| 用意 | 子类换一种实现 | 同一个方法名支持多种调用方式 |
| 分派时机 | 运行时（动态分派） | 编译期（静态分派） |
| 多态性 | ✅ 支持多态 | ❌ 不涉及多态 |

---

## 🔹 Override 示例

```java
class Animal {
    public void speak() {
        System.out.println("动物在叫");
    }
}

class Dog extends Animal {
    @Override
    public void speak() {                    // 重写：同名同参数
        System.out.println("汪汪汪");
    }
}

class Cat extends Animal {
    @Override
    public void speak() {                    // 重写
        System.out.println("喵喵喵");
    }
}

// 使用（多态）
Animal a1 = new Dog();   a1.speak();        // 输出：汪汪汪
Animal a2 = new Cat();   a2.speak();        // 输出：喵喵喵
```

**关键**：变量 `a1` 声明类型是 `Animal`，但**实际对象是 `Dog`**。Java 运行时根据**实际对象类型**调用对应子类的方法 —— 这就是**动态分派 / 多态**。

---

## 🔹 Overload 示例

```java
class Calculator {
    int add(int a, int b) {
        return a + b;
    }

    double add(double a, double b) {         // 重载：参数类型不同
        return a + b;
    }

    int add(int a, int b, int c) {           // 重载：参数个数不同
        return a + b + c;
    }

    // ⚠️ 以下不算重载：仅返回值不同不能重载！
    // double add(int a, int b) { return a + b; }  // 编译错误
}

Calculator c = new Calculator();
c.add(1, 2);           // 调 int add(int, int)
c.add(1.0, 2.0);       // 调 double add(double, double)
c.add(1, 2, 3);        // 调 int add(int, int, int)
```

**关键**：编译器**编译时就能决定**调哪个版本（根据实参类型）。和运行时对象类型无关。

---

## 🎯 `@Override` 注解详解

### 是什么
`@Override` 是 **Java 内置注解**（Java 5 引入），位于 `java.lang` 包。

### 作用
**告诉编译器："这个方法是故意重写父类的"**，让编译器做两项检查：

1. 父类是否真有这个方法？
2. 参数、返回值、泛型是否真的匹配？

### 不加会怎样？—— 经典坑

```java
class Pizza {
    public void cut() { System.out.println("切 8 块"); }
}

class BadPizza extends Pizza {
    public void cute() {                     // 😱 打错字！cut → cute
        System.out.println("切方块");
    }
}

new BadPizza().cut();     // 输出："切 8 块"（调用了父类的 cut）
```

**悲剧**：
- 编译器以为你在**定义一个新方法** `cute()`，不报错
- 你以为重写成功，运行时却发现行为没变化
- 这种 bug **极难排查**

### 加上 `@Override` 之后

```java
class BadPizza extends Pizza {
    @Override
    public void cute() {                     // ✅ 编译器立刻报错！
        // Compile error: Method does not override method from its superclass
    }
}
```

编译器立刻发现"父类根本没 cute 方法"，拒绝编译。错误在**上线前**就被捕获。

### 🏆 铁律
> **每个重写方法都应该加 `@Override`。** 这是 Java 开发无条件遵守的规范。

---

## 🧪 动态分派 vs 静态分派

**Override 是运行时动态分派**：

```java
Animal a = new Dog();
a.speak();
//  ↑
//  编译期：Animal 类型有 speak 方法 → 可以调
//  运行期：查 a 实际对象 → Dog → 跳去 Dog.speak()
```

**Overload 是编译期静态分派**：

```java
Calculator c = new Calculator();
c.add(1, 2);
//  ↑
//  编译期：参数类型 (int, int) → 绑定 int add(int, int)
//  运行期：直接调已绑定的版本
```

---

## ⚠️ 常见坑（和 Override 相关）

### 坑 1：静态方法不能被"重写"，只能被"隐藏"（hide）
```java
class Parent {
    static void foo() { System.out.println("parent"); }
}
class Child extends Parent {
    static void foo() { System.out.println("child"); }
}

Parent p = new Child();
p.foo();          // 输出 "parent" —— 按编译类型，不是运行时对象类型
```

**区别**：static 方法**不参与多态**。子类同名 static 方法"隐藏"了父类的，而不是"重写"。

### 坑 2：`private` 方法不能被重写
```java
class Parent {
    private void foo() { }
}
class Child extends Parent {
    @Override                                // ❌ 编译错误
    private void foo() { }                   // Parent.foo 对 Child 不可见
}
```

**private 方法对子类不可见**，所以谈不上"重写"。

### 坑 3：`final` 方法不能被重写
```java
class Parent {
    public final void foo() { }
}
class Child extends Parent {
    @Override                                // ❌ 编译错误
    public void foo() { }                    // foo is final in Parent
}
```

**`final` 方法是显式禁止重写**。这就是工厂方法 `orderPizza(type)` 为什么用 `final` —— 防止子类擅自修改流程。

### 坑 4：返回类型可以是"协变"的
```java
class Parent {
    public Number getValue() { return 0; }
}
class Child extends Parent {
    @Override
    public Integer getValue() { return 0; }  // ✅ Integer 是 Number 的子类，合法
}
```

**规则**：子类重写方法的返回类型，可以是父类方法返回类型的**子类型**。这叫协变返回类型（Java 5+）。

### 坑 5：访问修饰符可以放宽，不能收窄
```java
class Parent {
    protected void foo() { }
}
class Child extends Parent {
    @Override
    public void foo() { }      // ✅ 放宽：protected → public，合法
    // private void foo() { }  // ❌ 收窄：protected → private，编译错误
}
```

---

## 🎁 顺便：Java 注解（Annotation）基础

`@Override` 只是内置注解之一。Java 还有很多：

| 注解 | 作用 |
|------|------|
| `@Override` | 声明"我在重写父类方法" |
| `@Deprecated` | 声明"这个已过时，别再用了" |
| `@SuppressWarnings("unchecked")` | 抑制特定警告 |
| `@FunctionalInterface` | 声明这是函数式接口（只能有一个抽象方法） |
| `@SafeVarargs` | 声明变长参数是类型安全的 |

**注解本身不执行任何逻辑**，它只是"元数据"（metadata），由编译器或框架读取后做相应处理。

第三方框架的注解（比如 Spring 的 `@Autowired`、JPA 的 `@Entity`）则依赖**反射**在运行时读取，实现特殊功能。这个以后遇到代理模式再展开。

---

## 💡 一句话记忆

> **Override = 子类换实现**（父子间，运行时多态）
> **Overload = 同名多版本**（同类里，编译期分派）
>
> **`@Override` 注解让编译器帮你防止"以为重写成功其实打错字"的 bug。每次重写都加上。**

---

## 🔗 相关深入

- **Java 注解处理器（APT）** —— 编译时读取注解的机制
- **反射 + 运行时注解** —— Spring 等框架的注解魔法原理
- **Liskov 替换原则（LSP）** —— 重写应遵守的父子类行为契约

---

## 📌 场景应用

回到工厂方法：

```java
class ChicagoCheesePizza extends Pizza {
    @Override
    public void cut() {                      // 🎯 Override: 子类换实现
        System.out.println("  🔪 切成方块: " + name);
    }
}
```

**父类 `Pizza.cut()` 是默认切 8 块**，芝加哥披萨重写了它，改成切方块。

当 `orderPizza` 里调 `pizza.cut()`：
- `pizza` 声明类型是 `Pizza`
- 实际对象是 `ChicagoCheesePizza`
- Java 运行时动态分派 → 调**子类**的 `cut()` → 切方块

**这就是多态 + 重写 + 工厂方法协同工作的画面**。
