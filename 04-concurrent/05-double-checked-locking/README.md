# 第 28 课 · 双检锁 DCL (Double-Checked Locking) ★★

> 类型：并发模式 | 难度：★★ | **通用的"延迟初始化"方案**

## 🎯 本课目标

- [x] 理解 DCL 不只是单例的，是**通用的懒加载并发模式**
- [x] 再次巩固 `volatile` 为什么不能省
- [x] 看清各种延迟初始化方案的**选型**

---

## 🧭 上下文

**第 1 课 单例模式** 讲过 DCL 的细节（回忆）：
- 外层 `if (null)` 快路径无锁
- `synchronized` 加锁
- 内层 `if (null)` 双检
- `volatile` 防指令重排

这一课**把 DCL 作为通用模式看待** —— 不止用于单例，**任何"只初始化一次的共享资源"**都能用。

---

## 🎬 场景：懒加载大对象

你的应用启动时，**不一定用到**所有资源：

- 💾 大配置文件（100MB）
- 🔌 数据库连接池（启动要几秒）
- 🧠 机器学习模型（加载慢）

**如果启动时都加载** → 启动时间长 + 内存占用大
**如果懒加载** → 第一次用到才初始化

多线程下怎么**只初始化一次**？—— **DCL**。

---

## 🤔 三种错误/低效方案

### 方案 A：没加锁（多线程可能创建多次）

```java
private Config config;

public Config getConfig() {
    if (config == null) {
        config = loadFromDisk();      // 🚨 两个线程同时进来都 load
    }
    return config;
}
```

**后果**：多份大对象在内存里，浪费 + 状态不一致。

### 方案 B：`synchronized` 全方法（每次都加锁）

```java
public synchronized Config getConfig() {
    if (config == null) {
        config = loadFromDisk();
    }
    return config;
}
```

**后果**：之后**每次 get 都加锁**，99.99% 情况是没必要的。性能差。

### 方案 C：DCL 但缺 `volatile`（半成品陷阱）

```java
private Config config;    // 🚨 没有 volatile

public Config getConfig() {
    if (config == null) {
        synchronized (this) {
            if (config == null) {
                config = loadFromDisk();
            }
        }
    }
    return config;
}
```

**后果**：指令重排导致另一线程看到**非 null 但未初始化完**的对象 → 用起来 NPE/脏数据。

跑 [code/BadDCL.java](code/BadDCL.java) 看三种方案的现象。

---

## 💡 DCL 正确姿势

```java
private volatile Config config;      // ← volatile 不能省

public Config getConfig() {
    if (config == null) {                         // 第一次检查（快路径，无锁）
        synchronized (this) {
            if (config == null) {                 // 第二次检查（加锁后）
                config = loadFromDisk();
            }
        }
    }
    return config;
}
```

**4 个关键要素**：

| 要素 | 作用 |
|------|------|
| **第一次 `if`** | **快路径**：已初始化时无锁直接返回（最常见场景）|
| **`synchronized`** | 加锁，同一时刻只有一个线程 load |
| **第二次 `if`** | 加锁后再检查，防多次初始化 |
| **`volatile`** | 禁止 `new` 的指令重排，保证其他线程看到的是完整对象 |

### `volatile` 再次解析

`config = loadFromDisk()` 底层三步：
1. 分配内存
2. 执行 `loadFromDisk` 构造对象
3. `config` 指向这块内存

**JVM 可能重排为 ① → ③ → ②**（先让 config 指向空对象，再构造）。
线程 A 做完 ③ 还没做 ② 时，线程 B 在**锁外第一次 if** 看到 config 非 null → **拿到半成品**。

**`volatile` 禁止这种重排**，保证 `config != null` ⇔ 对象已初始化完整。

详细解析回看 [java-notes/04 volatile 与指令重排](../../java-notes/04-volatile与指令重排.md)。

跑 [code/DCLDemo.java](code/DCLDemo.java)。

