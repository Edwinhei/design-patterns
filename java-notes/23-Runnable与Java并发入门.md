# Runnable · Java 并发入门

> 📍 来源：命令模式课提到"Runnable 是 Java 的命令"，用户不熟
> 🎯 本篇目标：从零讲清楚 `Runnable` 接口和 Java 并发编程的基础

---

## 🤔 场景问题

你看到这些代码会不会懵？

```java
new Thread(() -> doSomething()).start();

executor.submit(() -> computeResult());

CompletableFuture.runAsync(() -> sendEmail());
```

**`() -> doSomething()` 是什么？为什么能丢给 Thread 和 executor？**

本篇讲清楚。

---

## 🧠 核心结论

> **`Runnable` 是 JDK 提供的一个接口，只有一个 `run()` 方法**。
>
> **代表"一段可以被执行的代码"**（= 命令模式里的 Command）。
>
> **作用**：把"要执行的任务"和"执行任务的线程"**解耦**，让同一个任务可以被**不同的线程 / 线程池 / 异步框架**执行。

---

# 📋 Part 1：Runnable 接口

## 定义（超简单）

```java
package java.lang;

@FunctionalInterface
public interface Runnable {
    void run();        // 无参、无返回值、无异常
}
```

**就一个方法**，就这么简单。

## 为什么需要这个接口

Java 设计者需要"**一段能被 Thread 执行的代码**"这种抽象。

**没有 Runnable 的话**：你怎么把代码传给线程？

```java
// 不可能这样：
Thread t = new Thread(一段代码);    // 代码不是对象，传不进去
```

**有了 Runnable**：

```java
// 把代码塞进 run 方法 → 对象 → 可传递
Runnable task = new Runnable() {
    public void run() {
        System.out.println("要执行的代码");
    }
};

Thread t = new Thread(task);
t.start();
```

**Runnable 就是"代码对象化"的载体**。

## 三种写法演化

```java
// 写法 1：匿名类（Java 5+）
Runnable r1 = new Runnable() {
    public void run() {
        System.out.println("hi");
    }
};

// 写法 2：Lambda（Java 8+，推荐）
Runnable r2 = () -> System.out.println("hi");

// 写法 3：方法引用（最简洁）
Runnable r3 = System.out::println;   // 会调用 println() 无参版
```

**99% 的现代代码都用 Lambda 或方法引用**。

---

# 🆚 Part 2：Runnable vs Thread（容易混淆）

| | `Runnable` | `Thread` |
|--|-----------|---------|
| 本质 | **任务**（要做的事） | **执行者**（真正跑的线程）|
| 接口/类 | 接口 | 类 |
| 关注点 | "要做什么" | "怎么跑" |

## 生活类比：餐厅点单

- **`Runnable`** = **订单小票**（"做一份牛排"）
- **`Thread`** = **厨师**（按小票做菜）

**小票本身不会做菜，得有厨师按小票做**。

## 两种启动线程的写法

### ✅ 推荐：实现 Runnable

```java
Runnable task = () -> System.out.println("hi");
new Thread(task).start();
```

**好处**：
- 任务和执行者解耦
- 同一个任务可以交给**多个线程**执行
- 可以交给**线程池**

### ❌ 老派：继承 Thread（不推荐）

```java
new Thread() {
    public void run() {
        System.out.println("hi");
    }
}.start();
```

**问题**：占用单继承名额，任务和线程耦合。

---

# 🎬 Part 3：Runnable 的典型用法

## 用法 1：开新线程

```java
Runnable task = () -> {
    System.out.println("在线程 " + Thread.currentThread().getName());
};

new Thread(task).start();              // 输出：在线程 Thread-0
```

## 用法 2：线程池（最常用）

**直接 `new Thread()` 不推荐**：频繁创建/销毁线程开销大。**用线程池复用**：

```java
import java.util.concurrent.*;

ExecutorService pool = Executors.newFixedThreadPool(4);   // 4 个线程的池

pool.submit(() -> System.out.println("任务 1"));
pool.submit(() -> System.out.println("任务 2"));
pool.submit(() -> System.out.println("任务 3"));

pool.shutdown();   // 用完要关
```

**4 个线程并发执行 3 个任务**。

## 用法 3：定时任务

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

// 5 秒后执行
scheduler.schedule(
    () -> System.out.println("5 秒到了"),
    5, TimeUnit.SECONDS
);

// 每 1 秒执行一次
scheduler.scheduleAtFixedRate(
    () -> System.out.println("心跳"),
    0, 1, TimeUnit.SECONDS
);
```

## 用法 4：异步编程

```java
CompletableFuture.runAsync(() -> {
    System.out.println("异步执行");
});
```

---

# 🚀 Part 4：Runnable 的升级版 —— `Callable`

`Runnable` 有两个小缺陷：
- **没有返回值**（`void run()`）
- **不能抛受检异常**

Java 5 引入了 `Callable` 解决：

```java
public interface Callable<V> {
    V call() throws Exception;           // ← 有返回值 + 能抛异常
}
```

## 用法（需要 Future）

```java
ExecutorService pool = Executors.newFixedThreadPool(1);

Callable<Integer> task = () -> {
    Thread.sleep(1000);
    return 42;                           // 有返回值！
};

Future<Integer> future = pool.submit(task);

// 拿结果（会阻塞直到任务完成）
Integer result = future.get();
System.out.println("结果: " + result);   // 42
```

## Runnable vs Callable 选择

| 需求 | 用哪个 |
|------|-------|
| 不需要返回值 | `Runnable` |
| 需要返回值 | `Callable` |
| 需要异步获得结果 | `CompletableFuture`（现代推荐）|

---

# 🏊 Part 5：线程池入门

**永远不要 `new Thread()` 跑任务**。用**线程池**：

## `ExecutorService` 常见工厂方法

```java
// 固定大小线程池（常用）
ExecutorService pool1 = Executors.newFixedThreadPool(4);

