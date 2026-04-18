# Java 拷贝全景地图

> 📍 来源：原型课 · 从"`new ArrayList<>(old)` 是不是清空"到"嵌套对象深拷贝"的一系列疑问
> 🎯 回答的核心问题："**Java 里到底有哪些拷贝手段？哪些是语言自带的？哪些是自己写的？复杂嵌套怎么整？**"

---

## 🤔 场景问题

原型模式学习中暴露出的三个连环疑问：

1. **`new ArrayList<>(old)` 不就是清空吗？** → 答：构造器重载，你理解错了
2. **Java 的"拷贝"能力到底有哪些？语言级 vs 代码级怎么区分？**
3. **嵌套的复杂对象要深拷贝，我每层都要写一遍？**

本篇一次讲透。

---

## 🧠 核心结论

> Java 的拷贝能力分**三层**：
>
> **【A】语言/JDK 层面已封装** —— 开箱即用，调 API 即可
> **【B】代码层面自己实现** —— 你写类时自己设计拷贝方式
> **【C】第三方库辅助** —— 借工具偷懒或提升性能
>
> 浅拷贝 vs 深拷贝的核心区别：**引用类型字段是"共享"还是"独立"**。

---

## 🗺 三层能力全景图

```
Java 里的"拷贝"能力
├── 【A】语言/JDK 层面（开箱即用）
│   ├── A1. Object.clone() + Cloneable 接口
│   ├── A2. 集合类的拷贝构造器  ← 最常用 new ArrayList<>(old)
│   ├── A3. List.copyOf / Set.copyOf / Map.copyOf（Java 10+）
│   ├── A4. Arrays.copyOf / System.arraycopy（数组）
│   ├── A5. String / Integer 等不可变类（无需拷贝）
│   └── A6. Record 的结构拷贝（Java 14+）
│
├── 【B】代码层面自己实现
│   ├── B1. Copy constructor（拷贝构造器）
│   ├── B2. 静态工厂方法 static X copyOf(X)
│   ├── B3. Builder.toBuilder() / from()
│   └── B4. 手动逐字段赋值（反模式）
│
└── 【C】第三方库辅助
    ├── C1. Apache Commons SerializationUtils
    ├── C2. Jackson JSON 序列化往返
    ├── C3. Kryo（高性能序列化）
    ├── C4. Spring BeanUtils
    ├── C5. Lombok @With / @Builder(toBuilder=true)
    └── C6. MapStruct（对象映射器）
```

---

# 【A】语言/JDK 层面封装的拷贝能力

这些是你不用写代码就能用的，**直接调 API** 的拷贝手段。

## A1. `Object.clone()` + `Cloneable` 接口

这是 Java 从 1.0 就有的"原始"拷贝机制。

```java
class Book implements Cloneable {          // 必须实现标记接口
    String title;
    List<String> tags;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();               // 调用 JDK 原生的 clone
    }
}

Book a = new Book();
Book b = (Book) a.clone();                  // 浅拷贝
```

### 特点
- ✅ JDK 原生支持
- ❌ **默认浅拷贝**（引用类型字段共享）
- ❌ 设计丑陋：`Cloneable` 是标记接口但 `clone` 在 `Object` 上
- ❌ 受检异常 `CloneNotSupportedException` 烦人
- ❌ 不走构造器 → `final` 字段处理困难

**Joshua Bloch 在《Effective Java》里明确说**："新代码别实现 Cloneable"。

## A2. 集合类的拷贝构造器（最常用！）

**每个 JDK 集合都有一个"接收同类对象的构造器"**，内容直接从参数拷贝：

```java
List<String> old = List.of("a", "b", "c");

// 各种集合的拷贝构造器
List<String> list   = new ArrayList<>(old);         // ArrayList 拷贝
List<String> linked = new LinkedList<>(old);        // LinkedList 拷贝
Set<String>  set    = new HashSet<>(old);           // HashSet 拷贝
Set<String>  tree   = new TreeSet<>(old);           // TreeSet 拷贝
```

