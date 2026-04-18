# equals 与 hashCode · Java 最重要的契约一对

> 📍 来源：原型/Record 讨论中引出的"对象相等性"核心问题
> 🎯 回答的核心问题："**什么时候对象相等？为什么 equals 和 hashCode 必须成对重写？**"

---

## 🤔 场景问题

1. `new User("张三")` 和 `new User("张三")` 是"相等"的吗？
2. 为什么 `HashMap.get(new User("张三"))` 可能取不到之前 put 进去的数据？
3. 为什么 Java 老师总说"重写 equals 必须同时重写 hashCode"？
4. 数组做 HashMap 的 key 为什么是大坑？

---

## 🧠 核心结论

> Java 里两个对象的"相等"有两个层次：
>
> - **`==`** 比较**引用**（是不是同一个对象）
> - **`equals()`** 比较**语义上的相等**（业务上是不是一回事）
>
> **`equals` 和 `hashCode` 必须同时重写**，否则 HashMap / HashSet 会出 bug。

---

## 🔍 两种"相等"的区别

```java
String a = new String("张三");
String b = new String("张三");

a == b          // false（两个不同对象）
a.equals(b)     // true（内容相同）
```

| 比较方式 | 比较什么 | 谁决定 |
|---------|---------|--------|
| `==` | 两个引用是否指向**同一个对象** | JVM（不可改）|
| `equals()` | **业务语义**上是否相等 | 类的作者（可重写）|

**关键**：基本类型 `==` 比较**值**（`1 == 1` true），引用类型 `==` 比较**地址**。

---

## 📋 Object 的默认实现

`Object.equals()` 的默认实现：

```java
public boolean equals(Object obj) {
    return this == obj;    // 就是 ==
}
```

**默认情况下，`equals` 和 `==` 等价**。所以你写的类如果不重写 equals，`equals` 就是比较地址。

`Object.hashCode()` 的默认实现是 **JVM 内部的 native 方法**，通常和对象身份（内存地址）相关。

---

## 📝 equals 和 hashCode 的契约（铁律）

这是 Java 的**硬性规则**，来自 `Object.java` 的文档：

### 规则 1：**如果 `a.equals(b)` 返回 true，那么 `a.hashCode()` 必须等于 `b.hashCode()`**

违反 → HashMap / HashSet 失灵。

### 规则 2：**`a.equals(b)` 和 `b.equals(a)` 必须结果一致**（对称性）

### 规则 3：**`a.equals(b)` && `b.equals(c)` → `a.equals(c)`**（传递性）

### 规则 4：**`a.equals(b)` 多次调用结果一致**（一致性）

### 规则 5：**`a.equals(null)` 必须返回 false**

### 规则 6：**`a.equals(a)` 必须返回 true**（自反性）

### 规则 7（hashCode 部分）：**对象没变，hashCode 不能变**

---

## 🧪 不同类型的 equals / hashCode 行为

### 1. 基本类型：没有 equals / hashCode（不是对象）

```java
int a = 1;
int b = 1;
a == b;          // true（直接比值）
a.equals(b);     // ❌ 编译错误：基本类型没方法
```

要用的话**自动装箱成包装类**：
```java
Integer a = 1;
Integer b = 1;
a.equals(b);     // true
a.hashCode();    // 1
```

### 2. 包装类：`equals` 按值 / `hashCode` 返回值本身

```java
Integer a = 1000;
Integer b = 1000;
a == b;          // false（对象不同！Integer 缓存 -128~127，超过就 new 新对象）
a.equals(b);     // true（按值比）
a.hashCode();    // 1000
b.hashCode();    // 1000

Integer x = 100;
Integer y = 100;
x == y;          // true（在缓存范围，同一个对象）
// 所以：包装类的 == 是个陷阱，永远用 equals 比值
```

### 3. String：`equals` 按字符内容 / `hashCode` 算法固定

```java
String a = new String("hello");
String b = new String("hello");
a == b;          // false（不同对象）
a.equals(b);     // true（内容相同）
a.hashCode();    // 99162322（基于字符的算法）
b.hashCode();    // 99162322（同上）
```

String 的 hashCode 源码：
```java
public int hashCode() {
    int h = 0;
    for (char c : value) {
        h = 31 * h + c;    // 乘 31 累加
    }
    return h;
}
```

### 4. 数组：**equals 和 hashCode 都没重写（坑！）**