// 缓存线程池（任务多时动态扩容）
ExecutorService pool2 = Executors.newCachedThreadPool();

// 单线程池（顺序执行）
ExecutorService pool3 = Executors.newSingleThreadExecutor();

// 定时任务池
ScheduledExecutorService pool4 = Executors.newScheduledThreadPool(2);

// 工作窃取池（Java 8+，并行计算）
ExecutorService pool5 = Executors.newWorkStealingPool();
```

## 生命周期

```java
ExecutorService pool = Executors.newFixedThreadPool(2);

// 提交任务（异步执行，立即返回）
pool.submit(() -> doSomething());
pool.execute(() -> doSomethingElse());

// 优雅关闭（拒绝新任务，等现有任务完成）
pool.shutdown();

// 或强制关闭
pool.shutdownNow();
```

## ⚠️ `Executors` 的坑

实际项目**不推荐用 `Executors.newXxx()`** —— 内部用了无界队列，可能 OOM。

**推荐直接 `new ThreadPoolExecutor(...)`** 自己指定参数（第 4 阶段并发模式课会详讲）。

---

# 🔗 Part 6：Runnable 和 Command 模式的关联

**Runnable 就是 Java 内置的 Command 接口**：

| Command 模式 | Runnable 世界 |
|-------------|-------------|
| Command 接口 | `Runnable` |
| ConcreteCommand | Lambda / 匿名类 / 实现类 |
| Receiver | Lambda 里调用的真对象 |
| Invoker | `Thread` / `ExecutorService` |
| Client | 你的 main 方法 |

**五个角色一一对应**。

所以当你用 Runnable：

```java
executor.submit(() -> {
    userService.save(user);         // Receiver 是 userService
});
```

你其实就在**用命令模式**。只是 Java 把 Command 接口命名为 "Runnable"。

---

# ⚠️ Part 7：常见误区

## 误区 1：`Runnable` = 线程
**错**。`Runnable` 是**任务**，`Thread` 才是线程。一个 Runnable 可以被多个线程执行。

## 误区 2：`new Thread()` 就能并发
**错**。`new Thread()` 只是**创建**线程对象，必须 `start()` 才会开新线程执行。

## 误区 3：调用 `t.run()` 会开新线程
**错**。**直接调 `run()` 就是普通方法调用**，在当前线程执行。**必须 `start()`**。

```java
Thread t = new Thread(() -> System.out.println("hi"));
t.run();     // ❌ 在当前线程跑，不是新线程
t.start();   // ✅ 新线程跑
```

## 误区 4：`Runnable` 不能有返回值就没用
**错**。`Runnable` 适合"只执行不返回"的任务（比如发邮件、存日志）。要返回值用 `Callable`。

## 误区 5：线程越多越快
**错**。线程太多会：
- 上下文切换开销大
- 内存爆炸（每个线程栈 ~1MB）
- 资源竞争

CPU 密集任务：线程数 ≈ CPU 核心数
I/O 密集任务：线程数可以更多（N × CPU 核心数）

---

# 🎁 Part 8：完整示例 —— 并发下载

```java
import java.util.concurrent.*;
import java.util.List;

public class ConcurrentDownloader {

    public static void main(String[] args) throws Exception {
        List<String> urls = List.of(
            "https://example.com/file1.jpg",
            "https://example.com/file2.jpg",
            "https://example.com/file3.jpg"
        );

        ExecutorService pool = Executors.newFixedThreadPool(3);

        long start = System.currentTimeMillis();

        // 为每个 URL 提交一个下载任务（Runnable）
        for (String url : urls) {
            pool.submit(() -> {
                System.out.println("⬇️  " + Thread.currentThread().getName()
                    + " 下载 " + url);
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                System.out.println("✅ " + url + " 完成");
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);     // 等所有任务完成

        long cost = System.currentTimeMillis() - start;
        System.out.println("总耗时: " + cost + "ms（并发）");
        // 串行要 3 秒，并发约 1 秒
    }
}
```

**输出**：
```
⬇️  pool-1-thread-1 下载 file1.jpg
⬇️  pool-1-thread-2 下载 file2.jpg
⬇️  pool-1-thread-3 下载 file3.jpg
✅ file1.jpg 完成
✅ file2.jpg 完成
✅ file3.jpg 完成
总耗时: 1015ms（并发）
```

**三个任务并发执行 → 耗时从 3 秒降到 1 秒**。

---

# 📚 相关笔记

- [22 Java Lambda 语法详解](./22-Java-Lambda语法详解.md) —— Runnable 是 Lambda 的完美舞台
- 设计模式 → [命令](../03-behavioral/06-command/) —— Runnable 就是 Java 内置的 Command
- 后续 → 第 4 阶段并发模式（Thread Pool / Future / Producer-Consumer）将深入展开

---

# 📌 一句话总结

> **`Runnable` = Java 提供的函数式接口，只有一个 `run()` 方法，代表"一段可执行代码"**。
>
> **典型用法**：丢给 `Thread` / 线程池 / 定时任务 / 异步框架执行。
>
> **现代写法**：Lambda 一行搞定 —— `new Thread(() -> {...}).start()`。
>
> **vs Thread**：Runnable 是**任务**（要做的事），Thread 是**执行者**（真正跑的）。
>
> **vs Callable**：Runnable 无返回值，Callable 有返回值 + 能抛异常。
>
> **本质**：**Java 内置的 Command 接口**，命令模式的典型应用。
