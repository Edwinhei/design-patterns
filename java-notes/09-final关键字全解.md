# final 关键字全解 · 不可变性的基石

> 📍 来源：建造者课 · `SubwaySandwich` 字段全 `final`
> 🎯 回答的核心问题："**final 字段到底是什么？只声明不赋值会怎样？**"

---

## 🤔 场景问题

Builder 课的 `SubwaySandwich` 所有字段都是 `final`：

```java
class SubwaySandwich {
    private final String bread;
    private final String meat;
    private final List<String> vegetables;
    private final boolean toasted;
    // ...
}
```

疑问：
1. `final` 到底意味什么？
2. 只声明 `private final String x;` 不赋值，会怎样？
3. 什么时候该用 final？

---

## 🧠 核心结论

> **`final` = 一次性赋值，之后不能再改。**
>
> 对字段而言：**必须**在"声明 / 初始化块 / 构造器"三者之一完成赋值，否则编译错误。

---

## 📋 `final` 的四种用法

### 用法 1：修饰字段 → 不能重新赋值

```java
class User {
    private final String name;

    public User(String name) {
        this.name = name;    // ✅ 构造器里赋值
    }
}

User u = new User("Alice");
u.name = "Bob";   // ❌ 编译错误
```

### 用法 2：修饰方法 → 不能被子类重写

```java
class Parent {
    public final void hello() { }
}

class Child extends Parent {
    @Override
    public void hello() { }    // ❌ 编译错误：Cannot override final method
}
```

**典型场景**：工厂方法模式的 `orderPizza(type)` 用 `final` —— 防止子类篡改流程。

### 用法 3：修饰类 → 不能被继承

```java
public final class String { ... }    // JDK 里 String 就是 final，所以你不能 extends String
```

**典型场景**：`String`、`Integer`、`Long` 等不可变类，设计者**不希望被继承**（继承会破坏不可变性）。

### 用法 4：修饰局部变量 / 方法参数 → 方法内不能修改

```java
public void foo(final int x) {
    x = 100;    // ❌ 编译错误
}
```

**典型场景**：Lambda 表达式 / 匿名内部类里引用外部变量时，JDK 要求外部变量必须是 `final` 或 **effectively final**（实际上没改过）。

---

## 📅 `final` 字段的三种赋值时机

**必须且只能在以下三处之一赋值**：

### 时机 1：声明时直接赋值
```java
class User {
    private final String country = "中国";
}
```

### 时机 2：实例初始化块
```java
class User {
    private final String country;
    {
        this.country = "中国";
    }
}
```

### 时机 3：构造器里赋值（最常见）
```java
class User {
    private final String name;
    public User(String name) {
        this.name = name;
    }
}
```

### 🚫 不赋值会怎样？**编译错误**

```java
class User {
    private final String name;           // 只声明

    public User() {
        // 构造器里也不赋值
    }
}
// 编译器报错：variable name might not have been initialized
```

Java 强制要求 final 字段在构造结束前**一定有值**，**在编译期**就能发现错误。

---

## 🔐 `static final` 字段

```java
class Constants {
    public static final int MAX_SIZE = 100;       // 必须在声明或 static 块中赋值
    public static final List<String> DEFAULT;
    static {
        DEFAULT = List.of("a", "b", "c");          // 或用静态初始化块
    }
}
```

**赋值时机**：
- 声明时赋值
- 或 `static {}` 静态初始化块里赋值
- **不能**在构造器里赋值（因为 static 字段属于类，不属于实例）

---

## 💡 `final` 的"隐藏 buff"：线程安全

这是很多人忽略的硬核知识点。

**JMM（Java 内存模型）特殊保证**：**final 字段在构造器中正确赋值后，对其他线程一定可见**，且**不会看到"半初始化"状态**。

