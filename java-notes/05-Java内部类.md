# Java 内部类 · 类里还能有类？

> 📍 来源：单例 V4 静态内部类
> 🎯 回答的核心问题："**Java 有内部类吗？类里面怎么能有类？**"

---

## 🤔 场景问题

单例 V4 静态内部类长这样：

```java
class HolderPrinter {
    private HolderPrinter() {}

    private static class Holder {                                  // 👈 类里面的类？！
        private static final HolderPrinter INSTANCE = new HolderPrinter();
    }

    public static HolderPrinter getInstance() {
        return Holder.INSTANCE;
    }
}
```

疑问：
1. Java 的类里不是只有字段和方法吗？怎么能有类？
2. `Holder` 怎么能访问 `HolderPrinter` 的私有构造器？

---

## 🧠 核心答案

> **Java 的一个类里不止能放字段和方法，还能放"嵌套类"（Nested Class）。**
> **嵌套类和外部类视为"同家人"，`private` 对家人无效。**

---

## 📦 类里能放什么 —— 完整清单

```java
class Outer {

    int field;                              // ① 字段
    void method() {}                        // ② 方法

    static { ... }                          // ③ 静态初始化块
    { ... }                                 // ④ 实例初始化块

    static class StaticNested {}            // ⑤ 静态内部类
    class Inner {}                          // ⑥ 成员内部类
    enum InnerEnum { A, B }                 // ⑦ 内部枚举
    interface InnerInterface {}             // ⑧ 内部接口

    void foo() {
        class Local {}                      // ⑨ 局部内部类
        Runnable r = new Runnable() {       // ⑩ 匿名内部类
            public void run() {}
        };
    }
}
```

**所以**：类里不止能放 ①② 字段方法，还能放**类、接口、枚举、代码块**。

---

## 🏢 生活比喻

> 一个"**公司**"（外部类）里不止有员工（方法）、办公设备（字段），
> 还可以有子公司（内部类）、部门（内部接口 / 枚举）、会议纪要（静态块）。

---

## 📊 四种"类里的类" 对比

| 类型 | 声明位置 | 是否 static | 需要外部类实例吗 | 典型用途 |
|------|---------|-----------|---------------|---------|
| 静态内部类 | 类体内 | ✅ | ❌ | 工具类、Builder、单例 |
| 成员内部类 | 类体内 | ❌ | ✅ 必须依附 | 紧耦合的辅助类（如 Map.Entry） |
| 局部内部类 | 方法内 | ❌ | ✅ | 方法内部临时使用 |
| 匿名内部类 | 表达式中 | ❌ | ✅ | 一次性实现接口/抽象类 |

---

## 🎯 重点：静态内部类（V4 用这个）

### 定义
```java
class Outer {
    static class StaticNested {
        void doSomething() { }
    }
}
```

### 关键特性
1. **不依赖外部类实例** —— 可以独立使用：
   ```java
   Outer.StaticNested x = new Outer.StaticNested();   // 不需要 new Outer() 先
   ```
2. **不会随外部类加载而加载** —— 这是 V4 懒加载的核心机制
3. **可以访问外部类的私有成员** —— "同家人规则"

---

## 🛡 "同家人规则"—— private 对嵌套类不生效

这是你最值得记的一条 Java 规则：

> **同一个外部类内部的嵌套类们，可以互相访问彼此的 `private` 成员。**

### 能做的

```java
class HolderPrinter {
    private HolderPrinter() {}                                 // 私有构造器

    private static class Holder {                              // 内部类
        static HolderPrinter I = new HolderPrinter();          // ✅ 能 new！
    }
}
```

### 不能做的

```java
class SomeOtherClass {                         // ❌ 一个独立的顶级类
    HolderPrinter h = new HolderPrinter();     // 编译错误！构造器私有
}
```

### 规则对比

