# 反射入门（二）· Method / Field / Constructor

> 📍 来源：承接 [18 号反射入门](./18-反射入门-什么是反射与Class类.md)
> 🎯 本篇目标：讲透另外三个核心类 —— **Method**（调方法）、**Field**（读写字段）、**Constructor**（创建对象）
> 📖 前置：读完 18 号笔记

---

## 🧠 前情提要

- 反射入口是 `Class<T>`
- 通过 Class 可以拿到 Method / Field / Constructor

本篇讲**怎么用这三个**干活。

---

## 🛠 Part 1：Method —— 方法的"句柄"

### 它是什么

**Java 里"一个方法"对应一个 Method 对象**。

类比：
- `User u` = 一个用户的对象
- `Method m` = 一个"**代表某方法**"的对象

### 怎么拿

从 Class 里拿：

```java
Class<User> c = User.class;

// 无参方法
Method m1 = c.getMethod("sayHello");

// 有参方法（必须指定参数类型）
Method m2 = c.getMethod("setName", String.class);
```

### 核心用法：**反射调用方法**

```java
class User {
    public void sayHello(String name) {
        System.out.println("Hello, " + name);
    }
}

// 普通调用
User u = new User();
u.sayHello("张三");

// 反射调用（等价）
Method m = User.class.getMethod("sayHello", String.class);
m.invoke(u, "张三");     // 等于 u.sayHello("张三")
```

### `method.invoke` 的签名

```java
Object invoke(Object obj, Object... args) throws Exception;
//              ↑              ↑
//          在哪个对象上    调用时传的参数
```

**参数讲解**：
- `obj` —— 在**哪个对象**上调用这个方法（如果是静态方法传 null）
- `args` —— 传给方法的参数，可变长度

**返回**：被调用方法的返回值（如果方法返回 void，返回 null）

### 完整示例

```java
import java.lang.reflect.*;

class User {
    public String greet(String name) {
        return "Hello, " + name;
    }
}

public class MethodDemo {
    public static void main(String[] args) throws Exception {
        User u = new User();

        // 1. 拿 Class
        Class<User> c = User.class;

        // 2. 拿 Method
        Method m = c.getMethod("greet", String.class);

        // 3. 反射调用
        Object result = m.invoke(u, "张三");
        System.out.println(result);   // "Hello, 张三"

        // 4. 读元信息
        System.out.println("方法名: " + m.getName());
        System.out.println("返回类型: " + m.getReturnType().getName());
    }
}
```

**Method 是"方法的句柄"** —— 拿着它可以动态调方法，像拿着"函数指针"。

---

## 🧩 Part 2：Field —— 字段的"句柄"

### 它是什么

**"一个字段"对应一个 Field 对象**。

### 怎么拿

```java
Class<User> c = User.class;

Field f = c.getDeclaredField("name");   // 拿到 name 字段
```

⚠️ 用 `getDeclaredField` 而不是 `getField`，因为 `getField` **只能拿 public**。

### 核心用法：**读写字段**

```java
class User {
    private String name = "张三";   // private 字段
}

User u = new User();
Field f = User.class.getDeclaredField("name");

// 🚨 访问 private 字段需要这一行"解封印"
f.setAccessible(true);

// 读字段
String val = (String) f.get(u);    // "张三"

// 写字段
f.set(u, "李四");

System.out.println(f.get(u));       // "李四"
```

### `setAccessible(true)` 的作用

**绕过 Java 访问权限检查**（private / protected / default）。

不加这行，读 private 字段会抛 `IllegalAccessException`。

**这是反射强大但危险的地方** —— 它能破坏封装性。但**框架**（如 Jackson 反序列化）大量使用。

### 完整示例

```java
import java.lang.reflect.*;

class User {
    private String name = "张三";
    private int age = 25;
}

public class FieldDemo {
    public static void main(String[] args) throws Exception {
        User u = new User();

        // 遍历所有字段（包括 private）
        for (Field f : User.class.getDeclaredFields()) {
            f.setAccessible(true);
            System.out.println(f.getName() + " = " + f.get(u));
        }
    }
}
```

