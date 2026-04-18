# Java 程序生命周期 · 从源代码到 INSTANCE 赋值

> 📍 来源：注解体系讨论中冒出的"编译期 / 运行时 / JVM"三个概念的精准定义需求 + 想把饿汉单例从"写代码"到"INSTANCE 被创建"的完整旅程串起来
> 🎯 回答的核心问题："**一个 Java 程序从写代码到 static 字段赋值、对象创建，中间一共经历了哪些阶段？每个阶段发生了什么？**"

---

## 🤔 场景问题

你写了这么一段饿汉单例：

```java
public class EagerPrinter {
    private static final EagerPrinter INSTANCE = new EagerPrinter();

    private EagerPrinter() {
        System.out.println("构造器执行");
    }
}
```

从你**敲下这段代码**，到**最终 INSTANCE 指向一个真实对象**，中间到底经过了**多少阶段**？**每个阶段在做什么**？

一次讲透。

---

## 🧠 核心结论

> Java 程序的一生分 **两大期 + 六小步**：
>
> **两大期**：编译期（javac 工作）→ 运行期（JVM 工作）
>
> **运行期的六小步**：`Loading → Verify → Prepare → Resolve → Initialize → Use`
>
> **饿汉单例的 `INSTANCE = new EagerPrinter()` 在第 5 步 Initialize 执行**。

---

## 📅 Java 程序一生的完整时间轴

```
【编译期】─────────────────────────┐
                                   │
① 你写代码                         │
   EagerPrinter.java               │
        ↓                          │
② 编译                             │
   javac EagerPrinter.java         │
   ├─ 语法/类型检查                │ 编译期
   ├─ @Override 内置注解处理        │ 发生在运行前
   └─ APT 处理（Lombok 等）        │
        ↓                          │
③ 产出字节码                       │
   EagerPrinter.class              │
                                   │
═══════════════════════════════════╪══════════════
                                   │
【运行期】─────────────────────────│
                                   │
④ JVM 启动                        │
   java Main                       │
        ↓                          │ 运行期
⑤ 首次使用 EagerPrinter 类 → 类加载 │ JVM 接管
   ┌─────────────────────────────┐ │
   │ 1) Loading 加载              │ │
   │    读字节码到方法区           │ │
   ├─────────────────────────────┤ │
   │ 2) Verification 验证        │ │
   │    检查字节码合法             │ │
   ├─────────────────────────────┤ │
   │ 3) Preparation 准备         │ │
   │    static 字段默认值         │ │
   │    ★ INSTANCE = null        │ │
   ├─────────────────────────────┤ │
   │ 4) Resolution 解析          │ │
   │    符号引用→直接引用         │ │
   ├─────────────────────────────┤ │
   │ 5) Initialization 初始化    │ │
   │    执行 <clinit>            │ │
   │    ★ INSTANCE = new Eager() │ │← 饿汉单例的"生日"在这里！
   └─────────────────────────────┘ │
        ↓                          │
⑥ 使用类                           │
   EagerPrinter.getInstance()      │
        ↓                          │
⑦ 程序结束 → JVM 退出              │
```

---

# 🛠 第一大期：编译期（你代码的"翻译时间"）

## 发生时机
你敲完代码、按下保存、IDE 自动编译 / 你手动 `javac` 的那一刻到字节码生成为止。

## 谁在工作
- **javac**（Java 编译器）
- **APT 处理器**（Lombok / Dagger 等）

## javac 做的事

### 1. 词法分析 + 语法分析
```java
int x = 1 + 2;
```
拆成 token 流：`int` `x` `=` `1` `+` `2` `;` → 构建 AST（抽象语法树）。

### 2. 类型检查
```java
int x = "hello";   // ❌ 编译器报错：类型不匹配
```
javac 在编译期**就抓住**类型错误，不让它进字节码。

### 3. 处理内置注解
```java
@Override
public void foo() { ... }
```
javac 看到 `@Override` → 检查父类有没有 foo 方法 → 没有就报错。

### 4. 调用 APT 处理器

```java
@Data
public class User { String name; }
```

