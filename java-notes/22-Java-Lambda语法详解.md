# Java Lambda 语法详解

> 📍 来源：策略模式课引出"Lambda 简化策略"，用户要求详细展开
> 🎯 本篇目标：从零讲清楚 Java Lambda 的**所有语法细节**

---

## 🤔 场景问题

看到这种代码你可能懵过：

```java
list.sort((a, b) -> a.getAge() - b.getAge());

Runnable r = () -> System.out.println("hello");

strategies.forEach((key, value) -> value.go("目的地"));
```

**`->` 是什么？括号怎么有时有有时没有？这些符号组合起来到底是什么？**

本篇讲透。

---

## 🧠 核心结论

> **Lambda 是 Java 8（2014）引入的"匿名函数"简化写法**。
>
> 本质：**一个接口（函数式接口）+ 一个简洁的函数实现语法**。
>
> 作用：让你用**一行代码**代替**一整个匿名类**。

---

## 📜 Lambda 的历史

### Java 8 之前：匿名内部类的痛

```java
// 排序：要写 5 行
Collections.sort(list, new Comparator<Integer>() {
    @Override
    public int compare(Integer a, Integer b) {
        return a - b;
    }
});

// 启动线程：也要 5 行
Thread t = new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("hello");
    }
});
```

**大量样板代码**，真正的逻辑只有一行。

### Java 8 之后：Lambda 让代码变成一行

```java
Collections.sort(list, (a, b) -> a - b);

Thread t = new Thread(() -> System.out.println("hello"));
```

**Lambda 是 Java 向函数式语言靠拢的一大步**。现在的 Java 代码几乎离不开它。

---

# 🔧 Part 1：基本语法

## Lambda 表达式的通用形式

```
(参数列表) -> { 方法体 }
```

## 5 种常见变体

### 变体 1：**无参**

```java
() -> System.out.println("hello")
```

等价于：
```java
new Runnable() {
    public void run() { System.out.println("hello"); }
}
```

### 变体 2：**1 个参数**（括号可省）

```java
// 带括号
(x) -> x * 2

// 省括号（只有一个参数时）
x -> x * 2
```

### 变体 3：**多个参数**（括号不能省）

```java
(a, b) -> a + b
```

### 变体 4：**方法体有多行**（花括号+ return）

```java
(x) -> {
    int y = x * 2;
    return y + 1;
}
```

**规则**：
- 方法体**单个表达式** → 不用 `{}`，返回值**自动就是表达式结果**
- 方法体**多条语句** → 要 `{}`，要显式 `return`

### 变体 5：**显式指定参数类型**（罕见）

```java
(int a, int b) -> a + b
```

通常类型可以从上下文**自动推断**，所以很少写类型。

## 对比看 5 种变体

```java
// ① 无参
Runnable r = () -> System.out.println("hi");

// ② 单参数
Function<Integer, Integer> square = x -> x * x;

// ③ 多参数
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

// ④ 多行方法体
Function<Integer, String> describe = x -> {
    if (x > 0) return "正数";
    if (x < 0) return "负数";
    return "零";
};

// ⑤ 显式类型（不推荐，啰嗦）
BiFunction<Integer, Integer, Integer> add2 = (Integer a, Integer b) -> a + b;
```

---

# ⚙️ Part 2：函数式接口（Lambda 的"锚点"）

## 什么是函数式接口

**函数式接口（Functional Interface）**：**有且只有一个抽象方法**的接口。

```java
// 这是函数式接口
interface Greeting {
    void say(String name);
}

// 这也是
interface Calculator {
    int calc(int a, int b);
}

// 这不是（有 2 个抽象方法）
interface Animal {
    void eat();
    void sleep();
}
```

## Lambda 只能赋给函数式接口

```java
Greeting g = name -> System.out.println("Hi, " + name);
//  ↑                ↑
// 接口类型         Lambda 实现

g.say("张三");   // 输出: Hi, 张三
```

**Lambda 不是"凭空"的函数**。它**必须有一个函数式接口"接"它**。

## `@FunctionalInterface` 注解

```java
@FunctionalInterface    // 👈 告诉编译器"这是函数式接口"
interface Greeting {
    void say(String name);
}
```

**作用**：
- 编译器检查"是不是真的只有一个抽象方法"
- 如果你后面误加了第二个抽象方法，编译器会报错
- **推荐所有函数式接口都加这个注解**

和 `@Override` 类似 —— 不加也能用，加了编译器帮你查错。

---

# 🎁 Part 3：JDK 5 个核心函数式接口

**绝大多数 Lambda 场景用 JDK 自带的 5 个接口，不用自己定义**。