---

## 🎯 DCL 通用模板

不只是单例，**任何"懒加载共享资源"**都能用：

```java
class LazyResource<T> {
    private volatile T resource;
    private final Supplier<T> loader;

    public LazyResource(Supplier<T> loader) {
        this.loader = loader;
    }

    public T get() {
        T r = resource;
        if (r == null) {
            synchronized (this) {
                r = resource;
                if (r == null) {
                    r = loader.get();
                    resource = r;
                }
            }
        }
        return r;
    }
}

// 使用
LazyResource<Config> lazy = new LazyResource<>(() -> loadFromDisk());
Config c = lazy.get();         // 第一次才加载
```

---

## 🔀 延迟初始化方案对比

| 方案 | 代码复杂度 | 性能 | 适用场景 |
|------|---------|------|--------|
| **饿汉式**（启动就 new）| ★ 简单 | 启动慢 | 一定会用到 + 初始化快 |
| **synchronized 方法**（懒汉线程安全）| ★ 简单 | 差 | 很少用到 |
| **DCL + volatile**（本课）| ★★★ 中等 | 好 | 懒加载 + 性能敏感 |
| **静态内部类 Holder**（单例限定）| ★★ 简单 | 好 | **只适合 static 单例** |
| **`AtomicReference` + CAS** | ★★★★ 复杂 | 好（某些场景可能 load 多次）| 高级场景 |
| **Java 9+ `VarHandle`** | ★★★★ 复杂 | 最好 | 框架作者 |

**日常选择**：
- 全局单例 → **静态内部类 Holder**（最简洁）
- 实例级懒加载 → **DCL + volatile**
- 纯 JDK → **`Supplier` + `Lazy`**（自己封装）
- 第三方库 → Guava `Suppliers.memoize`

---

## 🎁 Guava 的 `Suppliers.memoize` —— 优雅替代

```java
import com.google.common.base.Suppliers;

Supplier<Config> lazyConfig = Suppliers.memoize(() -> loadFromDisk());

Config c = lazyConfig.get();      // 第一次 load，之后缓存
```

**Guava 内部实现就是 DCL + volatile**。业务代码不用自己写。

---

## 🌍 真实应用

| 场景 | DCL 体现 |
|------|---------|
| **单例模式**（第 1 课）| 最经典的 DCL 应用 |
| **Spring `@Lazy`** | 容器懒初始化 Bean |
| **Guava Suppliers.memoize** | 线程安全的缓存计算 |
| **HotSpot 内部** | JIT 编译后的优化路径 |
| **JDK 的 `Class.forName`** | 类懒加载 |

---

## ⚠️ 什么时候别用

### 🚫 资源一定会用到
饿汉式更简单，没必要 DCL。

### 🚫 只有 static 单例
静态内部类 Holder 更简洁（依赖 JVM 保证，无需锁）。

### 🚫 对象会变化
DCL 是"**初始化一次**"模式。如果对象要更新，用其他同步机制。

---

## 📝 思考题

1. DCL 的 `volatile` 可以换成 `synchronized` 吗？（答：不能完全替代。synchronized 只保证临界区互斥，不保证 `new` 的指令不重排）
2. 为什么用静态内部类 Holder 不需要 volatile？（答：类加载由 JVM 保证原子性 + 线程安全，没有 `new` 暴露中间态的机会）
3. `AtomicReference` 为什么可能 load 多次？（答：CAS 重试时可能执行多次 loader，而 synchronized 保证只执行一次）

---

## 🏁 学完后

- **"懂了，下一课"** → 第 29 课 · 保护性暂停 Guarded Suspension（并发模式最后一课）
- **"各种懒加载方案再对比"** → 我可以深入
- **"先 commit"** → 我帮你

**DCL 是"延迟初始化 + 高性能"的标准答案**。懂了它，单例/懒加载/资源管理全通 ⚙️
