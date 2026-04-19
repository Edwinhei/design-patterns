# 第 27 课 · 读写锁 ReadWriteLock ★★

> 类型：并发模式 | 难度：★★ | **读多写少的性能利器**

## 🎯 本课目标

- [x] 理解"**读读并发 / 读写互斥 / 写写互斥**"
- [x] 掌握 Java `ReentrantReadWriteLock`
- [x] 知道 `StampedLock` / `ConcurrentHashMap` 的现代替代

---

## 🎬 场景：缓存系统

你的应用有个**内存缓存**：
- 📖 **99% 请求**：读缓存查数据
- ✏️ **1% 请求**：更新缓存

**特点**：**读远多于写**。

---

## 🤔 土办法：`synchronized` 全独占

```java
class NaiveCache {
    private final Map<String, String> data = new HashMap<>();

    public synchronized String get(String key) {     // 读也独占
        return data.get(key);
    }

    public synchronized void put(String key, String value) {
        data.put(key, value);
    }
}
```

跑 [code/BadReadWriteLock.java](code/BadReadWriteLock.java)。

**痛点**：
- 🙁 **读读也互斥** —— 两个线程同时 get 也要排队
- 🙁 99% 的读请求在**白白等**
- 🙁 并发性能很差

**观察**：读操作其实**没有冲突**（不改数据）—— 不应该互斥。

---

## 💡 读写锁模式登场

**核心思想**：**把锁拆成两把 —— 读锁和写锁**。

### 锁兼容表

| | 线程 A 拿读锁 | 线程 A 拿写锁 |
|--|-------------|--------------|
| **线程 B 想拿读锁** | ✅ 允许（并发）| ❌ 等待 |
| **线程 B 想拿写锁** | ❌ 等待 | ❌ 等待 |

**简单记忆**：**读读兼容，其他都互斥**。

### Java `ReentrantReadWriteLock` 用法

```java
class CachedMap {
    private final Map<String, String> data = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock  = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public String get(String key) {
        readLock.lock();
        try {
            return data.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public void put(String key, String value) {
        writeLock.lock();
        try {
            data.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }
}
```

**要点**：
- `readLock.lock()` 多个线程可**同时拿到**
- `writeLock.lock()` 只有**一个线程**能拿，且读锁被完全释放才能拿
- **必须用 `try-finally`** 保证解锁

跑 [code/ReadWriteLockDemo.java](code/ReadWriteLockDemo.java) 看性能对比。

---

## 📐 锁状态流转

```
初始状态
   │
   ▼
线程 A: 读锁 ◀━━━━━ 线程 B: 读锁 ← 多个并发读 ✅
   │
线程 C: 写锁 ← 等读锁全部释放才能拿
   │
   ▼（所有读锁释放后）
线程 C: 拿到写锁 ← 独占
   │
其他线程想读/写 ← 等写锁释放
```

**写锁"饥饿"问题**：如果读频繁 → 写锁可能长时间拿不到。`ReentrantReadWriteLock` 有**公平/非公平**模式可选。

---

## 🚀 `StampedLock`（Java 8+ · 升级版）

`ReentrantReadWriteLock` 有个问题：读操作也要获取锁（有开销）。

**`StampedLock` 提供"乐观读"** —— **不加锁直接读**，最后校验是否被写过：

```java
class OptimizedCache {
    private final StampedLock lock = new StampedLock();
    private Map<String, String> data = new HashMap<>();

    public String get(String key) {
        long stamp = lock.tryOptimisticRead();          // 乐观读
        String value = data.get(key);

        if (!lock.validate(stamp)) {                     // 期间有人写过？
            stamp = lock.readLock();                     // 降级为悲观读锁
            try {
                value = data.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return value;
    }

    public void put(String key, String value) {
        long stamp = lock.writeLock();
        try {
            data.put(key, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
```

**适合"读极多写极少"场景**，性能提升明显。

---

## 🎯 更好的选择：`ConcurrentHashMap`

**如果你的需求是"线程安全的 Map"**，直接用 `ConcurrentHashMap` 更好：

```java
Map<String, String> cache = new ConcurrentHashMap<>();

// 不用管锁
cache.get(key);
cache.put(key, value);
cache.computeIfAbsent(key, k -> loadFromDB(k));   // 原子操作
```

**`ConcurrentHashMap` 内部**用分段锁 + CAS + `synchronized` 单节点锁，性能远超 `ReadWriteLock`。

**工作中的实战选择**：
- Map 操作 → `ConcurrentHashMap`
- 不可变数据更新 → `AtomicReference` / `Copy-On-Write`
- 自定义共享数据结构的读写 → `ReadWriteLock` / `StampedLock`

---

## 🌍 真实应用

| 场景 | 用什么 |
|------|-------|
| **配置中心客户端**（本地缓存） | `ReadWriteLock` |
| **路由表**（网关） | `ReadWriteLock` / `CopyOnWriteArrayList` |
| **用户会话缓存** | `ConcurrentHashMap`（通常够用）|
| **字典 / 枚举加载** | `ReadWriteLock` 或不可变 + `AtomicReference` |
| **JDK ThreadLocalMap** | 特殊实现 |

---

## ⚠️ 什么时候别用

### 🚫 读写比接近 1:1
读写差不多频繁 → 读锁的开销 vs `synchronized` 差不多，甚至更差。

### 🚫 操作极简单
Map / Set / List 操作 → 用 `ConcurrentHashMap` / `CopyOnWriteArrayList`。

### 🚫 写操作极其频繁
写多的场景 `ReadWriteLock` 不如 `synchronized`。

---

## 🎭 锁升级 / 降级

### 锁降级（允许，推荐）
```java
writeLock.lock();
try {
    // 写操作
    readLock.lock();    // ← 在持有写锁时获取读锁（降级）
} finally {
    writeLock.unlock();  // 释放写锁，还持有读锁
}
```

### 锁升级（**禁止！**）
```java
readLock.lock();
try {
    // 读操作
    writeLock.lock();   // ❌ 死锁！
} finally { ... }
```

**`ReentrantReadWriteLock` 不支持读锁升级为写锁**（会死锁）。必须先释放读锁再获取写锁。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么读锁不能升级为写锁？（答：多个读锁持有者，都想升级 → 互相等 → 死锁）
2. `ConcurrentHashMap` 内部用读写锁吗？（答：不是。用分段锁 + CAS + synchronized 单节点锁，比读写锁更细粒度）
3. `StampedLock` 为什么比 `ReadWriteLock` 快？（答：乐观读不加锁，只在冲突时降级为悲观锁）

### 小练习

扩展 `ReadWriteLockDemo.java`：
- 用 5 个读线程 + 1 个写线程
- 统计读吞吐量（每秒读次数）
- 对比 synchronized 版 vs ReadWriteLock 版

---

## 🏁 学完后

- **"懂了，下一课"** → 第 28 课 · 双检锁 DCL（回到单例模式底层）
- **"ConcurrentHashMap 源码想深入"** → 可以专题
- **"先 commit"** → 我帮你

**读写锁是"读多写少"场景的利器**。但 `ConcurrentHashMap` 等并发容器通常更合适 📖