javac 发现 `@Data` 是 Lombok 的注解 → 调用 Lombok 的 APT 处理器 → Lombok 生成 getter/setter/equals/hashCode → javac 把生成的代码一起编译进字节码。

### 5. 生成字节码
最终产出 `.class` 文件，里面包含：
- 类的元数据（类名、字段声明、方法签名）
- 字节码指令（你方法里写的逻辑，翻译成 JVM 指令）
- 常量池（字符串、数字等常量）

## 编译期**能做**的事
- ✅ 检查语法、类型
- ✅ 生成代码（APT）
- ✅ 静态分析

## 编译期**做不到**的事
- ❌ 执行代码（没有值可以算）
- ❌ 访问运行时状态（没 JVM 呢）
- ❌ 处理用户输入

---

# 🚀 第二大期：运行期（程序的"演出时间"）

## 发生时机
你执行 `java Main` → JVM 启动 → 加载类 → 执行 main → 程序结束。

## 谁在工作
- **JVM**（Java 虚拟机）
- **ClassLoader**（类加载器）
- **GC**（垃圾回收器）
- **JIT**（即时编译器，热点代码优化）

## JVM 的核心任务
1. 启动（初始化内存分区、主线程）
2. **加载类**（按需加载 .class）
3. **执行字节码**（解释器 / JIT）
4. 管理内存（分配 + GC）
5. 反射 / 动态代理
6. JIT 编译热点代码成机器码

---

# 🔬 运行期的六小步：类加载详解

**当 JVM 第一次需要某个类时**（比如访问它的 static 字段 / 调 static 方法 / new 实例），进入**类加载流程**：

## Step 1：Loading（加载）

**做什么**：
- ClassLoader 找到对应的 `.class` 文件
- 读字节流 → 存到方法区（JDK 8+ 叫"元空间 Metaspace"）
- 在堆里生成一个代表这个类的 `Class` 对象

**产出**：JVM "认识"这个类了，有了它的元数据。

**注意**：这一步**还没创建任何实例**。

## Step 2：Verification（验证）

**做什么**：
- 检查字节码合法性（防止恶意字节码）
- 验证类型安全、引用合法等

**为什么重要**：JVM 是沙箱，字节码可以不信任。这一步是安全阀。

## Step 3：Preparation（准备）—— ★ 关键步骤 ★

**做什么**：
- **给 static 字段分配内存**
- **赋"默认值"**（不是你写的初始值！）

```java
class EagerPrinter {
    private static final EagerPrinter INSTANCE = new EagerPrinter();
    //                                           ↑
    //                          这一步执行后，INSTANCE = null（默认值）
    //                          还没调 new EagerPrinter()！
}
```

**默认值规则**：
- 引用类型 → `null`
- int → `0`
- boolean → `false`
- long → `0L`
- 等等

**关键**：Preparation 只分配内存 + 赋默认值，**不执行你写的初始化代码**。

## Step 4：Resolution（解析）

**做什么**：
- 把字节码里的**符号引用**（字符串形式的类名、方法名）换成**直接引用**（内存地址）

这一步比较抽象，记住"符号引用变直接引用"就行。

## Step 5：Initialization（初始化）—— ★ INSTANCE 的"生日" ★

**做什么**：
- 执行 `<clinit>` 方法（编译器生成的"类初始化方法"）
- `<clinit>` 里包含：
  - 所有 `static {}` 块的代码
  - 所有 `static 字段 = 值` 的赋值语句

**关键时刻**：

```java
class EagerPrinter {
    private static final EagerPrinter INSTANCE = new EagerPrinter();
    //                                  ↑
    //          <clinit> 执行到这里 → 调用构造器 new EagerPrinter()
    //          → 分配对象内存 → 执行构造器逻辑
    //          → INSTANCE 字段从 null 变成真实对象的地址
}
```

**饿汉单例的 INSTANCE 就是在这一步真正"出生"的**。

## Step 6：Use（使用）