### 1. `Function<T, R>` —— 一入参、有返回值

```java
Function<Integer, String> intToStr = x -> "数字: " + x;
String r = intToStr.apply(42);       // "数字: 42"
```

对应 `R apply(T t);`

### 2. `Consumer<T>` —— 一入参、无返回值（只"消费"）

```java
Consumer<String> printer = s -> System.out.println(s);
printer.accept("hello");              // 输出 hello
```

对应 `void accept(T t);`

### 3. `Supplier<T>` —— 无入参、有返回值（"生产"）

```java
Supplier<String> greeter = () -> "Hello";
String r = greeter.get();              // "Hello"
```

对应 `T get();`

### 4. `Predicate<T>` —— 一入参、返回 boolean（"判断"）

```java
Predicate<Integer> isEven = x -> x % 2 == 0;
isEven.test(4);                        // true
isEven.test(5);                        // false
```

对应 `boolean test(T t);`

### 5. `BiFunction<T, U, R>` —— 两入参、有返回值

```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
add.apply(1, 2);                       // 3
```

对应 `R apply(T t, U u);`

### 记忆口诀

| 接口 | 输入 | 输出 | 语义 |
|------|------|------|------|
| `Function<T,R>` | T | R | 变换 |
| `Consumer<T>` | T | void | 消费 |
| `Supplier<T>` | 无 | T | 生产 |
| `Predicate<T>` | T | boolean | 判断 |
| `BiFunction<T,U,R>` | T, U | R | 双入变换 |

---

# 🎯 Part 4：方法引用（Method Reference）

Lambda 还能**进一步简化**成"方法引用"。

## 四种方法引用

### 类型 1：**静态方法引用** `Class::staticMethod`

```java
// Lambda 写法
Function<Integer, String> f1 = x -> Integer.toString(x);

// 方法引用（更简洁）
Function<Integer, String> f2 = Integer::toString;
```

**规则**：Lambda 只是调了个静态方法 → 可以用 `类::方法名`。

### 类型 2：**实例方法引用**（固定对象）`instance::method`

```java
String prefix = "Hello, ";

// Lambda
Function<String, String> f1 = name -> prefix.concat(name);

// 方法引用
Function<String, String> f2 = prefix::concat;
```

### 类型 3：**类的实例方法引用**（动态对象）`Class::instanceMethod`

```java
// Lambda
Function<String, Integer> f1 = s -> s.length();

// 方法引用
Function<String, Integer> f2 = String::length;
```

**规则**：Lambda 在**参数上**调实例方法 → 可以用 `Class::方法`。

### 类型 4：**构造器引用** `Class::new`

```java
// Lambda
Supplier<ArrayList<String>> s1 = () -> new ArrayList<>();

// 方法引用
Supplier<ArrayList<String>> s2 = ArrayList::new;
```

## 能省就省的原则

```java
// 别这么写
list.forEach(x -> System.out.println(x));

// 这样最干净
list.forEach(System.out::println);
```

**IDE 会自动提示你改**。

---

# 🔒 Part 5：Lambda 的变量捕获

Lambda 可以**引用外部变量**，但有一个规则：

## 规则：只能引用 "effectively final" 的变量

"**effectively final**" = 实际上没改过的变量（哪怕没显式加 `final`）。

### ✅ 合法

```java
int base = 10;                             // 没显式 final，但没改过
Function<Integer, Integer> add = x -> x + base;   // ✅ base 是 effectively final
```

### ❌ 非法

```java
int base = 10;
base = 20;                                 // 🚨 改了！

Function<Integer, Integer> add = x -> x + base;
// ❌ 编译错误：Variable 'base' used in lambda expression should be final or effectively final
```

### 为什么？

**Lambda 可能被异步执行**（比如放到线程池里），那时候外部变量可能已经变了。
JDK 为了避免这种混乱，规定 Lambda 捕获的变量必须"**不变**"。

## 对比：Lambda 捕获 vs Python 闭包

- **Python**：可以随意捕获可变变量
- **Java Lambda**：必须是 effectively final

Java 的规则更严格，但更安全。

---

# 🆚 Part 6：Lambda vs 匿名内部类

**Lambda 并不完全等价于匿名内部类**，有几个重要区别：

| 对比项 | 匿名内部类 | Lambda |
|--------|----------|--------|
| 支持的接口 | 任意（含多方法）| **只支持函数式接口**（单方法）|
| `this` 关键字 | 指代匿名类本身 | **指代外部类** |
| 语法 | 冗长 | 极简 |
| 编译产物 | 单独的 `.class` 文件（Outer$1.class）| `invokedynamic` + 运行时生成（无单独 class）|
| 性能 | 每次创建新实例 | 可能被 JVM 缓存优化 |

