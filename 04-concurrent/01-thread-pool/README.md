# 第 24 课 · 线程池 Thread Pool ★

> 类型：并发模式 | 难度：★ | **并发编程最基础的模式**

## 🎯 本课目标

- [x] 理解"**为什么不能每任务 new Thread**"
- [x] 掌握 Java `ExecutorService` 的基本用法
- [x] 知道 `ThreadPoolExecutor` **7 个核心参数**
- [x] 避开 `Executors` 工厂的经典坑

---

## 🎬 场景：Web 服务器

假设你写一个简单的 Web 服务器：

```
Client 1 ──▶ 服务器 ──▶ 处理请求（IO + 计算，耗时 100ms）
Client 2 ──▶ 服务器 ──▶ 处理请求
Client 3 ──▶ 服务器 ──▶ 处理请求
...（每秒上千个）
```

**并发处理 = 多线程**，但**每个请求 new 一个 Thread** 行吗？

---

## 🤔 土办法：每个请求 new Thread

```java
while (true) {
    Request req = server.accept();
    new Thread(() -> handle(req)).start();   // 🚨 每次 new
}
```

跑 [code/BadThreadPool.java](code/BadThreadPool.java)。

**痛点**：
- 🙁 **创建/销毁线程成本高**（OS 级操作）
- 🙁 **线程数量失控**：请求暴增 → 线程暴增 → 内存爆炸（每个线程栈约 1 MB）
- 🙁 **上下文切换频繁**：线程太多 CPU 忙切换，不干活
- 🙁 **无法复用**：请求结束线程就销毁，下次又要重新建

**传统 Web 服务器进程模型（如 Apache prefork）都会设置最大进程/线程数**，不是无限创建。

---

## 💡 线程池模式登场

**核心思想**：**预创建一批工人线程 + 任务队列，让线程循环取任务执行**。

```
                    ┌─────────┐
    任务 ──push──▶  │ 任务队列 │
                    └────┬────┘
                         │ poll
          ┌──────────────┼──────────────┐
          │              │              │
      ┌───▼───┐      ┌───▼───┐      ┌───▼───┐
      │线程 1 │      │线程 2 │      │线程 3 │   ← 线程池（预建好）
      └───────┘      └───────┘      └───────┘
         │              │              │
         └──── 循环取任务执行 ──────────┘
```

**优势**：
- ✅ **线程复用**（用完不销毁，继续取下个任务）
- ✅ **线程数可控**（OOM 压力可管理）
- ✅ **减少创建/销毁开销**

---

## 🔧 Java 的 `ExecutorService`

**Java 内置了完整的线程池**。用法极其简单：

```java
import java.util.concurrent.*;

// 创建 10 个线程的池
ExecutorService pool = Executors.newFixedThreadPool(10);

// 提交任务（Runnable）
pool.submit(() -> System.out.println("任务 1"));
pool.submit(() -> System.out.println("任务 2"));

// 用完关
pool.shutdown();
```

**就这几行**。

## 工厂方法快速选型

```java
// 1. 固定大小（最常用）
Executors.newFixedThreadPool(10);

// 2. 单线程（按序执行）
Executors.newSingleThreadExecutor();

// 3. 缓存型（任务多时扩容，少时回收）
Executors.newCachedThreadPool();

// 4. 定时任务池
Executors.newScheduledThreadPool(2);

// 5. 工作窃取池（Java 8+，并行计算）
Executors.newWorkStealingPool();
```

跑 [code/ThreadPoolDemo.java](code/ThreadPoolDemo.java) 看所有写法。

---

## 🧩 `ThreadPoolExecutor` 7 个核心参数

`Executors.newXxx()` 底层全是 `ThreadPoolExecutor`。**生产环境推荐直接 new**，7 个参数：

```java
new ThreadPoolExecutor(
    int corePoolSize,              // ① 核心线程数（常驻）
    int maximumPoolSize,           // ② 最大线程数
    long keepAliveTime,            // ③ 非核心线程闲置超时
    TimeUnit unit,                 // ④ 上面时间单位
    BlockingQueue<Runnable> workQueue,   // ⑤ 任务队列
    ThreadFactory threadFactory,   // ⑥ 线程工厂（命名等）
    RejectedExecutionHandler handler     // ⑦ 拒绝策略
);
```

### 新任务来了的执行流程

```
  来新任务
     │
     ▼
  核心线程满了吗？
     │
 未满 ──▶ 开新核心线程执行
     │
 满了
     ▼
  队列满了吗？
     │
 未满 ──▶ 放进队列等待
     │
 满了
     ▼
  最大线程达到了吗？
     │
 未达 ──▶ 开【非核心】线程执行
     │
 达到
     ▼
  **执行拒绝策略**（throw / 丢弃 / 让调用方自己跑...）
```

### 4 种内置拒绝策略