### 为什么这不是"清空"？

**构造器重载！`ArrayList` 有多个构造器**：

```java
public class ArrayList<E> {
    public ArrayList() { ... }                      // 空
    public ArrayList(int capacity) { ... }          // 空 + 预分配容量
    public ArrayList(Collection<? extends E> c) { // 👈 拷贝构造器
        // 内部把 c 里所有元素复制到新数组
    }
}
```

**参数不同 → 调用不同构造器 → 结果完全不同**。

```java
List<String> empty = new ArrayList<>();            // 空 List
List<String> copy  = new ArrayList<>(old);         // 拷贝 old 的所有元素
```

**这是最常用的 Java 拷贝方式**，几乎所有代码里的 List 拷贝都用它。

### Map 的拷贝构造器

```java
Map<String, Integer> old = Map.of("a", 1, "b", 2);
Map<String, Integer> copy = new HashMap<>(old);    // 拷贝所有键值对
```

## A3. `List.copyOf` / `Set.copyOf` / `Map.copyOf`（Java 10+）

**创建不可变副本**：

```java
List<String> immutableCopy = List.copyOf(old);
immutableCopy.add("x");   // ❌ UnsupportedOperationException
```

### 和 A2 的区别

| 方式 | 结果 |
|------|------|
| `new ArrayList<>(old)` | 可变的拷贝 |
| `List.copyOf(old)` | **不可变**的拷贝 |

**用途**：
- 向外暴露集合时用 `copyOf` 保护内部数据
- 创建真正不可变的对象

## A4. `Arrays.copyOf` / `System.arraycopy`（数组专用）

```java
int[] old = {1, 2, 3, 4};

int[] copy = Arrays.copyOf(old, old.length);       // 完整拷贝
int[] grow = Arrays.copyOf(old, 10);               // 拷贝 + 扩容到 10

// 性能最高：System.arraycopy（JVM 原生）
int[] dst = new int[4];
System.arraycopy(old, 0, dst, 0, 4);
```

### 二维数组的坑

```java
int[][] old = {{1, 2}, {3, 4}};

int[][] copy = Arrays.copyOf(old, old.length);     // 🚨 浅拷贝！
copy[0][0] = 999;
System.out.println(old[0][0]);   // 999 —— 原数组也变了！

// 正确的深拷贝：
int[][] deep = new int[old.length][];
for (int i = 0; i < old.length; i++) {
    deep[i] = Arrays.copyOf(old[i], old[i].length);
}
```

## A5. String / Integer 等不可变类 —— 不用拷贝

```java
String a = "hello";
String b = a;          // 直接赋引用 —— 永远安全
```

**因为 String 不可变**，多变量共享一个 String 对象完全安全。**不可变对象不需要拷贝**。

Java 的不可变类：
- `String`
- 所有包装类：`Integer`, `Long`, `Double`, `Boolean`, `Character` 等
- `LocalDate`, `LocalDateTime`, `Duration`, `Instant`（Java 8+ 时间类）
- `UUID`
- `BigInteger`, `BigDecimal`
- `Optional`

## A6. Record 的结构拷贝（Java 14+）

```java
record User(String name, int age, List<String> tags) {}

User old = new User("Alice", 30, List.of("a", "b"));

// "修改"一个字段 = 构造新 Record，其余字段引用共享
User modified = new User(old.name(), 31, old.tags());
//                       ↑            ↑    ↑
//                  共享 String   新值  共享 List
```

Record 本身不可变，所以**引用共享天然安全**。这是 Java 向**不可变设计**靠近的一步。

---

# 【B】代码层面自己实现

你写业务类时自己设计的拷贝方式。

## B1. Copy constructor（拷贝构造器）

**最经典、最推荐**的自实现方式：