## `this` 的陷阱（最大的坑）

```java
class OuterClass {
    String name = "外部";

    void test() {
        // 匿名内部类
        Runnable r1 = new Runnable() {
            String name = "匿名类内";
            public void run() {
                System.out.println(this.name);    // "匿名类内"
            }
        };

        // Lambda
        Runnable r2 = () -> {
            System.out.println(this.name);         // "外部" —— this 指外部类！
        };

        r1.run();
        r2.run();
    }
}
```

**规则**：
- 匿名类里 `this` = 匿名类本身
- Lambda 里 `this` = **外部类的 this**

**坑**：别把匿名类改写成 Lambda 时忘了这点。

---

# 🌊 Part 7：Stream + Lambda 实战

Lambda 的真正威力在 **Stream API** 里。

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// 过滤偶数 → 平方 → 求和
int sum = numbers.stream()
                 .filter(x -> x % 2 == 0)    // Lambda：Predicate
                 .map(x -> x * x)             // Lambda：Function
                 .mapToInt(Integer::intValue) // 方法引用
                 .sum();
System.out.println(sum);   // 4 + 16 + 36 + 64 + 100 = 220
```

**常用操作**：

| 操作 | Lambda 类型 |
|------|-----------|
| `filter(Predicate)` | `x -> boolean` |
| `map(Function)` | `x -> y` |
| `forEach(Consumer)` | `x -> void` |
| `reduce(BinaryOperator)` | `(a, b) -> result` |
| `collect(Collector)` | 复杂收集器 |

```java
// 排序
list.sort((a, b) -> a - b);
list.sort(Comparator.naturalOrder());        // 方法引用版

// 分组
Map<String, List<User>> byCity = users.stream()
    .collect(Collectors.groupingBy(User::getCity));

// 平均值
double avg = users.stream()
    .mapToInt(User::getAge)
    .average()
    .orElse(0);
```

**你日常写 Java 几乎天天用 Stream + Lambda**。

---

# ⚠️ Part 8：常见误区

### 误区 1："Lambda 只是语法糖，和匿名类一模一样"
**不对**。`this` 指向、编译产物、性能都不同。

### 误区 2："Lambda 可以实现任意接口"
**不对**。**只能实现函数式接口**（只有一个抽象方法）。

### 误区 3："Lambda 里可以修改外部变量"
**不可以**。外部变量必须是 **effectively final**（没改过）。

```java
int count = 0;
list.forEach(x -> count++);   // ❌ 编译错误
```

要改状态？用 `AtomicInteger` 或 `int[] count = {0};`（数组元素可改）。

### 误区 4："类型要写出来"
**不用**。类型可以推断：
```java
list.sort((a, b) -> a - b);     // 编译器推断 a, b 是 Integer
```

### 误区 5："方法引用总是更好"
**不一定**。简单场景是，但复杂逻辑写 Lambda 更清晰。

### 误区 6：`return` 怎么写
- 单表达式不写 return：`x -> x * 2`
- 多行要写 return：`x -> { return x * 2; }`

混淆写法会报错。

---

# 🎁 Bonus：自定义函数式接口

大多数时候用 JDK 的 `Function` / `Consumer` 等就够了。但有时候自定义更清晰：

```java
@FunctionalInterface
interface TransportStrategy {
    void go(String destination);
}

// 使用：任何 Lambda 只要匹配签名就能赋给它
TransportStrategy car = dest -> System.out.println("🚗 " + dest);
car.go("公司");
```

---

# 📚 相关笔记

- [07 Override vs Overload](./07-Override-vs-Overload.md) —— Lambda 不参与 Override
- [11 集合框架 & 泛型](./11-集合框架与泛型.md) —— Stream API 和 Lambda 的配合
- [16 跨语言辨析](./16-注解与装饰器跨语言辨析.md) —— Lambda 是函数式思想向 Java 的渗透
- 设计模式：[策略](../03-behavioral/02-strategy/) —— Lambda 让策略模式大幅简化

---

# 📌 一句话总结

> **Lambda = 匿名函数的简洁写法**，只能赋给**函数式接口**（单方法接口）。
>
> **5 种语法变体**（无参/1 参/多参/多行/带类型），**JDK 5 个核心函数式接口**（Function/Consumer/Supplier/Predicate/BiFunction），**4 种方法引用**（静态/实例/类型/构造器）。
>
> **变量捕获必须 effectively final**。**`this` 指向外部类**（和匿名类不同）。
>
> **Stream + Lambda 是现代 Java 的标配**，日常代码离不开。
