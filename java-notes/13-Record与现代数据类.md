# Record 与现代 Java 数据类

> 📍 来源：原型课 / 拷贝全景笔记中反复提到 Record 却没讲透
> 🎯 回答的核心问题："**Record 是什么？哪个版本开始有的？怎么用？和普通类有什么区别？**"

---

## 🤔 场景问题

学原型模式和拷贝全景时我多次提到"用 Record 解决不可变问题"，但你可能会问：
- Record 是个语法关键字吗？类吗？
- 从哪个 Java 版本开始？
- 怎么写、怎么用？
- 和普通类、Lombok 的 `@Data` 有什么区别？

一次讲完。

---

## 🧠 核心结论

> **Record 是 Java 14 引入、Java 16 正式的"语法糖"**，用于声明**不可变数据类**。
>
> **一行代码 = 一个带字段、构造器、getter、equals、hashCode、toString 的完整类**。

---

## 📅 版本历史

| Java 版本 | 发布时间 | Record 状态 |
|-----------|---------|------------|
| Java 14 | 2020-03 | **预览特性（Preview）** |
| Java 15 | 2020-09 | 二次预览 |
| **Java 16** | **2021-03** | **✅ 正式版（Standard）** |
| Java 17 LTS | 2021-09 | 稳定可用 |
| Java 21 LTS | 2023-09 | 和 Sealed Class / Pattern Matching 深度协作 |

**实战建议**：
- Java 17+ 项目可以**放心用 Record**
- Java 11（某些老项目）**不支持**，要考虑兼容性

你当前用的 **Java 21 完全支持**，写 Record 没任何问题。

---

## 💥 为什么需要 Record？—— 先看老写法的痛

在 Record 之前，写一个"简单数据类"（比如一个点 Point）要这样：

```java
public final class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point[x=" + x + ", y=" + y + "]";
    }
}
```

**40+ 行**，全是样板代码。加一个字段所有方法都要改。

---

## ✨ Record 版：**一行搞定**

```java
public record Point(int x, int y) {}
```

**就这一行**。编译器自动生成：

- ✅ 两个 `private final` 字段 `x` 和 `y`
- ✅ 规范构造器 `Point(int x, int y)`
- ✅ 访问器 `x()` 和 `y()`（注意**不是** `getX()` / `getY()`）
- ✅ 合理的 `equals()` / `hashCode()` / `toString()`

---

## 🔬 编译器生成的等价代码

你可以这样理解 Record：

```java
// 你写的：
public record Point(int x, int y) {}

// 编译器背后生成的（大致等价于）：
public final class Point extends java.lang.Record {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() { return x; }                 // 注意叫 x() 不是 getX()
    public int y() { return y; }

    @Override
    public boolean equals(Object o) { /* 自动生成，按字段比 */ }

    @Override
    public int hashCode() { /* 自动生成，按字段组合 */ }

    @Override
    public String toString() {
        return "Point[x=" + x + ", y=" + y + "]";
    }
}
```

**关键事实**：
- Record **隐式继承自** `java.lang.Record`
- Record **隐式是 `final` class**（不能被继承）
- 所有字段**自动是 `private final`**
- 所有字段的 getter 名字**就是字段名**（`x()` 不是 `getX()`）

---

## 📝 完整语法详解

### 1. 最基础用法

```java
record Point(int x, int y) {}

Point p = new Point(3, 4);
System.out.println(p.x());           // 3        ← 注意是 x() 不是 getX()
System.out.println(p.y());           // 4
System.out.println(p);                // Point[x=3, y=4]

Point q = new Point(3, 4);
System.out.println(p.equals(q));     // true     ← 自动按字段比
System.out.println(p == q);          // false    ← 不同对象
```

### 2. 加自定义方法

```java
record Point(int x, int y) {
    public double distance(Point other) {
        int dx = x - other.x;              // 可以直接用字段名
        int dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

Point a = new Point(0, 0);
Point b = new Point(3, 4);
System.out.println(a.distance(b));   // 5.0
```

### 3. **紧凑构造器（Compact Constructor）—— Record 的杀手锏**

想在构造时**做校验或规范化**？用紧凑构造器：

```java
record Point(int x, int y) {
    // 👇 紧凑构造器：没有参数列表，省去 this.x = x 这些赋值
    public Point {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("坐标必须非负");
        }
        // 对参数进行规范化也可以
        // x = Math.min(x, 100);
        // y = Math.min(y, 100);
    }
    // this.x = x; this.y = y;  ← 编译器自动加在紧凑构造器末尾
}

new Point(-1, 0);   // 抛 IllegalArgumentException
```

**紧凑构造器的特点**：
- 没有参数列表
- 没有显式赋值（`this.x = x` 编译器自动补）
- 用于**参数校验**和**规范化**