| 场景 | 能访问 private 吗 |
|------|------------------|
| 类的方法访问自己的 private 字段 | ✅ 能 |
| 内部类访问外部类的 private | ✅ 能 |
| 外部类访问内部类的 private | ✅ 能 |
| 两个独立的顶级类之间 | ❌ 不能 |

**Java 的 `private` 作用域实际上是"同一个顶级类内部"，而不是"同一个类内部"。**
你平时感觉 private 挡住了一切，是因为你很少写嵌套类。

---

## 💡 静态内部类的妙用：实现懒加载单例

### 为什么"懒"？

```java
// ① 第一次访问 HolderPrinter 这个类（比如 HolderPrinter.getInstance 被调用）
HolderPrinter.getInstance();

// ② JVM 加载 HolderPrinter，但【不会】加载 Holder（因为还没引用到它）

// ③ 执行 getInstance() → 方法体里 return Holder.INSTANCE
//    这是第一次引用 Holder

// ④ JVM 此时才加载 Holder，并执行它的 <clinit>：
//    INSTANCE = new HolderPrinter()

// ⑤ 以后再调 getInstance()，Holder 已加载，直接返回 INSTANCE
```

**精髓**：**Java 规定"内部类不随外部类加载"**，所以把创建动作藏进内部类里，天然实现了懒加载。

### 为什么"线程安全"？

类加载过程**由 JVM 的 ClassLoader 上锁**。同一个类只会被初始化一次，即使多线程并发触发加载也由 JVM 串行化。

**等于 JVM 替你写好了 synchronized + 双检 + volatile 的效果。**

---

## 🎁 成员内部类（非静态）vs 静态内部类

### 成员内部类
```java
class Outer {
    int x = 1;
    class Inner {               // 非 static
        void foo() {
            System.out.println(x);   // 可以直接访问 Outer 的实例字段
        }
    }
}

// 使用：必须先有 Outer 实例
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();    // 奇怪的语法
```

**成员内部类隐式持有外部类的引用**（叫 `this$0`）。这会：
- ✅ 让内部类能访问外部类的实例字段和方法
- ❌ 带来**内存泄漏风险**（内部类存活期间，外部类无法被 GC）

### 静态内部类
```java
class Outer {
    int x = 1;
    static class StaticNested {
        void foo() {
            // System.out.println(x);   ❌ 编译错误，没有 Outer 实例引用
            // 但可以访问 Outer 的 static 字段
        }
    }
}
```

**静态内部类不持有外部类引用**，更独立、更安全。

**Android / Java 圈的经验法则**：
> **能用静态内部类就用静态内部类，除非你真的需要访问外部类实例字段。**

---

## ⚠️ 常见误区

### 误区 1："内部类是不是多此一举，直接写两个类不就行了？"
**不是**。内部类表达的是一种"**紧密从属**"关系：
- `Map.Entry` 离开 `Map` 没意义
- `Builder` 离开它构建的目标类没意义
- `Holder` 离开 `HolderPrinter` 没意义

把它写成内部类比写成独立类语义更清晰。

### 误区 2："内部类编译出来还是嵌套结构吧？"
**不是**。内部类会被编译成**独立的 .class 文件**，命名为 `Outer$Inner.class`。你在 `target/` 或 `out/` 目录能看到。

### 误区 3："匿名内部类里用到的外部变量必须是 final 的？"
**Java 8 起是"effectively final"（实际上没改过就行，不用显式写 final）**。

---

## 🔗 相关深入

- **Lambda 表达式 vs 匿名内部类** —— Java 8 后很多匿名类场景被 Lambda 取代
- **局部内部类捕获变量的原理** —— 编译器生成的合成字段
- **Builder 模式** —— 静态内部类的经典应用（第 3 课会学到）

---

## 📌 一句话总结

> **Java 的类里不止能放字段方法，还能放嵌套类。嵌套类和外部类是"同家人"，`private` 对家人无效。静态内部类 + JVM 按需加载 = 天然的懒加载 + 线程安全的单例方案。**
