# 标记接口 & Java 序列化

> 📍 来源：建造者课 · JavaBean 约定里的 `implements Serializable`
> 🎯 回答的核心问题："**`Serializable` 是空接口，实现它不用写方法？这也太怪了吧？**"

---

## 🤔 场景问题

在 JavaBean 的典型例子里：

```java
public class User implements Serializable {
    private String name;

    public User() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

疑问：**`implements Serializable` 但没实现任何方法，为什么编译器不报错？**

---

## 🧠 核心结论

> **`Serializable` 是"标记接口"（Marker Interface）—— 一个空接口，只起"打标签"的作用，不约束行为。**

实现它等于告诉 JVM：**"我这个类同意被序列化"**。具体怎么序列化的活由 JVM 干（反射读字段 + 写流），你不用自己写。

---

## 📋 什么是标记接口

**定义**：没有任何抽象方法的接口，纯粹用来"标记"一个类具备某种特性。

**工作机制**：框架/JVM 通过 `instanceof XxxMarker` 判断，对打了标签的类做特殊处理。

### Serializable 源码真相

```java
package java.io;

public interface Serializable {
    // 空接口，什么都没有
}
```

**真的什么都没有**。所以 `implements Serializable` 不需要实现任何方法。

---

## 🎯 Java 三大经典标记接口

| 接口 | 标记含义 | 谁在用它 |
|------|---------|---------|
| `java.io.Serializable` | 可序列化为字节流 | `ObjectOutputStream.writeObject()` 会检查 |
| `java.lang.Cloneable` | 可通过 `Object.clone()` 克隆 | `Object.clone()` 会检查 |
| `java.util.RandomAccess` | List 支持 O(1) 随机访问 | `Collections.binarySearch()` 会选不同算法 |

### RandomAccess 的用法示例

```java
List<Integer> arrayList = new ArrayList<>();         // implements RandomAccess
List<Integer> linkedList = new LinkedList<>();       // 不实现 RandomAccess

if (list instanceof RandomAccess) {
    // for 下标循环更快
    for (int i = 0; i < list.size(); i++) list.get(i);
} else {
    // 用迭代器更合适
    for (Integer x : list) ...
}
```

JDK 内部的 `Collections.sort()` 就这么做的 —— **根据标记接口选算法**。

---

## 🔧 Java 序列化原理

当你调 `ObjectOutputStream.writeObject(user)`，JVM 内部大致做：

```java
public void writeObject(Object obj) {
    // ① 检查标签
    if (!(obj instanceof Serializable)) {
        throw new NotSerializableException();
    }

    // ② 用反射读取所有非 transient 字段
    Field[] fields = obj.getClass().getDeclaredFields();
    for (Field f : fields) {
        if (!Modifier.isTransient(f.getModifiers())) {
            writeField(f.get(obj));              // 写入流
        }
    }
}
```

**序列化逻辑 JVM 全包了**（反射读字段 + 写入流），你不用自己写。

---

## 🎁 Serializable 的"可选"特殊方法

虽然不强制，但这几个方法 JVM 会识别并在序列化/反序列化时调用：

### 1. `serialVersionUID` —— 版本号（强烈推荐）

```java
public class User implements Serializable {
    private static final long serialVersionUID = 1L;    // 👈 关键
    private String name;
}
```

**作用**：序列化时存入版本号。反序列化时检查对比，不一致抛 `InvalidClassException`。

**不写的后果**：JVM 会根据类结构自动生成一个。类稍微改一下（加字段、改方法），UID 就变 → 之前序列化的数据**反序列化失败**。

**建议**：**所有 Serializable 类都加 `serialVersionUID`**，哪怕写 `1L`。

### 2. `transient` 关键字 —— 跳过字段

```java
public class User implements Serializable {
    private String name;
    private transient String password;    // 👈 不参与序列化
    private transient String cache;       // 👈 临时数据，不需要存
}
```

序列化时 `password` 和 `cache` 字段会被**跳过**。反序列化后这两个字段是 null（或基本类型默认值）。

**典型用途**：敏感数据（密码）、临时缓存、无法序列化的字段（如 Thread）。

### 3. `writeObject` / `readObject` —— 自定义读写

```java
public class User implements Serializable {
    private String name;
    private transient String password;

    // 自定义写入：密码加密后再写
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(encrypt(password));
    }

    // 自定义读取：读出来后解密
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        password = decrypt(in.readUTF());
    }
}
```

**注意**：方法必须是 `private`，签名必须完全匹配上面的样子，否则 JVM 不识别。

### 4. `readResolve` —— 反序列化时"换对象"

```java
public class Singleton implements Serializable {
    public static final Singleton INSTANCE = new Singleton();
    private Singleton() {}