类已经完全加载 + 初始化完毕，可以随便用：
- 调 static 方法：`EagerPrinter.getInstance()`
- 读 static 字段：`EagerPrinter.INSTANCE`
- new 实例（如果构造器是 public）
- 反射、代理、一切操作

---

# 🎬 饿汉单例的完整旅程（串起所有知识）

```java
public class EagerPrinter {
    private static final EagerPrinter INSTANCE = new EagerPrinter();
    private EagerPrinter() {
        System.out.println("构造器被调用");
    }
    public static EagerPrinter getInstance() { return INSTANCE; }
}
```

假设 main 方法里写：`EagerPrinter e = EagerPrinter.getInstance();`

**完整时间线**：

```
T1: 编译期（javac）
    ├── 读 EagerPrinter.java
    ├── 检查语法（private 构造器？OK）
    ├── 生成字节码 EagerPrinter.class
    │   ├── 字段: static INSTANCE (EagerPrinter 类型)
    │   ├── 方法: <init>() { println("构造器被调用"); }
    │   ├── 方法: getInstance() { return INSTANCE; }
    │   └── 方法: <clinit>() { INSTANCE = new EagerPrinter(); }
    └── 完成

T2: java Main 启动
    JVM 启动 → 主线程 → 加载 Main 类 → 执行 Main.main

T3: main 里执行 EagerPrinter.getInstance()
    JVM: "我还没见过 EagerPrinter，得加载它"
    ↓

T4: 【Loading】
    ClassLoader 找到 EagerPrinter.class
    读字节流 → 方法区
    堆里生成 Class<EagerPrinter> 对象

T5: 【Verification】
    检查字节码合法 → OK

T6: 【Preparation】
    为 INSTANCE 分配内存
    ★ INSTANCE = null（默认值，不是你写的那个 new）

T7: 【Resolution】
    解析符号引用 → 直接引用

T8: 【Initialization】← 最激动人心的一步
    执行 <clinit>:
        INSTANCE = new EagerPrinter()  ← 这一行触发：
            ├── 在堆上分配 EagerPrinter 对象的内存
            ├── 调用构造器 <init>()
            │   └── 打印 "构造器被调用"
            ├── 对象创建完成，返回地址
            └── INSTANCE 字段更新为那个地址
    ★ 此时 INSTANCE 从 null 变成真实对象引用

T9: 【Use】
    EagerPrinter.getInstance() 返回 INSTANCE
    → 调用方拿到唯一那个对象

T10: 以后任何地方再调 getInstance()
    → 类已初始化 → 直接返回 INSTANCE
    → 永远返回同一个对象 ← 这就是单例
```

---

# 🎯 各种注解在哪个阶段生效（串联 15 号笔记）

```
【编译期】
  ├── @Override      → javac 检查
  ├── @Deprecated    → javac 警告
  ├── @SuppressWarnings → javac 抑制警告
  └── @Data / @With / @Builder (Lombok) → APT 生成代码

【运行期】
  ├── 类加载时:
  │   └── 类加载器读取所有 @Retention(RUNTIME) 的注解元数据
  ├── 应用启动时:
  │   ├── @Component / @Service → Spring 扫描 + 实例化
  │   ├── @Entity → JPA 建立映射
  │   └── @Test → JUnit 识别
  ├── 方法调用时:
  │   └── @Autowired → Spring 反射注入
  └── 代理机制:
      └── @Transactional → 动态代理拦截
```

**规律**：
- 注解的 `@Retention` 决定它能存活到哪个阶段
- 处理时机对应注解的使用者（javac / APT / Spring）

---

# 🔀 和 JVM 的关系

**严谨区分**：

| 术语 | 指代 |
|------|------|
| **编译期**（Compile Time） | 强调"**代码生成 / 检查**"的阶段 |
| **运行期**（Runtime） | 强调"**程序执行**"的阶段 |
| **JVM** | 运行期的**执行引擎** |

**有趣的事实**：
- javac 本身是**用 Java 写的**，所以 javac 运行时也跑在 JVM 上
- 但 javac 的**工作目标**是**把你的源码转成别的程序能运行的字节码**
- 所以"**编译期**"和"**运行期**"指的是**你写的程序**的生命周期，不是 javac 或 JVM 自己

