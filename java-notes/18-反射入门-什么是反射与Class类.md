# 反射入门（一）· 什么是反射 & Class 类

> 📍 来源：代理模式课引出的动态代理 → 需要先懂反射
> 🎯 本篇目标：**只讲两件事** —— "反射是什么" + "Class 类怎么用"
> 📚 本系列共 4 篇（18 / 19 / 20 / 21），一篇一个主题，渐进式推进

---

## 🤔 场景问题

你写代码时通常是这样：

```java
User u = new User("张三");
u.sayHello();
```

**编译期就知道**有 User 类、有 sayHello 方法。

但有时候，**运行时才知道要操作什么**。例如：

- 框架代码：Spring 扫描 `@Controller` 注解的类（哪些类有这个注解？运行时才知道）
- 插件系统：用户配置了 `handler=com.example.LoginHandler` 这个字符串
- JUnit：找所有带 `@Test` 的方法自动执行

**这些场景下你没法写 `new User()`，因为编译期不知道要 new 什么类**。

**解决方案：反射**。

---

## 🧠 反射是什么

> **反射（Reflection）= Java 运行时查看和操作类/对象的能力**。

一句话：**运行时拿到"类的说明书"，能查看 + 能操纵**。

## 反射能做什么（四件事）

| 能力 | 能干啥 |
|------|-------|
| ① 查看类信息 | 这个类叫什么、有哪些方法、哪些字段、有哪些注解 |
| ② 创建对象 | 不用 `new`，通过字符串类名动态创建 |
| ③ 调用方法 | 通过方法名字符串调方法 |
| ④ 读写字段 | 哪怕字段是 private 也能读写（危险但有用）|

**这四件事靠四个类完成**：

- `Class<T>` —— 类的"身份证"（本篇讲）
- `Method` —— 方法的"句柄"（下一篇讲）
- `Field` —— 字段的"句柄"（下一篇讲）
- `Constructor<T>` —— 构造器的"句柄"（下一篇讲）

---

## 📛 Class 类：类的"身份证"

### 它是什么

**Java 里每个类都自带一个对应的 `Class` 对象**，代表"这个类本身"。

就像每个人都有张身份证 —— `User.class` 就是 User 类的"身份证"。

**整个 JVM 里每个类只有一个 Class 对象**（类加载时创建一次）。

### 怎么拿到

**三种方式**：

```java
// 方式 1：类字面量（最常用）
Class<User> c1 = User.class;

// 方式 2：从已有对象拿
User u = new User();
Class<?> c2 = u.getClass();

// 方式 3：从字符串（运行时才知道类名时）
Class<?> c3 = Class.forName("com.example.User");
```

**三种方式拿到的是同一个对象**：

```java
System.out.println(c1 == c2);   // true
System.out.println(c1 == c3);   // true
```

### 常用 API

```java
Class<User> c = User.class;

// === 基本信息 ===
c.getName();                // "com.example.User"（全名）
c.getSimpleName();          // "User"（简名）
c.getSuperclass();          // 父类的 Class
c.getInterfaces();          // 实现的接口们

// === 获取方法 ===
c.getMethods();             // 所有 public 方法（含继承来的）
c.getDeclaredMethods();     // 本类声明的方法（含 private，不含继承）

// === 获取字段 ===
c.getFields();              // 所有 public 字段
c.getDeclaredFields();      // 本类声明的字段（含 private）

// === 获取构造器 ===
c.getConstructors();        // public 构造器
c.getDeclaredConstructors();// 所有构造器

// === 注解 ===
c.getAnnotations();         // 类上的注解
c.isAnnotationPresent(Deprecated.class);   // 是否有某注解
```

---

## 🧪 可运行示例

```java
import java.lang.reflect.*;

public class ClassDemo {
    public static void main(String[] args) {
        // 以 String 类为例
        Class<String> c = String.class;

        System.out.println("类名: " + c.getName());
        System.out.println("简名: " + c.getSimpleName());
        System.out.println("父类: " + c.getSuperclass().getName());

        System.out.println("\n前 5 个方法:");
        Method[] methods = c.getDeclaredMethods();
        for (int i = 0; i < Math.min(5, methods.length); i++) {
            System.out.println("  " + methods[i].getName());
        }
    }
}
```

**运行输出类似**：
```
类名: java.lang.String
简名: String
父类: java.lang.Object

前 5 个方法:
  equals
  toString
  hashCode
  length
  isEmpty
```

**你用反射"看透"了 String 类**。

---

## 🎯 Class 是什么？一图说清

```
     ┌──────────────────────────────────┐
     │   Java 虚拟机（JVM）              │
     │                                  │
     │   【方法区 / Metaspace】          │
     │                                  │
     │   User 类信息:                    │
     │   ┌────────────────────────┐     │
     │   │ Class<User> 对象       │     │
     │   │  - 类名                │     │
     │   │  - 字段列表            │     │
     │   │  - 方法列表            │     │
     │   │  - 注解列表            │     │
     │   │  - ...                 │     │
     │   └────────────────────────┘     │
     │                                  │
     │   任何时候 User.class 拿到的      │
     │   都是同一个 Class 对象           │
     └──────────────────────────────────┘
```

**Class 对象是"类的元数据"** —— 你通过它访问类的一切。

---

## 📌 本篇一句话总结

> **反射 = 运行时操作类的能力**。**`Class<T>` 是反射的入口** —— 每个类都有一个 Class 对象，通过它你能查看类的方法、字段、注解等所有信息。

---

## 🔗 下一步

**下一篇 → [19 · 反射进阶：Method / Field / Constructor](./19-反射进阶-Method与Field.md)**

在 Class 基础上，讲另外三个核心类：
- `Method` —— 怎么用反射调方法
- `Field` —— 怎么用反射读写字段
- `Constructor` —— 怎么用反射创建对象

**消化完本篇再读下一篇**。