### 4. 规范构造器（Canonical Constructor，完整写法）

```java
record Point(int x, int y) {
    public Point(int x, int y) {       // 这叫规范构造器
        if (x < 0 || y < 0) throw new IllegalArgumentException();
        this.x = x;
        this.y = y;
    }
}
```

**和紧凑构造器的区别**：规范构造器**要求显式赋值**。紧凑构造器更简洁，大多数场景用紧凑构造器。

### 5. 附加构造器（必须委托给规范构造器）

```java
record Point(int x, int y) {
    // 附加构造器
    public Point() {
        this(0, 0);      // 必须调用其他构造器
    }

    public Point(int both) {
        this(both, both);
    }
}

new Point();        // Point[x=0, y=0]
new Point(5);       // Point[x=5, y=5]
```

### 6. 静态方法 / 静态字段

```java
record Point(int x, int y) {
    public static final Point ORIGIN = new Point(0, 0);

    public static Point of(int x, int y) {     // 静态工厂
        return new Point(x, y);
    }
}

Point p = Point.of(3, 4);
Point o = Point.ORIGIN;
```

### 7. 实现接口

```java
record Point(int x, int y) implements Comparable<Point> {
    @Override
    public int compareTo(Point other) {
        int dx = Integer.compare(x, other.x);
        return dx != 0 ? dx : Integer.compare(y, other.y);
    }
}
```

**Record 可以实现接口，但不能继承类**（它隐式继承 `java.lang.Record`）。

### 8. 泛型 Record

```java
record Pair<A, B>(A first, B second) {}

Pair<String, Integer> p = new Pair<>("Alice", 30);
System.out.println(p.first());     // Alice
System.out.println(p.second());    // 30
```

---

## 🚫 Record 的限制

### 1. **不能继承其他类**

```java
record Point(int x, int y) extends SomeClass {}  // ❌ 编译错误
```

因为 Record 隐式 `extends java.lang.Record`，Java 不支持多继承。

### 2. **不能声明实例字段**

```java
record Point(int x, int y) {
    private int z;    // ❌ 编译错误
}
```

字段只能在 header 里声明。这保证了"不可变 + 字段一目了然"。

### 3. **字段强制 `final`**

```java
record Point(int x, int y) {
    public void setX(int x) {
        this.x = x;     // ❌ 编译错误：x 是 final
    }
}
```

Record 的不可变是**语言级保证**，不是约定。

### 4. **Record 本身是 `final`**

```java
class SpecialPoint extends Point {}   // ❌ 编译错误
```

Record 不能被继承。

### 5. **不适合需要 setter 的老框架**

一些老版本的 Hibernate、Jackson（虽然 Jackson 2.12+ 已支持）、Spring MVC 在反射创建对象时依赖**无参构造器 + setter**。Record 没有无参构造器 + 字段是 final → 这些框架**可能不兼容**。

**实战**：新项目 + 新框架（Spring Boot 3+ / Jackson 2.12+）没问题。老框架先确认支持。

---

## 🎯 Record vs 传统 class vs Lombok

| 特性 | 传统 class | Lombok `@Data` | **Record** |
|------|-----------|----------------|-----------|
| 代码量 | 多 | 少（有注解） | **最少** |
| 需要第三方依赖 | 否 | **是** | 否 |
| IDE 支持 | 100% | 需要插件 | 100% |
| 不可变 | 手动 final | `@Value` 实现 | **强制** |
| 能继承类 | 是 | 是 | 否 |
| 自动 `equals` / `hashCode` | 手写 | 自动 | 自动 |
| 生成的访问器命名 | `getX()` | `getX()` | **`x()`** |
| 版本要求 | Java 1.0+ | Java + Lombok 注解处理器 | **Java 14+ 预览 / 16+ 正式** |

**选型**：
- 新项目，JDK 17+ → **直接用 Record**
- 老项目，还没升 Java 16+ → Lombok `@Value`
- 需要继承 → 传统 class

---

## 🌍 Record 最适合的场景

### 1. **DTO / VO（数据传输对象）**

```java
record UserDto(Long id, String name, String email) {}

// Controller 返回
@GetMapping("/users/{id}")
public UserDto getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return new UserDto(user.getId(), user.getName(), user.getEmail());
}
```

### 2. **Map 的 Key**

```java
record Coord(int x, int y) {}

Map<Coord, String> grid = new HashMap<>();
grid.put(new Coord(0, 0), "起点");
grid.put(new Coord(3, 4), "宝藏");
```

**为什么适合**：Record 自动有正确的 `equals` / `hashCode`，是 Map Key 的完美候选。

### 3. **返回多个值（元组）**