```java
class Resume {
    private String name;
    private List<String> skills;

    // 普通构造器
    public Resume(String name, List<String> skills) {
        this.name = name;
        this.skills = new ArrayList<>(skills);
    }

    // 👇 拷贝构造器
    public Resume(Resume other) {
        this.name = other.name;
        this.skills = new ArrayList<>(other.skills);   // 用 A2 的手段
    }
}

// 使用：
Resume copy = new Resume(original);
```

**优点**：
- ✅ 走正常构造器 → `final` 字段也能处理
- ✅ 代码清晰
- ✅ 可以和 Builder 配合
- ✅ 无 `Cloneable` 的坑

**Effective Java 推荐**：优先于 `Cloneable`。

## B2. 静态工厂方法

```java
public static Resume copyOf(Resume other) {
    return new Resume(other.name, new ArrayList<>(other.skills));
}

// 使用：
Resume copy = Resume.copyOf(original);
```

比拷贝构造器多一层命名语义（`copyOf` 比 `new Resume(resume)` 更直白）。

## B3. Builder 的 `toBuilder()` 方法

```java
Resume modified = original.toBuilder()
    .skill("Kubernetes")
    .build();
```

**Lombok `@Builder(toBuilder = true)` 自动生成**此方法。

## B4. 手动逐字段赋值（反模式）

```java
Resume copy = new Resume();
copy.name = original.name;
copy.skills = new ArrayList<>(original.skills);
copy.email = original.email;
// ... 一个个抄
```

字段多就是地狱。**能避免就避免**。

---

# 【C】第三方库辅助

## C1. Apache Commons `SerializationUtils.clone()`

```java
import org.apache.commons.lang3.SerializationUtils;

Company copy = SerializationUtils.clone(original);    // 一行搞定深拷贝
```

**原理**：对象序列化成字节流 → 反序列化出来。自动递归深拷贝。

**要求**：所有相关类都 `implements Serializable`。

## C2. Jackson JSON 往返

```java
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(original);
Company copy = mapper.readValue(json, Company.class);
```

**优点**：不要求 Serializable，要求可 JSON 化（getter/构造器）。**业界最常用**。

## C3. Kryo（高性能序列化库）

```java
Kryo kryo = new Kryo();
kryo.register(Company.class);
Company copy = kryo.copy(original);
```

性能**比 Java 原生序列化快 10 倍**。游戏服务器、缓存库（Redisson）常用。

## C4. Spring `BeanUtils.copyProperties`

```java
Resume copy = new Resume();
BeanUtils.copyProperties(original, copy);   // 按 getter/setter 复制
```

**浅拷贝**。Spring 项目常用。

## C5. Lombok

```java
@Value
@With                         // 生成 withXxx 方法
class Resume {
    String name;
    List<String> skills;
}

// 自动得到：
Resume copy = original.withName("李四");         // 改 name
Resume copy2 = original.withSkills(newList);    // 改 skills
```

## C6. MapStruct / ModelMapper

主要用于 DTO 映射，也能做深拷贝。Spring Boot 项目常用。

---

# 🌊 浅拷贝 vs 深拷贝：关键认知

## 图解

### 浅拷贝
```
【原版】                    【克隆】
┌──────────┐              ┌──────────┐
│ name: a  │              │ name: a  │
│ list ----┼─────┐   ┌────┼ list     │
└──────────┘     │   │    └──────────┘
                 ↓   ↓
              ┌─────────┐
              │ List    │  ← 共享同一个 List！一方改另一方也变
              │ [x, y]  │
              └─────────┘
```

### 深拷贝
```
【原版】                    【克隆】
┌──────────┐              ┌──────────┐
│ name: a  │              │ name: a  │
│ list ----┼──┐           │ list ----┼──┐
└──────────┘  │           └──────────┘  │
              ↓                         ↓
           ┌───────┐                ┌───────┐
           │ List1 │                │ List2 │   ← 独立的两个 List
           │ [x,y] │                │ [x,y] │
           └───────┘                └───────┘
```

## 判断规则