```java
int[] a = {1, 3};
int[] b = {1, 3};
a == b;               // false
a.equals(b);          // **false** ❌ 按对象身份比
a.hashCode();         // 例如 1846274136（基于身份）
b.hashCode();         // 例如 1639705018（不同）

// 正确方式：用 Arrays 工具类
Arrays.equals(a, b);         // true ✅
Arrays.hashCode(a);          // 994
Arrays.hashCode(b);          // 994
```

**这就是"数组不能直接当 HashMap 的 key"的根源**。

### 5. List / Set / Map：按元素/键值内容

```java
List<Integer> a = List.of(1, 3);
List<Integer> b = List.of(1, 3);
a.equals(b);         // true
a.hashCode();        // 994
b.hashCode();        // 994
```

List 的 hashCode 规范（JDK 源码注释）：
```java
int hashCode = 1;
for (E e : list)
    hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
```

### 6. 自定义类（没重写）：按对象身份

```java
class User {
    String name;
    User(String name) { this.name = name; }
}

User a = new User("张三");
User b = new User("张三");
a == b;              // false
a.equals(b);         // false ❌（继承 Object 的默认实现）
a.hashCode();        // 随机数字（基于身份）
b.hashCode();        // 不同的随机数字
```

### 7. Record：自动生成，按所有字段

```java
record User(String name, int age) {}

User a = new User("张三", 30);
User b = new User("张三", 30);
a.equals(b);         // true ✅
a.hashCode() == b.hashCode();   // true ✅
```

**Record 自动按所有字段实现 equals / hashCode，无法写错**。

---

## 🔧 如何正确重写 equals 和 hashCode

### 标准模板

```java
class User {
    private String name;
    private int age;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                      // ① 同一个对象
        if (!(o instanceof User u)) return false;        // ② 类型检查 + 强转
        return age == u.age && Objects.equals(name, u.name);   // ③ 按字段比
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);                  // 一行搞定
    }
}
```

### 模板解析

**equals 四步走**：
1. 自反性：`this == o` 是同一个对象 → 直接 true
2. 类型检查：确保 `o` 是 User 类型（用 `instanceof` 更简洁，附带强转）
3. null 检查：`instanceof` 遇到 null 返回 false（省得你写）
4. 按字段比较：`Objects.equals(name, u.name)` 比 `name.equals(u.name)` 更安全（防 null）

**hashCode 一步**：
1. `Objects.hash(...)` 把所有 equals 参与比较的字段传进去

### `Objects.equals` vs 直接调 equals

```java
// 老写法（可能 NPE）
name.equals(u.name)             // 如果 name 是 null → NullPointerException

// 新写法（null 安全）
Objects.equals(name, u.name)    // 任一为 null 也不抛
```

`Objects.equals` 源码：
```java
public static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
}
```

---

## 💥 经典陷阱

### 陷阱 1：**重写 equals 忘了 hashCode**（最常见 bug）

```java
class User {
    String name;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User u)) return false;
        return name.equals(u.name);
    }
    // 🚨 忘了重写 hashCode
}

Map<User, String> map = new HashMap<>();
map.put(new User("张三"), "北京");
map.get(new User("张三"));   // null ❌
```

**原因**：HashMap 先用 hashCode 找桶，再用 equals 比较。两个 `new User("张三")` 的 hashCode 用的是 Object 默认实现（基于身份）→ 不同 → 去不同桶找 → 找不到。

**铁律**：**重写 equals 一定同时重写 hashCode**。

### 陷阱 2：**数组做 HashMap key**

```java
Map<int[], String> map = new HashMap<>();
map.put(new int[]{1, 3}, "A");
map.get(new int[]{1, 3});    // null ❌
```

**原因**：数组没重写 equals / hashCode，都是按身份。

**解决**：用 `List<Integer>` 或 Record 代替数组做 key。

### 陷阱 3：**可变对象做 HashMap key**

```java
class MutableKey {
    String name;
    MutableKey(String name) { this.name = name; }
    @Override public int hashCode() { return name.hashCode(); }
    @Override public boolean equals(Object o) { /* 按 name */ }
}

MutableKey k = new MutableKey("张三");
Map<MutableKey, String> map = new HashMap<>();
map.put(k, "北京");

k.name = "李四";             // 🚨 改了 key 的内部状态
map.get(k);                 // null ❌（hashCode 变了，找不到原桶）
```

**解决**：**HashMap 的 key 应该不可变**。用 String / Record 做 key。

### 陷阱 4：**继承中的 equals 破坏对称性**