```java
class ImmutableUser {
    private final String name;
    private final int age;

    public ImmutableUser(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

这个对象：
- ✅ **创建后字段永远不变**
- ✅ **多线程共享无需加锁**
- ✅ **即使没 volatile，其他线程读到的一定是完整值**

**回忆 DCL 单例的"半初始化"坑**（参见 [04-volatile与指令重排](./04-volatile与指令重排.md)）：
> 普通字段下，A 线程正在构造对象时 B 线程可能看到"非 null 但字段还没赋值"的半成品。
>
> 但如果字段都是 **final**，JMM 保证 B 看到的**一定**是构造完成后的完整对象。这就是为什么 `String` 等不可变类可以安全地在多线程共享。

---

## ⚠️ 最大陷阱：`final` ≠ 对象不可变

这是 Java 初学者**最容易踩的坑**。

```java
final List<String> list = new ArrayList<>();

list = new ArrayList<>();   // ❌ 编译错误：不能重新赋值 list

list.add("hello");          // ✅ 完全合法！对象内部状态可以修改
list.add("world");
list.remove(0);
```

**`final` 保护的是"**引用**"不能改变指向**，**不保护**引用指向的对象内部状态。

想让 List 真正不可变，需要：

```java
// 方式 1：List.of()（Java 9+，返回不可变 List）
final List<String> list = List.of("a", "b", "c");
list.add("d");    // ❌ UnsupportedOperationException

// 方式 2：Collections.unmodifiableList 包装
final List<String> list = Collections.unmodifiableList(original);
```

**"深不可变"需要**：
1. 字段 `final`
2. 引用指向的对象本身是不可变的
3. 或者提供**防御性复制**（Builder 里 `List.copyOf(b.vegetables)` 就是这招）

---

## 🎭 回到 Builder 模式：为什么这样设计

```java
class SubwaySandwich {
    private final String bread;               // final：产品不可变
    private final List<String> vegetables;

    private SubwaySandwich(Builder b) {
        this.bread = b.bread;
        this.vegetables = List.copyOf(b.vegetables);    // 防御性复制 + 不可变 List
    }
}

public static class Builder {
    private final String bread;                // 必选，构造时就定
    private String meat;                        // 👈 可选，不是 final（支持链式 setter）
    private List<String> vegetables = new ArrayList<>();
    // ...
}
```

**精巧设计**：
- **Builder 可变**（支持链式配置）
- **产品不可变**（构造后锁死，线程安全）
- **防御性复制** `List.copyOf` 防止 `b.vegetables` 事后被修改影响产品

---

## 🌍 `final` vs 其他语言

| 语言 | "不可变"声明 |
|------|------------|
| **Java** | `final` |
| **C++** | `const`（更强大，可修饰方法表示"不修改对象状态"） |
| **C#** | `readonly`（字段）/ `const`（编译期常量） |
| **Kotlin** | `val`（默认不可变，推崇）vs `var`（可变） |
| **Scala** | `val` / `var`（同 Kotlin） |
| **Rust** | `let`（默认不可变）vs `let mut`（可变） |
| **JavaScript** | `const`（引用不变，对象内部可变 —— 和 Java 完全一样！） |

**趋势**：现代语言（Kotlin、Rust、Scala）**默认不可变**，可变要显式声明。Java 是"默认可变，不可变要 final"，这被认为是"历史包袱"。

---

## 🎯 什么时候该用 final

### ✅ 强烈推荐用 final 的场景

1. **不可变对象的字段**（DTO、Value Object、配置类）
2. **Builder 模式的产品类**
3. **`@FunctionalInterface` 被 lambda 用的字段**
4. **工厂方法 / 模板方法中的"流程方法"**（禁止子类改流程）
5. **工具类**（`public final class StringUtils`，防止被继承 + 破坏）
6. **常量**（`public static final`）

### ❌ 不要用 final 的场景

1. **简单业务类的字段**，需要被 ORM / Jackson 框架反射填充（需要 setter）
2. **可变实体类**（Entity）
3. **需要继承的类**（不能加在类上）

---

## ⚠️ 常见误区

### 误区 1："final 对象不能被修改"
**错**。只保证引用不能改，对象内部完全可变。见前文 ArrayList 的例子。

### 误区 2："final 能防止任何形式的修改"
**反射能绕过 final**（`Field.setAccessible(true)` + `Field.set()`），但这是刻意破坏，不在正常使用范围。

### 误区 3："所有字段都加 final 是最佳实践"
**要看情况**。ORM 实体、JavaBean、需要 setter 的类都不能全 final。但"能加就加"是好习惯，强制你思考"这个字段真的需要可变吗"。

### 误区 4："final 变量性能更好"
**几乎没有性能差异**。早期 JVM 可能利用 final 做优化，现代 JVM（JIT）对普通变量也能达到同样优化。**用 final 的理由是语义和正确性**，不是性能。

### 误区 5："只要字段是 final 对象就是线程安全的"
**远远不够**。只有"**所有字段 final + 字段类型本身不可变 + 没有方法修改状态**"，才是真正的不可变对象。

---

## 📎 补充：final 字段赋值的边界情况

### "没赋值" vs "赋值为 null" —— 编译器只看字面

编译器判定 final 字段"是否已赋值"的标准**非常字面** —— **只看是否有赋值语句**，不看值是什么：

| 情况 | 编译器判定 |
|------|-----------|
| 有赋值语句（值任意，包括 null） | ✅ 通过 |
| 完全没有赋值语句 | ❌ 失败 |

### 两段对比

```java
// ❌ 编译失败
class A {
    final String x;
    public A() {
        // 没有任何赋值语句
    }
}
// 报错：variable x might not have been initialized