| 字段类型 | 浅拷贝处理 | 深拷贝处理 |
|---------|----------|----------|
| 基本类型（int/boolean）| 复制值 | 复制值 |
| String 等**不可变**引用 | 复制引用（安全）| 同浅拷贝 |
| 可变对象（List/Map/自定义类）| 复制引用（共享 🚨）| 递归克隆 |

---

# 🪆 嵌套复杂对象的深拷贝实战

```java
class Company {
    String name;
    Address address;
    List<Employee> employees;
}

class Employee {
    String name;
    Department dept;
    List<Project> projects;
}

class Project {
    String name;
    List<Task> tasks;
}

class Task {
    String title;
    User assignee;
    List<Comment> comments;
}
// ...
```

4 种方案对比：

## 方案 1：手写递归 Copy constructor

```java
class Company {
    public Company(Company other) {
        this.name = other.name;
        this.address = new Address(other.address);
        this.employees = new ArrayList<>();
        for (Employee e : other.employees) {
            this.employees.add(new Employee(e));         // 调下一层的拷贝构造器
        }
    }
}

class Employee {
    public Employee(Employee other) {
        // ... 同样的递归
    }
}
// 每一层都要写
```

| 优点 | 缺点 |
|------|------|
| 最精确 / 性能最好 | 代码爆炸 / 循环引用会栈溢出 / 维护成本高 |

## 方案 2：序列化往返

```java
Company copy = SerializationUtils.clone(original);
// 或：
String json = mapper.writeValueAsString(original);
Company copy = mapper.readValue(json, Company.class);
```

| 优点 | 缺点 |
|------|------|
| 1 行 / 深度任意 | 慢（10-50 倍）/ 要求可序列化 / transient 字段丢失 |

## 方案 3：不可变 + 结构共享（函数式思路）

```java
record Company(String name, Address address, List<Employee> employees) {}
record Employee(String name, Department dept, List<Project> projects) {}
// ... 全 record

// "修改"一处：共享不变部分
Company modified = new Company(old.name(), newAddress, old.employees());
//                                          ↑           ↑
//                                       新建         共享引用（不可变所以安全）
```

| 优点 | 缺点 |
|------|------|
| 最优雅 / 线程安全 / 性能好 | 需要前期设计成不可变 |

## 方案 4：Kryo 等高性能库

```java
Company copy = kryo.copy(original);
```

性能和手写接近，代码一行。是**真实工程里深拷贝的主流选择**。

---

# 🕳 循环引用陷阱

```java
class User {
    String name;
    List<User> friends;
}

User alice = new User("Alice");
User bob = new User("Bob");
alice.friends.add(bob);
bob.friends.add(alice);    // 🔁 循环引用
```

| 方案 | 处理 |
|------|------|
| 手写递归 | ❌ 栈溢出 |
| Java 原生序列化 | ✅ 自动处理 |
| Jackson | ❌ 默认报错，加 `@JsonIdentityInfo` 可处理 |
| Record | 🚫 根本构造不出（编译不过）|
| Kryo | ✅ 自动处理 |

**设计建议**：**避免循环引用**，用 ID 查找表代替直接引用。

---

# 🎯 选型决策树

```
需要拷贝？
  ├── 对象是不可变的（如 String）？
  │     └── 直接赋值，不用拷贝
  │
  ├── 是基本类型数组？
  │     └── Arrays.copyOf
  │
  ├── 是 JDK 集合（List/Map/Set）？
  │     ├── 需要可变副本？ new ArrayList<>(old)
  │     └── 需要不可变？    List.copyOf(old)
  │
  ├── 是自定义的业务对象？
  │     ├── 简单（1-5 字段）？ → Copy constructor（自己写）
  │     ├── 中等复杂（多层嵌套）？ → Jackson / SerializationUtils
  │     └── 高频调用/性能敏感？ → Kryo 或手写递归
  │
  └── 新项目？
        └── 考虑设计成不可变（Record + List.copyOf），根本不用深拷贝
```

---