```java
record Result(int value, String message) {}

public Result process(int x) {
    if (x < 0) return new Result(-1, "负数");
    return new Result(x * 2, "成功");
}

Result r = process(5);
System.out.println(r.value() + ": " + r.message());
```

Java 以前没有"元组"类型，要靠 `Pair<A, B>` 这种自己写。Record 轻松解决。

### 4. **不可变值对象（DDD）**

```java
record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负");
        }
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("币种不匹配");
        }
        return new Money(amount.add(other.amount), currency);
    }
}
```

### 5. **API 响应**

```java
record ApiResponse<T>(int code, String msg, T data) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "OK", data);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(500, msg, null);
    }
}

ApiResponse.ok(userDto);
ApiResponse.<String>error("找不到用户");
```

---

## 🚀 现代 Java 特性：Record 的组合拳

### Record + Pattern Matching（Java 21 正式）

```java
record Point(int x, int y) {}

Object obj = new Point(3, 4);

// Java 老写法
if (obj instanceof Point) {
    Point p = (Point) obj;
    System.out.println(p.x() + ", " + p.y());
}

// Java 16+ 模式匹配
if (obj instanceof Point p) {
    System.out.println(p.x() + ", " + p.y());
}

// Java 21 Record 解构
if (obj instanceof Point(int x, int y)) {
    System.out.println(x + ", " + y);       // 直接拿到字段！
}
```

### Record + Sealed Classes（Java 17+）

```java
sealed interface Shape permits Circle, Square, Triangle {}

record Circle(double radius) implements Shape {}
record Square(double side) implements Shape {}
record Triangle(double base, double height) implements Shape {}

// 穷尽性检查的 switch
double area(Shape s) {
    return switch (s) {
        case Circle c   -> Math.PI * c.radius() * c.radius();
        case Square sq  -> sq.side() * sq.side();
        case Triangle t -> 0.5 * t.base() * t.height();
        // 编译器保证覆盖了所有可能，不用 default
    };
}
```

这叫 **ADT（代数数据类型）**，函数式语言的核心特性。Java 现在也有了。

### Record + Switch Expression

```java
record Order(String id, int amount, String status) {}

Order o = new Order("001", 100, "PAID");

String desc = switch (o.status()) {
    case "PAID"     -> "已支付";
    case "PENDING"  -> "待支付";
    case "SHIPPED"  -> "已发货";
    default         -> "未知";
};
```

---

## ⚠️ 常见误区

### 误区 1：`getX()` 获取 Record 字段
**错**。Record 的访问器**没有 `get` 前缀**，直接是字段名：`p.x()`。

### 误区 2：Record 就是 Lombok `@Data`
**错**。Lombok `@Data` 生成的类是**可变**的（有 setter）。Record 是**强制不可变**的。Lombok 真正对应的是 `@Value`。

### 误区 3：Record 不能做 JPA Entity
**主要正确**。JPA Entity 需要无参构造器 + setter + 可变。Record 不支持。用传统 `@Entity` class。

### 误区 4：Record 是反射受限的
**不是**。Record 可以被反射正常访问，API 一样。

### 误区 5：Record 不能有业务逻辑
**错**。Record 可以定义方法，和普通类一样。只是字段是 final、不能加实例字段。

### 误区 6：Record 总能继承某个类
**错**。Record 不能继承任何类，只能实现接口。

---

## 🎁 Record 的"隐藏好处"

### 1. `toString` 默认带字段名
```java
System.out.println(new Point(3, 4));
// 输出：Point[x=3, y=4]
```
比传统类的 `@Id{x=3,y=4}` 或 Lombok 的 `Point(x=3, y=4)` 更规范。

### 2. `equals` 自动按字段比
**所有字段都参与比较**，不用担心漏字段。

### 3. **序列化天然支持**
Record 实现 `Serializable` 后，序列化反序列化**不需要** `readObject` / `writeObject` 等自定义方法。

### 4. **反射 API 增强**
Java 16+ 的 `Class.getRecordComponents()` 能拿到所有 Record 组件的元信息，比反射实例字段更清晰。

---

## 📌 适用 / 不适用速查表

### ✅ 适合用 Record
- DTO / VO / API 响应
- Map 的 Key
- 返回多值（元组）
- 值对象（Money / Address）
- 函数式风格的数据

### ❌ 不适合用 Record
- JPA Entity（需要可变 + 无参构造）
- 需要继承的类
- 复杂业务对象（有复杂生命周期 / 状态）
- 需要 setter 的老框架

---

## 🔗 相关深入

- **Pattern Matching for switch**（Java 21 正式）—— Record 解构
- **Sealed Classes**（Java 17）—— 和 Record 组合成 ADT
- **Valhalla Project** —— Record 是通向 Value Types 的垫脚石
- **Scala case class / Kotlin data class** —— Record 灵感来源

---

## 🎯 回到原型/拷贝的主线