// ✅ 编译成功
class B {
    final String x;
    public B() {
        this.x = null;    // 显式赋值为 null，合法
    }
}
// 运行时 b.x 的值就是 null
```

### Builder 模式的应用

这个规则让 Builder 模式能与 final 产品字段完美共处：

```java
public static class Builder {
    private final String bread;    // 必选：Builder 构造时赋值
    private String meat;            // 👈 非 final，默认值 null
    private String sauce;           // 👈 非 final，默认值 null
}

class SubwaySandwich {
    private final String meat;      // 👈 final
    private final String sauce;

    private SubwaySandwich(Builder b) {
        this.meat  = b.meat;        // 总是执行赋值（哪怕 b.meat 是 null）
        this.sauce = b.sauce;       // 编译器满意 ✅
    }
}
```

**场景还原**：

用户调 `builder("全麦").build()`，没调 `.meat(...)`：

1. Builder 的 `meat` 字段保持默认 null
2. `build()` 调 SubwaySandwich 构造器
3. 构造器执行 `this.meat = b.meat` → **this.meat = null**
4. 编译器看到"有赋值动作"，合法通过
5. 运行时 `sandwich.meat` 的值就是 null
6. `describe()` 里 `if (meat != null)` 判断，不输出

**核心规则**：**编译器只管语法合规，不管运行时值是什么**。

---

## 🎁 不可变类设计清单

想设计一个真正的不可变类（像 `String` 那样），checklist：

- [ ] 类用 `final` 修饰（不让继承破坏）
- [ ] 所有字段 `private final`
- [ ] 不提供任何 setter
- [ ] 构造器里对可变对象做**防御性复制**
- [ ] getter 返回可变对象时也做**防御性复制**或返回不可变视图
- [ ] 避免暴露内部可变状态的引用

完整示例：

```java
public final class ImmutablePoint {
    private final int x;
    private final int y;
    private final List<String> tags;

    public ImmutablePoint(int x, int y, List<String> tags) {
        this.x = x;
        this.y = y;
        this.tags = List.copyOf(tags);         // 防御性复制 + 不可变视图
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public List<String> getTags() { return tags; }    // List.copyOf 返回不可变 List
}
```

这个类可以安全在多线程间共享，不需要任何锁。

---

## 📌 一句话总结

> **`final` 字段 = "一次性赋值，之后不能改"**，必须在声明/初始化块/构造器三者之一赋值。**只保护引用，不保护对象内部**。配合 JMM 特性可带来天然线程安全，是不可变对象设计的基石。

---

## 🔗 相关深入

- **JMM 内存模型**：final 的可见性保证机制
- **Record**（Java 14+）：一行代码声明不可变类的现代方式
- **不可变集合**：`List.of`、`Set.of`、`Map.of`（Java 9+）
- **Valhalla 项目**：Java 未来的 value type，默认不可变
