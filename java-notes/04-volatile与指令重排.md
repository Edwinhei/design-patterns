# volatile 与指令重排 · 为什么 DCL 必须要 volatile

> 📍 来源：单例 V3 双检锁 DCL
> 🎯 回答的核心问题："**synchronized 都加了，为什么还非要 volatile？**"

---

## 🤔 场景问题

V3 DCL 的字段声明：

```java
private static volatile DCLPrinter instance;
//               ↑
//          这个 volatile 能省吗？
```

很多文章说"不能省"，但具体为什么？**synchronized 不是已经保证线程安全了吗？**

---

## 🧠 一句话答案

> **synchronized 管不到"`new` 这一行代码内部的三个步骤之间的顺序"。volatile 管的就是这个。**

---

## 🔬 `new X()` 的三步真相

你写的这一行：

```java
instance = new DCLPrinter();
```

JVM 实际上拆成三步执行：

```
① 分配内存         （开个空房子）
② 调构造器         （装修房子：初始化字段）
③ instance 指向    （挂门牌号）
```

**逻辑顺序应该是 ① → ② → ③**。

但 JVM 的 JIT 编译器 + CPU 为了性能，会做**指令重排**，可能变成：

```
① 分配内存
③ instance 指向这块内存    ← 先挂门牌（instance 已经非 null！）
② 调构造器                  ← 后装修（对象字段还是默认值）
```

**只要最终结果"看起来一样"，JVM 认为重排合法**。在单线程内这确实没问题，但多线程下暴露漏洞。

---

## 🚨 多线程下的致命时刻

```
时间 →

┌─ 线程 A（在 synchronized 块里）──┐
│ ① 分配内存                        │
│ ③ instance 指向这块内存           │  ← 重排，instance 已非 null
│                                   │
│    【这瞬间，B 恰好路过……】       │
│                                   │
│ ② 调构造器（还没跑！）             │
└───────────────────────────────────┘
                 ↕
┌─ 线程 B ──────────────────────────┐
│                                   │
│ 第 1 次检查：if (instance==null)  │  ← B 在锁外偷看
│   → 看到 instance **非 null**     │  ← 被重排骗了
│   → 跳过整个 synchronized 块     │
│                                   │
│ return instance;                  │  ← 拿到一个**半初始化**对象
│                                   │
│ 使用对象 → 字段全是默认值         │  ← NPE / 脏数据
└───────────────────────────────────┘
```

**关键点**：
- A 还在锁里没出来
- 但因为重排，`instance` 已经"挂了门牌号"（非 null）
- B 在**锁外**的第 1 次检查看到非 null，**跳过 synchronized 直接 return**
- B 拿到一个"空壳对象"

---

## ✅ volatile 的两大作用

### 作用 1：**禁止指令重排**（DCL 里的主角）

加了 `volatile` 后，JVM 承诺：
- `new DCLPrinter()` 的三步**严格按 ① → ② → ③** 执行
- 只有 ② 完成后 ③ 才发生
- **所以"instance 非 null"永远等价于"对象已完整"**

```
volatile 修饰后的状态转换：
null → null → null → null → 【完整对象】
                              ↑
                       一步到位，没有"非 null 但半成品"中间态
```

### 作用 2：**保证可见性**（副产品）

volatile 字段的读写**直接走主内存，不经过 CPU 缓存**。

```java
// 没有 volatile
线程 A：修改 x = 5   → 可能只写到 A 的 CPU 缓存，主内存还是旧值
线程 B：读取 x       → 可能看到 B 自己缓存里的旧值

// 有 volatile
线程 A：修改 x = 5   → 立刻刷到主内存
线程 B：读取 x       → 直接从主内存读，看到最新
```

但在 DCL 场景下，**禁止重排是主要作用**，可见性是附赠。

---

## 🏠 门牌号比喻

- **没 volatile**：房子盖到一半，就有人挂门牌号 → 路人看到门牌 → 进去发现是毛坯
- **有 volatile**：房子**完全装修好**才挂门牌号 → 路人要么看到没门牌（房子还没好，等等），要么看到门牌（进去一定是精装好的）

> **volatile 把"挂门牌号"这个动作，锁死在"装修完成之后"。**

---

## 📐 synchronized 和 volatile 的分工

| 工具 | 管什么 |
|------|-------|
| **synchronized** | 同一时刻只有一个线程能执行临界区（互斥） |
| **volatile** | `new` 这一行内部不被重排；字段修改立刻可见 |

**DCL 里两者分工**：

```java
if (instance == null) {                 // volatile 保证看到的是明确状态
    synchronized (X.class) {             // synchronized 保证互斥
        if (instance == null) {
            instance = new X();          // volatile 保证这一行内部有序
        }
    }
}
```

**缺一不可**：
- 缺 synchronized → 两个线程可能同时 new（竞态）
- 缺 volatile → B 可能从锁外看到半成品对象（重排漏洞）

---

## ⚠️ volatile 能做的 vs 不能做的

### ✅ volatile 能做的
- 禁止指令重排（DCL 的核心）
- 保证 **单次读/写** 的可见性
- 适合"一写多读"或"状态标志位"

```java
// 经典用法：终止标志
volatile boolean stopped = false;

void run() {
    while (!stopped) {
        // 工作...
    }
}

// 另一个线程：
stopped = true;   // volatile 保证这个修改对工作线程立刻可见
```

### ❌ volatile 不能做的
- **不能保证复合操作的原子性**！

```java
volatile int count = 0;

count++;   // 这不是原子的！
// 实际上是：① 读 count  ② +1  ③ 写回
// 多线程下这三步可能交错 → 丢更新
```

**解决**：用 `AtomicInteger` 或 `synchronized`。

---

## 🎯 volatile 的使用场景判断

问自己两个问题：

1. **是否需要"一写多读"的可见性？** → 适合 volatile
2. **是否需要"读-改-写"的原子性？** → 不能用 volatile，必须 synchronized / Atomic 类

典型适用场景：
- 状态标志位（stopped、initialized）
- DCL 单例
- 发布不可变对象（某个字段一经赋值就不再变，但赋值动作要让其他线程看见）

---

## ⚠️ 常见误区

### 误区 1："volatile 能替代 synchronized"
**不能**。volatile **没有互斥能力**。

### 误区 2："volatile 保证线程安全"
**不一定**。只保证**可见性 + 禁重排**，不保证复合操作原子性。

### 误区 3："volatile 字段的 ++ 是安全的"
**绝对不是**！见上文。

### 误区 4："所有共享变量都加上 volatile 更安全"
**错**。volatile 有性能开销（禁止优化、刷主内存）。只在**真的需要**的地方加。

---

## 🔗 相关深入（后面会遇到）

- **JMM 内存模型（Java Memory Model）** —— volatile 背后的理论基础
- **happens-before 关系** —— 并发正确性的形式化描述
- **内存屏障（Memory Barrier）** —— volatile 底层的 CPU 指令
- **CAS + Atomic 类** —— 无锁并发的另一条路

---

## 📌 一句话总结

> **volatile = 禁止指令重排 + 保证单次读写可见性。DCL 离不开它，不然 B 线程可能从锁外偷看到"非 null 但半成品"的对象。**