---

# 📊 完整对比表

| 维度 | 编译期 | 运行期 |
|------|-------|-------|
| 发生时机 | 你写完代码，程序跑之前 | JVM 启动后，程序执行时 |
| 工具 | `javac` + APT 插件 | JVM + ClassLoader + GC |
| 输入 | `.java` 源代码 | `.class` 字节码 |
| 输出 | `.class` 字节码 | 程序执行结果 |
| 能访问值 | ❌（还没有运行时状态）| ✅ |
| 能生成代码 | ✅（APT）| ❌ |
| 能动态决定 | ❌（一切静态）| ✅（反射、代理）|
| 注解作用 | 内置 + APT | 反射读取 |
| 典型错误 | 编译错误 | 运行时异常（NPE、StackOverflow）|
| 性能特征 | 一次性 | 长期运行，JIT 优化 |

---

# ⚠️ 常见误区

### 误区 1："编译期 = javac 自己的运行期"
**概念混淆**。"编译期/运行期"是**针对你写的程序**而言的：
- 你的 `Foo.java` 被 javac 编译 → 这对 Foo 来说是"编译期"
- `Foo.class` 被 JVM 执行 → 这对 Foo 来说是"运行期"

### 误区 2："static 字段在声明时就有值"
**错**。
- 声明时：`static int x = 10;` 只是**一条赋值语句**
- Preparation（准备）阶段：x 被分配内存，**赋默认值 0**
- Initialization（初始化）阶段：**执行 x = 10**，才真正赋 10

### 误区 3："类加载 = 创建对象"
**完全不同**。
- 类加载：把 class 信息读进 JVM（一次）
- 创建对象：在堆里分配一个实例（可以多次）

一个类加载一次后，可以 new 1000 个对象。

### 误区 4："饿汉单例的 INSTANCE 在程序启动时就创建"
**不一定**。
- 如果你**从来不用 EagerPrinter 类**，JVM 就**不会加载它**
- 类加载是**按需触发**的，不是启动时批量做

### 误区 5："APT 能修改运行期行为"
**不能直接**。APT 只在编译期**生成代码**，运行时 APT 本身不在。但它生成的代码（比如 Lombok 的 `setName` 方法）在运行时照常工作。

---

# 🎁 一个让你秒懂"按需加载"的实验

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("main 开始");
        // 没用 NeverUsed 类
    }
}

class NeverUsed {
    static {
        System.out.println("⚠️ NeverUsed 被加载了！");
    }
}
```

**跑一下 `java Main`，你会看到**：
```
main 开始
```

**就这一行**。`NeverUsed` 根本没被加载（static 块都没执行），因为 main 里没用到它。

**类加载是按需触发的**。

---

# 🔗 相关笔记

- [01 类加载三阶段](./01-类加载三阶段.md) —— 类加载的详细展开（本篇是总览）
- [02 静态字段 vs 实例字段](./02-静态字段vs实例字段.md) —— static 字段的内存归属
- [13 Record 与现代数据类](./13-Record与现代数据类.md) —— 编译器把 Record 翻译成什么
- [15 Java 注解体系完全指南](./15-Java注解体系完全指南.md) —— 注解在哪个阶段生效
- [16 跨语言辨析](./16-注解与装饰器跨语言辨析.md) —— 不同语言的编译/运行期机制

---

# 📌 一句话总结

> **Java 程序的一生 = 编译期（javac 把 .java 翻译成 .class）+ 运行期（JVM 加载并执行 .class）**。
>
> **运行期里**，当首次使用某个类时，会走 6 小步：**加载 → 验证 → 准备（字段默认值）→ 解析 → 初始化（执行 static 赋值，饿汉单例的 INSTANCE 在这里"出生"）→ 使用**。
>
> **饿汉单例的 `INSTANCE = new EagerPrinter()` 发生在运行期的第 5 步"初始化"**，不是写代码时，不是编译时，是 JVM 在执行 `<clinit>` 方法时。