**输出**：
```
name = 张三
age = 25
```

---

## 🏗 Part 3：Constructor —— 构造器的"句柄"

### 它是什么

**"一个构造器"对应一个 Constructor 对象**。用来**动态创建对象**。

### 怎么拿

```java
Class<User> c = User.class;

// 无参构造
Constructor<User> ctor1 = c.getDeclaredConstructor();

// 有参构造
Constructor<User> ctor2 = c.getDeclaredConstructor(String.class, int.class);
```

### 核心用法：**创建对象**

```java
class User {
    private String name;
    private int age;

    public User() {}

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

// 方式 1：无参 new
Constructor<User> ctor1 = User.class.getDeclaredConstructor();
User u1 = ctor1.newInstance();

// 方式 2：有参 new
Constructor<User> ctor2 = User.class.getDeclaredConstructor(String.class, int.class);
User u2 = ctor2.newInstance("张三", 25);
```

### `newInstance` 签名

```java
T newInstance(Object... args) throws Exception;
```

**等价于**：
- `new User()` ≈ `User.class.getDeclaredConstructor().newInstance()`
- `new User("张三", 25)` ≈ `ctor.newInstance("张三", 25)`

**这就是 Spring / Jackson 等"从字符串类名动态 new 对象"的底层机制**。

---

## ⚠️ Part 4：反射的代价

反射很强大，**但不是免费的**。

### 1. 性能开销
反射调用**比直接调用慢 10-100 倍**（现代 JVM 的 JIT 优化后差距缩小）。

**热点代码尽量避免反射**。

### 2. 破坏封装
`setAccessible(true)` 能读 private 字段 → 破坏了 Java 的封装原则。

### 3. 编译期检查失效
反射绕过编译期，**错误只有运行时才暴露**：
- 类名写错 → 运行时 `ClassNotFoundException`
- 方法名写错 → 运行时 `NoSuchMethodException`
- 参数类型错 → 运行时 `IllegalArgumentException`

### 4. 可读性差
反射代码**远比直接调用难读**。

### 使用指南
**能不用就不用**。只在**框架代码** / **必要场景**使用：
- 扫描注解
- 动态加载插件
- 序列化 / 反序列化
- ORM 映射

---

## 🧪 综合示例：模拟一个简化版 JSON 反序列化器

```java
import java.lang.reflect.*;
import java.util.Map;

class User {
    private String name;
    private int age;

    @Override
    public String toString() {
        return "User{name=" + name + ", age=" + age + "}";
    }
}

public class MiniJsonDeserializer {
    public static <T> T deserialize(Map<String, Object> data, Class<T> type) throws Exception {
        // 1. 创建对象
        T instance = type.getDeclaredConstructor().newInstance();

        // 2. 遍历字段，反射赋值
        for (Field field : type.getDeclaredFields()) {
            Object value = data.get(field.getName());
            if (value != null) {
                field.setAccessible(true);
                field.set(instance, value);
            }
        }
        return instance;
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> json = Map.of("name", "张三", "age", 25);
        User u = deserialize(json, User.class);
        System.out.println(u);
        // 输出：User{name=张三, age=25}
    }
}
```

**这就是 Jackson / Gson 的最朴素版本**。

---

## 📌 本篇一句话总结

> **Method / Field / Constructor 是"方法 / 字段 / 构造器"的反射对象**。通过它们可以：**调任意方法（`method.invoke`）**、**读写任意字段（`field.get/set`）**、**动态创建对象（`constructor.newInstance`）**。**`setAccessible(true)` 可绕过 private 限制**。

---

## 🔗 下一步

学完反射，就可以进入**动态代理**了：

**下一篇 → [20 · 动态代理（一）· InvocationHandler 设计推演](./20-动态代理一-InvocationHandler设计推演.md)**

我们会"**假设你是 JDK 设计者**"，一步步推出 `InvocationHandler` 的设计。