Record 对拷贝的意义：

```java
record User(String name, int age) {}

User old = new User("Alice", 30);

// 想"修改" age：创建新 Record，共享不变字段
User updated = new User(old.name(), 31);
//                       ↑          ↑
//                  共享 String   新值

// 这就是"原型模式"的本质 —— 基于已有对象创建新对象
// 但因为 Record 不可变，不存在"浅拷贝污染"问题，共享引用是安全的
```

**Record + 不可变设计 = 原型模式的终极答案**，你不用操心浅/深拷贝，因为**不可变对象的引用共享永远安全**。

---

## 🌊 Record 体现的设计思想：Data Oriented Programming

Record 不是凭空出现的语法糖，它代表 **Java 演化的一个大方向**。

### 📜 Record 的血缘谱系

```
Haskell / ML (函数式鼻祖 1970-80s)
  data Point = Point Int Int        ← 代数数据类型 ADT
      ↓
Scala (2004) case class
  case class Point(x: Int, y: Int)  ← 不可变数据类
      ↓
Kotlin (2011) data class
  data class Point(val x: Int, val y: Int)
      ↓
C# 9 (2020) record
  record Point(int X, int Y);
      ↓
Java 16 (2021) record              ← Java 终于跟上
  record Point(int x, int y) {}
```

共同目标：**让"声明一个不可变的简单数据类型"不再痛苦**。

### 🧭 Record 吸收了函数式的哪些思想

Java 语言架构师 **Brian Goetz**（Record 的主要推手）提出一个概念：**"Data Oriented Programming（数据导向编程）"**。

| 函数式特征 | Java 情况 |
|----------|---------|
| **不可变数据** | **Record 支持**（从函数式借来的） |
| **纯函数** | Java 部分支持（Stream / Lambda） |
| **函数作为一等公民** | Java 支持（Lambda） |
| **代数数据类型 ADT** | **Java 16+ 用 Record + Sealed 支持** |
| **模式匹配** | Java 16+ 支持（未来更强） |

**Record 是"不可变数据"这一条的实现** —— 不是完整的函数式，是"一片函数式思想移植到 Java"。

### 🎭 三种编程范式对比（同样实现"银行账户入账"）

**OOP 风格（传统 Java）**
```java
class Account {
    private BigDecimal balance;
    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);   // 修改自己的状态
    }
}
// 数据和行为捆绑 / 可变
```

**数据导向风格（Java 16+ Record）**
```java
record Account(String id, BigDecimal balance) {}

class AccountOps {
    public static Account deposit(Account a, BigDecimal amount) {
        return new Account(a.id(), a.balance().add(amount));   // 返回新对象
    }
}
// 数据和行为分离 / 不可变
```

**纯函数式（Haskell）**
```haskell
deposit :: Account -> Rational -> Account
deposit acc amount = acc { balance = balance acc + amount }
-- 强制不可变 / 强制纯函数 / 数据行为彻底分离
```

**Java 16+ 的 Record 让你能写出"接近函数式风格"的 Java 代码，但不强制**。

### 🎯 Brian Goetz 的明确立场

> "Java is not becoming a functional language. Java is becoming a language where you can choose OOP or data-oriented style based on the problem."
>
> "Java 不是在变成函数式语言。Java 是在变成一种**让你按问题选择** OOP 或数据导向风格的语言。"

**关键词**：**多范式**（multi-paradigm）。工具多了，选择权给了开发者。

### 📚 你会听到的相关概念

| 术语 | 含义 |
|------|------|
| **POJO** (Plain Old Java Object) | 纯 Java 对象，无框架约束 |
| **Value Object** (DDD) | 不可变的值对象，Record 天然合适 |
| **ADT** (Algebraic Data Types) | 代数数据类型 = Product + Sum types |
| **Product Type** | 多字段组合（Record 就是这个）|
| **Sum Type** | 多种可能之一（Sealed Class 就是这个）|
| **Data Oriented Programming** | 数据和行为分离的编程范式 |
| **Persistent Data Structure** | 不可变数据结构（Clojure / Scala 核心）|
| **Structural Sharing** | 结构共享（不可变对象的高效更新）|

以后看到这些词就不陌生了。

---

## 📌 一句话总结

> **Record 是 Java 14 引入、Java 16 正式的"不可变数据类语法糖"。一行 `record Point(int x, int y) {}` 等价于一个 40 行的 final class。访问器是 `x()` 不是 `getX()`。Record 是 Java 向 Data Oriented Programming 演化的关键一步，吸收了函数式的"不可变数据"思想，但 Java 仍是多范式语言 —— OOP 和数据导向风格任你选。最适合 DTO / 值对象 / 元组 / Map Key，不适合 JPA Entity 和需要继承的场景。**