    // 反序列化时不 new 新对象，返回已存在的 INSTANCE
    private Object readResolve() {
        return INSTANCE;
    }
}
```

**用途**：**防止单例被反序列化破坏**（每次反序列化默认会 new 新对象）。这是手写单例最容易忘记的一步，枚举单例天生不需要这一步。

---

## 🌍 现代 Java 其实在抛弃 Serializable

几个原因：

### 1. **JSON 更流行**
Jackson / Gson 跨语言、人类可读、无 Java 依赖。微服务、REST API 几乎只用 JSON。

### 2. **严重的安全漏洞史**
Java 原生序列化爆发过多起 RCE（远程代码执行）漏洞：
- 2015 年 Apache Commons Collections 的 `InvokerTransformer` 链
- 2017 年 Apache Struts2 的 S2-055 漏洞
- ……

原因：反序列化过程中会**自动调用类的各种方法**，恶意字节流可以触发任意代码执行。

### 3. **性能差**
相比 Protobuf / MessagePack / JSON，Java 原生序列化**又慢又大**。

### 4. **JEP 154（2014）已经开始"准备废弃"**
Oracle 多次表态希望将来移除 Java 原生序列化。Java 17+ 已经有大量相关警告。

---

## ✅ 现代 JavaBean 什么时候仍然要 Serializable

不是绝对不用，仍有少量合法场景：

| 场景 | 原因 |
|------|------|
| `HttpSession` 存入会话对象（Tomcat / Jetty） | 容器跨重启持久化用 |
| **Spring Session + Redis** | Session 存到 Redis，Redis 反序列化要用（但可切 JSON） |
| RMI 远程调用 | Java 原生 RMI 协议依赖它 |
| `HashMap` / `ArrayList` 等 JDK 集合 | 实现了 Serializable 是历史遗留 |

**新项目**：能不用就不用，用 JSON / Protobuf。

---

## 🧭 标记接口 vs 注解 —— 演化趋势

现代 Java 倾向用**注解**代替标记接口：

| 老派：标记接口 | 新派：注解 |
|---------------|-----------|
| `implements Serializable` | 没替代品（但用 JSON 库加 `@JsonProperty` 等） |
| `implements Cloneable` | 改用 copy constructor / `record` 自动 copy |
| `@Override`（本来就是注解） | — |
| `@FunctionalInterface` | — |

**注解更灵活**（可以带参数），**标记接口更传统**。新写库都用注解了。

---

## ⚠️ 常见误区

### 误区 1："Serializable 必须有方法要实现"
**不是**。空接口，纯标签。

### 误区 2："implements Serializable 自动能序列化任意字段"
**不是**。**非 transient 字段**必须都可序列化（字段类型也要 Serializable），不然抛 `NotSerializableException`。

### 误区 3："serialVersionUID 随便写不重要"
**不是**。不一致会反序列化失败。每次兼容修改后**可以保持不变**，每次不兼容修改**应该更新**。

### 误区 4："static 字段会被序列化"
**不会**。static 字段属于类不属于对象，序列化只处理实例字段。

### 误区 5："枚举单例需要实现 readResolve"
**不用**。JVM 对枚举反序列化有特殊处理，不走常规流程，自动保单例。

---

## 🎁 一个完整的 Serializable 示例

```java
import java.io.*;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
    private transient String password;       // 不参与序列化

    public User(String name, int age, String password) {
        this.name = name;
        this.age = age;
        this.password = password;
    }

    public static void main(String[] args) throws Exception {
        User u1 = new User("张三", 25, "secret");

        // 序列化到文件
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("user.dat"))) {
            out.writeObject(u1);
        }

        // 反序列化回来
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("user.dat"))) {
            User u2 = (User) in.readObject();
            System.out.println(u2.name + ", " + u2.age + ", " + u2.password);
            // 输出：张三, 25, null   ← password 是 transient，恢复为 null
        }
    }
}
```

---

## 💡 一句话记忆

> **`Serializable` 是空标记接口，只起"打标签"作用，序列化工作 JVM 全包。但现代 Java 更偏爱 JSON，Serializable 有安全和性能隐患，新项目能不用就不用。**

---

## 🔗 相关深入

- **JSON 序列化（Jackson / Gson）** —— 现代标准做法
- **Protobuf / Thrift** —— 高性能跨语言序列化
- **反射 + 注解** —— 现代框架的主流做法（Spring、JPA、Jackson 都这么搞）
- **`ObjectInputStream` 的安全漏洞** —— 为什么不该反序列化不可信数据
