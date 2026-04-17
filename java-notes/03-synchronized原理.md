# synchronized 原理 · 互斥 + 可见性

> 📍 来源：单例 V3 双检锁 DCL
> 🎯 回答的核心问题："**synchronized 到底干了什么？为什么能让多线程安全？**"

---

## 🤔 场景问题

单例 V2 懒汉式不安全版：两个线程同时进 `getInstance()`，可能各自 new 一个，破坏单例。

V3 DCL 用 `synchronized` 解决：

```java
synchronized (DCLPrinter.class) {
    if (instance == null) {
        instance = new DCLPrinter();
    }
}
```

问题：`synchronized` 具体做了什么？

---

## 🧠 核心：synchronized 只做 2 件事

1. **互斥（Mutual Exclusion）**：同一时刻只允许 1 个线程进入被保护的代码
2. **可见性（Visibility）**：线程 A 释放锁前做的所有修改，线程 B 拿锁后**一定能看到**

---

## 🚽 公共厕所比喻

```
       ┌──────────────────┐
       │ 🚻 厕所门         │
       │                  │
       │  门口挂牌子：     │
       │  【没人】/【有人】 │
       └──────────────────┘
```

- `synchronized(x)` = 冲向 **x 这间厕所**
- 只有一把钥匙。谁先到谁拿到，拿到就把牌翻成【有人】
- 别的线程到门口看【有人】 → 在外面排队
- 拿到钥匙的线程进去执行**临界区代码**
- 执行完释放锁 → 把牌翻回【没人】 → 下一个等待的线程进

**关键**：钥匙绑在**对象**上。不同对象各有一把钥匙，互不干扰。

```java
synchronized (objA) { /* 拿 objA 的钥匙 */ }
synchronized (objB) { /* 拿 objB 的钥匙 */ }
// 两把钥匙互不干扰 = 两间厕所
```

---

## 📌 synchronized 的三种写法

```java
// ① 同步代码块（最灵活，推荐）
synchronized (this) {
    // ...
}

// ② 同步实例方法 —— 等价于 synchronized(this)
public synchronized void foo() { ... }

// ③ 同步静态方法 —— 等价于 synchronized(X.class)
public static synchronized void bar() { ... }
```

**DCL 里用的是 ① + 锁对象是 `DCLPrinter.class`** —— 静态字段属于类，所以锁的是 Class 对象。

---

## 🔬 JVM 底层：每个对象都有 Monitor

> Java 里**每个对象**都自带一个隐藏的 **Monitor（监视器）**。`synchronized(x)` 本质上就是"抢 x 的 Monitor"。

对应字节码（`javap -c` 可见）：

```
monitorenter   ← 进入 synchronized 块，抢 Monitor
 (临界区代码)
monitorexit    ← 退出 synchronized 块，释放 Monitor
```

JVM 保证：
- `monitorenter` 抢不到就**阻塞**（线程挂起）
- `monitorexit` 前，所有修改会被**刷回主内存**
- 下一个 `monitorenter` 成功时，会从主内存**重新读取**字段

这就是"可见性"的物理保证 —— A 写的数据，B 拿锁后一定看得到。

---

## 🚀 锁升级（Java 6+ 优化）

Java 6 之前 synchronized 被称为"重量级锁"（每次都要进内核态），性能差。
Java 6 做了大幅优化，锁会**按需升级**：

```
无锁 → 偏向锁 → 轻量级锁 → 重量级锁
 ↑       ↑          ↑          ↑
启动   单线程访问   线程交替    真的在激烈争抢
```

| 级别 | 场景 | 开销 |
|------|------|------|
| 偏向锁 | 一直只有一个线程访问 | 几乎免费 |
| 轻量级锁 | 两个线程交替访问，不真正冲突 | CAS，快 |
| 重量级锁 | 真的多个线程在抢 | 挂起线程，进内核 |

**结论**：现代 JVM 里 synchronized **并不慢**。老一辈说的"synchronized 性能差"是 Java 6 之前的事。

---

## 💡 回到 DCL：synchronized 在守护什么

```java
public static DCLPrinter getInstance() {
    if (instance == null) {                    // 🔸 在锁外（快速路径）
        synchronized (DCLPrinter.class) {       // 🔒 抢 DCLPrinter Class 的锁
            if (instance == null) {             // 🔸 在锁内（确认）
                instance = new DCLPrinter();    // 🔸 唯一能 new 的地方
            }
        }                                       // 🔓 释放锁
    }
    return instance;
}
```

**synchronized 在这里保证**：

1. **互斥**："造打印机"这个动作同一时刻只有一个线程做 → 不会 new 出两个
2. **可见性**：A 造完释放锁，B 拿锁时**一定能看到** `instance` 已经不是 null

没有 synchronized，两个线程会**真的同时**跑 `new DCLPrinter()`，单例失败。

> ⚠️ **但 synchronized 还不够** —— 还需要 `volatile` 防止"半初始化对象"泄露给锁外的 B。详见 [volatile 与指令重排](./04-volatile与指令重排.md)。

---

## ⚠️ 常见误区

### 误区 1：`synchronized` 锁的是"代码"
**不是**。锁的是**对象**。没有对象就没有锁。

### 误区 2：`synchronized(this)` 和 `synchronized(ClassName.class)` 是一回事
**不是**。`this` 是实例锁，每个对象有自己的锁；`ClassName.class` 是类锁，全程序就一把。

### 误区 3：方法上的 synchronized 保护的是整个方法
**是，但也只是 synchronized(this) 的语法糖**。看起来是"保护方法"，本质还是"抢对象的锁"。

### 误区 4："我加了 synchronized 就线程安全了"
**不一定**。还要看：
- 锁的粒度对不对（是否包住了所有共享状态的访问）
- 锁的对象对不对（不同对象的锁互相独立）
- 是否存在指令重排（需要 volatile 配合）

---

## 🎁 经典面试题

**Q: `synchronized` 修饰静态方法和修饰实例方法的区别？**

A:
- 修饰静态方法 → 锁 `ClassName.class`（全程序一把）
- 修饰实例方法 → 锁 `this`（每个对象一把）
- **两把锁互相独立**，一个线程在执行 `synchronized static foo()`，另一个线程可以同时执行 `synchronized void bar()`，互不阻塞

---

## 🔗 相关深入（以后用到再看）

- **ReentrantLock** —— 更灵活的显式锁（可中断、可超时、可 Condition）
- **读写锁 ReentrantReadWriteLock** —— 读多写少的优化
- **StampedLock** —— 乐观读锁
- **JMM 内存模型 + happens-before 关系** —— 并发正确性的理论基础

---

## 📌 一句话总结

> **synchronized = 对象级别的互斥锁 + 内存屏障（保可见性）。进锁阻塞，出锁刷新主内存。配合 volatile 才能完整保证 DCL 的正确性。**
