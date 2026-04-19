# 第 26 课 · 生产者-消费者 Producer-Consumer ★

> 类型：并发模式 | 难度：★ | **消息队列 / 异步处理的基石**

## 🎯 本课目标

- [x] 理解"**共享队列解耦生产和消费**"
- [x] 掌握 Java `BlockingQueue` 用法
- [x] 识别 Kafka / 线程池 / 日志框架里的 P-C 身影

---

## 🎬 场景：餐厅传菜窗口

想象餐厅的流程：

```
  厨师甲 ──┐
  厨师乙 ──┤──▶ 🪟 传菜窗口 ──▶ 服务员甲 ──▶ 客人
  厨师丙 ──┘      (队列)        服务员乙
```

- **厨师**（生产者）做好菜放到窗口
- **窗口**（队列）缓冲
- **服务员**（消费者）从窗口取菜送餐

**关键**：
- 厨师**不需要知道哪个服务员接菜**
- 服务员**不需要知道谁做的菜**
- 窗口满了 → 厨师等等
- 窗口空了 → 服务员等等

---

## 🤔 土办法：生产者直接调消费者

```java
class Order {
    void place() {
        orderHandler.handle(this);     // 🚨 下单时同步处理
    }
}
```

**痛点**：
- 🙁 下单接口**等处理完才返回** → 用户等
- 🙁 处理慢 → 下单也慢
- 🙁 生产者和消费者**紧耦合**
- 🙁 无法**多消费者并行**

---

## 💡 生产者-消费者模式登场

**核心思想**：**共享一个队列，生产者 put，消费者 take，互不等对方**。

```
 Producer 1 ──┐             ┌──▶ Consumer 1
 Producer 2 ──┤──▶ Queue ──┤──▶ Consumer 2
 Producer 3 ──┘             └──▶ Consumer 3
```

### Java 内置的 `BlockingQueue`

Java 不用自己造轮子，直接用 `java.util.concurrent.BlockingQueue`：

```java
BlockingQueue<Order> queue = new LinkedBlockingQueue<>(100);    // 容量 100

// 生产者
queue.put(order);        // 放。队列满就阻塞

// 消费者
Order o = queue.take();  // 取。队列空就阻塞
```

**两个关键方法**：
- `put(e)`：放，**满了自动等**
- `take()`：取，**空了自动等**

### 带超时的安全版

```java
queue.offer(order, 5, TimeUnit.SECONDS);   // 放不进就返回 false（不会阻塞死）
queue.poll(5, TimeUnit.SECONDS);            // 取不到返回 null
```

跑 [code/ProducerConsumerDemo.java](code/ProducerConsumerDemo.java) 看完整演示。

---

## 📐 完整架构图

```
【生产者】       【队列（缓冲）】        【消费者】
   ↓                 ↓                     ↓
  put()         [o1][o2][o3]...         take()
   │               │  │                    │
   │               ▼  ▼                    │
   │           队列满时 put 阻塞            │
   │           队列空时 take 阻塞           │
   ▼                                       ▼
 不关心谁消费                           不关心谁生产
```

**生产者和消费者完全解耦**。

---

## 🔧 `BlockingQueue` 家族

| 实现 | 特点 | 典型用途 |
|------|------|--------|
| **`ArrayBlockingQueue`** | 有界、数组实现 | 固定容量队列 |
| **`LinkedBlockingQueue`** | 链表实现，默认无界（谨慎！）| 可变大小队列 |
| **`PriorityBlockingQueue`** | 按优先级取 | 优先级任务调度 |
| **`SynchronousQueue`** | **0 容量**，put 必须等 take | 直接交接（Executors.newCachedThreadPool 用它）|
| **`DelayQueue`** | 延迟队列，到时间才能 take | 定时任务 |
| **`LinkedTransferQueue`** | 高级版，支持 transfer | 高性能场景 |

---

## 🌍 Producer-Consumer 在哪里？

**几乎所有"异步 / 队列 / 流处理"的系统都是它**：

| 场景 | 生产者 | 队列 | 消费者 |
|------|-------|------|-------|
| **线程池** | 提交任务代码 | `ThreadPoolExecutor` 内部队列 | 工作线程 |
| **Kafka** | Producer | Topic Partition | Consumer |
| **日志框架** | 业务代码 | 异步 Appender 队列 | 写盘线程 |
| **RxJava / Reactor** | Observable / Flux | Subject / Sink | Subscriber |
| **Node.js EventEmitter** | emit | 事件队列 | on handler |
| **Go channel** | `ch <- value` | channel | `<- ch` |

**异步编程的灵魂就是 Producer-Consumer**。

---

## 🎯 为什么"队列"这个中间层这么重要

### 解耦
生产/消费互不知道对方存在。

### 缓冲
生产速度波动时，队列能**吸收突发流量**。

### 限流（背压）
队列满 → 生产者自然慢下来（反压机制）。

### 可伸缩
生产者 / 消费者数量可以**独立调整**（加消费者即可扩容）。

### 持久化（进阶）
Kafka 这种把队列**持久化到磁盘**，即使消费者挂了消息不丢。

---

## ⚠️ 什么时候别用

### 🚫 同步处理足够
简单接口调用，搞队列反而复杂。

### 🚫 实时性极高
走队列有延迟。

### 🚫 不能丢消息 + 无持久化方案
内存队列一挂消息丢。要保证不丢 → 用 Kafka / RabbitMQ 等持久化队列。

---

## 🎁 线程池本身就是 P-C

```java
ExecutorService pool = new ThreadPoolExecutor(
    2, 4, 60, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(10)    // ← 这就是 P-C 的队列
);

pool.submit(task);    // submit = put
// 工作线程内部：while (true) queue.take().run();
```

**`submit()` 是生产**，**工作线程 `take().run()` 是消费**。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么 `LinkedBlockingQueue` 的"无界"容易 OOM？（答：`Executors.newFixedThreadPool` 默认用它，突发流量把内存干爆）
2. 生产者比消费者快很多怎么办？（答：加消费者 / 增大队列 / 加限流 / 用持久化队列）
3. Kafka 为什么要持久化消息？（答：消费者挂了不丢，可重放）

### 小练习

基于 `ProducerConsumerDemo.java`：
- 把 `LinkedBlockingQueue<>(100)` 改成 `ArrayBlockingQueue<>(5)`
- 观察队列满时的行为（生产者会被阻塞）

---

## 🏁 学完后

- **"懂了，下一课"** → 第 27 课 · 读写锁 ReadWriteLock
- **"想深入 Kafka / RabbitMQ"** → 到 EIP 阶段讲
- **"先 commit"** → 我帮你

**Producer-Consumer 是现代并发系统的基石**。从线程池到消息队列，从反应式流到 Go channel，全是它 📬