| 策略 | 行为 |
|------|------|
| `AbortPolicy`（默认） | 抛异常 |
| `DiscardPolicy` | 丢弃新任务 |
| `DiscardOldestPolicy` | 丢弃最老任务 |
| `CallerRunsPolicy` | 让**提交任务的线程自己跑**（反压）|

---

## ⚠️ Executors 工厂的**经典坑**

`Executors.newFixedThreadPool(n)` 长什么样？

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(
        nThreads, nThreads,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>()   // ⚠️ 无界队列
    );
}
```

**`LinkedBlockingQueue` 无界**！任务堆积**无限吃内存 → OOM**。

### 阿里 Java 开发手册的强制规定

> **【强制】线程池不允许使用 Executors 创建，推荐通过 ThreadPoolExecutor 手动创建**。
>
> 说明：Executors 返回的线程池对象的弊端如下：
> - `newFixedThreadPool` / `newSingleThreadExecutor`：队列长度 `Integer.MAX_VALUE` → 可能 OOM
> - `newCachedThreadPool` / `newScheduledThreadPool`：线程数 `Integer.MAX_VALUE` → 可能 OOM

**推荐手动构造**：

```java
new ThreadPoolExecutor(
    10,                              // 核心
    20,                              // 最大
    60, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(1000),  // 有界队列 👈
    Executors.defaultThreadFactory(),
    new ThreadPoolExecutor.CallerRunsPolicy()  // 合理拒绝策略
);
```

---

## 🎯 核心线程数怎么选？

两种典型场景：

### CPU 密集型任务
```
核心数 ≈ CPU 核心数
```
太多反而造成上下文切换浪费。

### I/O 密集型任务
```
核心数 ≈ CPU 核心数 × 2（或更高）
```
I/O 等待时线程让出 CPU，可以更多线程充分利用。

**经验公式**：
```
核心数 = CPU 核心数 × (1 + 等待时间 / 计算时间)
```

---

## 🌍 真实应用

线程池**无处不在**：

| 场景 | 线程池扮演 |
|------|----------|
| **Web 服务器**（Tomcat / Netty）| 工作线程池处理请求 |
| **数据库连接池** | 把 Runnable 想象成 SQL 任务 |
| **异步任务框架**（Spring `@Async`）| 线程池执行异步方法 |
| **批量处理**（ETL）| 多个线程并行处理数据 |
| **RPC 框架**（Dubbo / gRPC）| 服务端线程池 |
| **消息消费**（Kafka Consumer）| 消费者工作线程 |
| **定时任务**（Quartz）| 任务线程池 |

**凡是并发系统几乎都有线程池**。

---

## ⚠️ 什么时候别用

### 🚫 任务极少且一次性
写个 `main` 里跑一下，`new Thread` 也行。

### 🚫 对线程生命周期有强需求
比如需要线程**独占某资源**的场景，池化复用反而麻烦。

### 🚫 虚拟线程场景（Java 21+）
Java 21 的虚拟线程（Virtual Thread）极轻量，**可以每任务开一个**，这时不需要传统线程池。

---

## 🎁 Java 21 的 Virtual Thread —— 线程池的新对手

Java 21 引入**虚拟线程**，彻底改变了并发编程：

```java
// 老写法（线程池）
ExecutorService pool = Executors.newFixedThreadPool(200);
for (int i = 0; i < 10000; i++) {
    pool.submit(() -> handleRequest());
}

// 新写法（虚拟线程，每请求一个）
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10000; i++) {
        executor.submit(() -> handleRequest());
    }
}
```

**虚拟线程不用池化** —— JVM 能轻松创建百万级虚拟线程。

**这是并发编程的范式转变**（"**每任务一线程**"重新成为可行方案）。

但**传统线程池仍然是当前主流**。虚拟线程是未来，不是替代。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么 `Executors.newFixedThreadPool` 不推荐？（答：无界队列可能 OOM）
2. `corePoolSize = 10, maximumPoolSize = 20` 什么时候会开到 20 个线程？（答：核心已满 + 队列已满才会）
3. `CallerRunsPolicy` 拒绝策略为什么叫"反压"？（答：调用方自己跑 → 提交速度自然慢下来 → 给后端喘息机会）

### 小练习

**手动构造一个 ThreadPoolExecutor**

参数：
- 核心 5，最大 10
- 队列 ArrayBlockingQueue 容量 100
- 拒绝策略：CallerRunsPolicy

提交 20 个耗时任务，观察执行顺序。

---

## 🏁 学完后

- **"懂了，下一课"** → 第 25 课 · Future/Promise（异步获取结果）
- **"ThreadPoolExecutor 参数细节再讲"** → 可以深入
- **"先 commit"** → 我帮你

**线程池是并发模式里最基础、最常用的一个**。接下来的 Future / 生产者消费者 / 读写锁都建立在它之上 ⚙️