# ⚡ 性能对比（粗略）

| 方式 | 相对性能 | 备注 |
|------|--------|------|
| 手写递归 copy constructor | 100（基准）| 最快 |
| `new ArrayList<>(old)` | ~100 | 极快 |
| `Object.clone()` | ~100 | 快但有坑 |
| Kryo | ~70-90 | 接近手写 |
| Jackson JSON 往返 | ~20-30 | 慢但省心 |
| Java 原生序列化 | ~10-20 | 最慢 |

实际数字差异大，取决于对象复杂度。**冷路径不用在意性能**。

---

# 💼 真实项目最佳实践

### 原则 1：**优先不可变设计**
用 Record、`List.copyOf`、`final` 字段 → 根本不需要深拷贝。

### 原则 2：**DTO 分层**
别直接传递领域对象（Domain Entity），用**扁平的 DTO**。DTO 无嵌套 → 浅拷贝就够。

### 原则 3：**热路径手写 / 冷路径序列化**
- 业务核心每秒千次调用 → 手写 / Kryo
- 偶尔跑的场景（定时任务、测试）→ Jackson / SerializationUtils

### 原则 4：**避免循环引用**
一旦你发现对象有循环引用，通常意味着建模有问题。

### 原则 5：**"手写深拷贝"是 code smell**
如果你经常在写"深拷贝 N 层对象"的代码，停下来想想：
- 是不是应该改成**不可变**？
- 是不是应该**用 DTO 分层**？
- 是不是**值对象（Value Object）** 设计缺失？

**避免深拷贝问题的最好方式是：让对象根本不需要被深拷贝**。

---

# ⚠️ 常见误区（精华）

### 误区 1：`new ArrayList<>(old)` 会清空
**不是**。这是拷贝构造器，**内容从 old 拷贝过来**。和无参 `new ArrayList<>()` 完全不同。

### 误区 2：`clone()` 自动深拷贝
**默认是浅拷贝**。引用类型字段共享。要深拷贝必须手动 override。

### 误区 3：加了 `final` 就不用担心拷贝
**不是**。`final` 只保护引用不能重新赋值，不保护对象内部状态。`final List` 的内容照样可以被修改。

### 误区 4：`Serializable` 接口自动就能深拷贝
**不是**。只是"可序列化"的标签，要深拷贝还得 `SerializationUtils.clone` 或手动序列化。

### 误区 5：`System.arraycopy` 是深拷贝
**不是**。它只复制引用（对引用类型数组来说）。基本类型数组则是真正的拷贝。

### 误区 6：浅拷贝比深拷贝快一点点
**差异巨大**。浅拷贝只复制字段，深拷贝要递归整个对象图。大对象图深拷贝可能慢几十倍。

---

# 📚 相关笔记串联

- [02 静态字段 vs 实例字段](./02-静态字段vs实例字段.md) —— 理解拷贝的"字段"边界
- [08 标记接口 & Serializable](./08-标记接口与Serializable.md) —— Cloneable 的设计同源
- [09 final 关键字全解](./09-final关键字全解.md) —— 不可变的基石
- [11 集合框架 & 泛型](./11-集合框架与泛型.md) —— A2/A3 的细节

---

# 📌 一句话总结

> **Java 拷贝能力分三层：语言封装（A1-A6，直接用）、代码实现（B1-B4，自己写）、工具库（C1-C6，借工具）**。**核心区别是浅拷贝（共享引用）vs 深拷贝（独立内容）**。嵌套复杂对象用**序列化往返 / Kryo** 最省事；新项目最佳方案是**不可变设计 + Record**，从根子上消除深拷贝问题。

---

# 🔗 相关深入

- **持久化数据结构（Persistent Data Structures）** —— Clojure、Scala 的核心思想
- **Immer.js** —— JavaScript 的不可变更新库
- **Project Valhalla** —— Java 未来的 value types
- **Flyweight 模式**（后面会学）—— 拷贝的反面：如何让对象"共享"