```java
class Point {
    int x, y;
    @Override public boolean equals(Object o) {
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }
}

class ColorPoint extends Point {
    String color;
    @Override public boolean equals(Object o) {
        if (!(o instanceof ColorPoint cp)) return false;
        return super.equals(o) && color.equals(cp.color);
    }
}

Point p = new Point(1, 2);
ColorPoint cp = new ColorPoint(1, 2, "red");

p.equals(cp);    // true（Point 只比 x/y）
cp.equals(p);    // false（ColorPoint 要比 color）
// 🚨 违反对称性！
```

**解决**：
- Joshua Bloch 建议**使用组合而非继承**
- 或者用 `getClass() != o.getClass()` 代替 instanceof（更严格）

### 陷阱 5：**Lombok @EqualsAndHashCode 只用部分字段**

```java
@EqualsAndHashCode(of = {"id"})     // 只按 id
class User {
    Long id;
    String name;
}

// 两个 id 相同但 name 不同的 User 会被认为"相等"
// 可能不是你想要的！
```

**注意**：Lombok 的注解参数要仔细看。

---

## 🎁 实战建议

### 1. **能用 Record 就用 Record**

```java
record User(Long id, String name, int age) {}
// ✅ equals / hashCode 自动生成，不可能写错
```

### 2. **老项目用 Lombok**

```java
@Data            // 自动生成 equals / hashCode / toString / getter / setter
public class User {
    private Long id;
    private String name;
}
```

### 3. **手写时用 IDE 或 Objects 工具**

IntelliJ / VSCode → 右键 → Generate → "equals() and hashCode()" → 一键生成，保证正确。

### 4. **永远不要用数组做 HashMap 的 key**

换成 `List<>` 或 Record。

### 5. **HashMap 的 key 选不可变类型**

```java
// ✅ 推荐
Map<String, ?> / Map<Integer, ?> / Map<Long, ?> / Map<UUID, ?> / Map<Record, ?>

// ❌ 禁止
Map<int[], ?> / Map<Object[], ?> / Map<MutableClass, ?>
```

---

## 📐 equals / hashCode 决策树

```
你要比较两个对象是否"相等"？
  │
  ├── 是"同一个对象"吗？   → 用 ==
  │
  └── 是"业务上相等"吗？   → 用 equals
          │
          └── 你的类重写 equals 了吗？
                │
                ├── 重写了 → 按你的逻辑比较
                └── 没重写 → 和 == 一样（比身份）

要把对象放进 HashMap / HashSet？
  │
  ├── 类是 Record / JDK 不可变类 → ✅ 放心用
  ├── 类是你自己写的 → 一定要重写 equals + hashCode
  ├── 类是数组 → ❌ 不能用，换成 List 或 Record
  └── 类是可变的 → ❌ 别用，找个不可变替代
```

---

## ⚠️ 常见误区

### 误区 1：`equals` 就是 `==`
**不对**。`==` 比引用，`equals` 按类的实现（默认等价于 ==，重写后按内容）。

### 误区 2：`hashCode` 唯一标识对象
**不对**。不同对象可以有相同 hashCode（叫**哈希冲突**）。

### 误区 3：重写 equals 不用重写 hashCode
**错**。**必须同时重写**。

### 误区 4：`a.hashCode() == b.hashCode()` → `a.equals(b)`
**错**。反向不成立。hashCode 相等只是**必要条件**不是**充分条件**。

### 误区 5：数组重写了 equals
**没有**。数组的 `equals` 和 `hashCode` 都用 Object 默认实现。用 `Arrays.equals` / `Arrays.hashCode`。

### 误区 6：`equals` 的参数用 `equals(User u)`
**错**。应该是 `equals(Object o)`（要覆盖 Object 的方法签名）。用 `User u` 只是**重载**不是**重写**，HashMap 不会调用。

---

## 🔗 相关笔记

- [07 Override vs Overload](./07-Override-vs-Overload.md) —— 为什么 `equals(User)` 只是重载
- [10 Objects 工具类](./10-工具类设计与Objects.md) —— `Objects.equals` / `Objects.hash`
- [11 集合框架 & 泛型](./11-集合框架与泛型.md) —— HashMap 如何用 hashCode 找桶
- [13 Record 与现代数据类](./13-Record与现代数据类.md) —— Record 自动生成 equals/hashCode

---

## 📌 一句话总结

> **`==` 比引用，`equals` 比语义**。**`equals` 和 `hashCode` 必须成对重写**，否则 HashMap 会失灵。**数组是个坑** —— 没重写 equals/hashCode，永远用 `Arrays.equals` / `Arrays.hashCode`。**现代 Java 首选 Record**，equals/hashCode 自动生成，永远正确。
