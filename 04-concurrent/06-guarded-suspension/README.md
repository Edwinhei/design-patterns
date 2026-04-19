# 第 29 课 · Guarded Suspension · 保护性暂停 ★★

> 类型：并发模式 | 难度：★★ | **并发模式收官** 🎊

## 🎯 本课目标

- [x] 理解"**条件不满足就挂起，满足再唤醒**"
- [x] 掌握 Java 的 **`wait / notify / notifyAll`** 和 **`Condition`**
- [x] 看清这个模式是 `BlockingQueue` 的底层

---

## 🎬 场景：仓库发货

一个仓库 + 几个工人：

- 📦 **发货工** 来仓库拿货 → **仓库有货** 才能拿，**没货** 就等
- 🚚 **进货工** 把新货送来 → **喊一声** 通知发货工

**关键词**：**等待 + 唤醒**。

这就是 **Guarded Suspension（保护性暂停）** —— **"暂停执行直到守护条件满足"**。

---

## 🤔 土办法：忙等待（Busy Wait）

```java
class Warehouse {
    private final List<Item> items = new ArrayList<>();

    public Item take() {
        while (items.isEmpty()) {       // 🚨 疯狂循环查
            // 什么都不做
        }
        return items.remove(0);
    }

    public void put(Item item) {
        items.add(item);
    }
}
```

**痛点**：
- 🙁 **CPU 100%**（线程一直在空转）
- 🙁 **线程不安全**（没有锁）
- 🙁 唤醒延迟（依赖 CPU 调度）

---

## 💡 Guarded Suspension 模式登场

**核心思想**：用 **`synchronized` + `wait / notify`**：
- 条件不满足 → **`wait()` 挂起**（释放锁，让 CPU）
- 条件变化 → **`notify()` / `notifyAll()` 唤醒**

```java
class Warehouse {
    private final List<Item> items = new ArrayList<>();

    public synchronized Item take() throws InterruptedException {
        while (items.isEmpty()) {      // ← 关键：必须 while 不是 if
            wait();                     // 释放锁 + 挂起，等通知
        }
        return items.remove(0);
    }

    public synchronized void put(Item item) {
        items.add(item);
        notifyAll();                    // 通知所有等待的线程
    }
}
```

**4 个关键要点**：

| 要素 | 作用 |
|------|------|
| `synchronized` | wait/notify **必须在同步块里**（操纵的是对象的 monitor）|
| `while` 循环 | 防**虚假唤醒**（spurious wakeup）|
| `wait()` | 释放锁 + 挂起 |
| `notifyAll()` | 唤醒所有等待的线程 |

### 为什么是 `while` 不是 `if`

```java
while (items.isEmpty()) {        // ✅ 正确
    wait();
}
```

```java
if (items.isEmpty()) {           // 🚨 错误
    wait();
}
```

**原因**：
1. **多个线程在 wait** —— 被 `notifyAll` 唤醒时只有一个能继续，其他需要重新检查条件
2. **虚假唤醒**（JVM 偶尔会无故唤醒 wait 的线程）—— 必须复核条件

**铁律**：**`wait` 必须放在 `while` 循环里**，**永远不要用 `if`**。

---

## 🎬 完整示例：多生产者 + 多消费者

```java
import java.util.*;

class Warehouse {
    private final List<String> items = new ArrayList<>();
    private final int maxCapacity = 5;

    // 消费：仓库空了就等
    public synchronized String take() throws InterruptedException {
        while (items.isEmpty()) {
            System.out.println(Thread.currentThread().getName() + " 等货...");
            wait();
        }
        String item = items.remove(0);
        notifyAll();                    // 通知等待的生产者（仓库有空位了）
        return item;
    }

    // 生产：仓库满了就等
    public synchronized void put(String item) throws InterruptedException {
        while (items.size() >= maxCapacity) {
            System.out.println(Thread.currentThread().getName() + " 等空位...");
            wait();
        }
        items.add(item);
        notifyAll();                    // 通知等待的消费者（来货了）
    }
}

public class Demo {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();

        // 2 个生产者
        for (int i = 1; i <= 2; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < 5; j++) {
                        warehouse.put("货#" + id + "-" + j);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {}
            }, "P" + i).start();
        }

        // 3 个消费者
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < 3; j++) {
                        String item = warehouse.take();
                        System.out.println("C" + id + " 取到 " + item);
                        Thread.sleep(150);
                    }
                } catch (InterruptedException e) {}
            }, "C" + i).start();
        }
    }
}
```

**效果**：
- 仓库空 → 消费者等
- 仓库满 → 生产者等
- 双向唤醒

---

## 🚀 升级版：`ReentrantLock` + `Condition`

`synchronized + wait/notify` 是**老派 JDK 原语**。**Java 5+ 推荐 `Condition`**：

```java
import java.util.concurrent.locks.*;

class BetterWarehouse {
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();    // ← 多个条件变量
    private final Condition notFull  = lock.newCondition();

    private final List<String> items = new ArrayList<>();
    private final int max = 5;

    public String take() throws InterruptedException {
        lock.lock();
        try {
            while (items.isEmpty()) {
                notEmpty.await();              // 等 "非空" 条件
            }
            String item = items.remove(0);
            notFull.signalAll();                // 通知 "有空位"
            return item;
        } finally {
            lock.unlock();
        }
    }

    public void put(String item) throws InterruptedException {
        lock.lock();
        try {
            while (items.size() >= max) {
                notFull.await();                // 等 "有空位" 条件
            }
            items.add(item);
            notEmpty.signalAll();               // 通知 "非空"
        } finally {
            lock.unlock();
        }
    }
}
```

**优势**：
- ✅ 可以有**多个条件变量**（notEmpty / notFull 分开）→ 唤醒更精准
- ✅ 支持可中断等待 / 超时等待
- ✅ 支持公平锁选项

---

## 🔀 和 `BlockingQueue` 的关系

这一整套逻辑 **Java 已经封装好了** —— 就是 [第 26 课生产者-消费者](../03-producer-consumer/) 里讲的 `BlockingQueue`。

```java
// 你自己写 Guarded Suspension（底层）
synchronized (obj) {
    while (!condition) obj.wait();
}

// JDK 帮你封装好（推荐日常用）
BlockingQueue<Item> queue = new LinkedBlockingQueue<>();
queue.take();   // 空就自动等
queue.put(x);   // 满就自动等
```

**`BlockingQueue` 内部就是 `Condition + await/signal`**。

**实战选择**：
- 日常业务 → 直接用 `BlockingQueue`
- 复杂自定义条件 → 用 `Lock + Condition`
- 老代码维护 → 可能见到 `synchronized + wait/notify`

---

## 🧭 `wait/notify` 易错点

### 错误 1：**忘记同步块**
```java
obj.wait();      // ❌ IllegalMonitorStateException
```
必须在 `synchronized(obj) { ... }` 里。

### 错误 2：**用 `if` 不用 `while`**
```java
if (!ready) {
    wait();      // 🚨 醒来后条件可能已不满足（虚假唤醒 / 多消费者）
}
```

### 错误 3：`notify()` vs `notifyAll()`
- `notify()` → 只唤醒一个（可能唤醒错目标）
- `notifyAll()` → 唤醒所有（更安全）

**大多数场景用 `notifyAll()`**。

### 错误 4：忘记 `notify`
线程 wait 了没人唤醒 → **永久阻塞**（死等）。

---

## 🌍 真实应用

| 场景 | Guarded Suspension 体现 |
|------|-----------------------|
| **`BlockingQueue`** | take / put 的核心 |
| **`CountDownLatch`** | `await()` 等到 count 归零 |
| **`CyclicBarrier`** | 等所有线程到达栅栏 |
| **`Semaphore`** | 获取许可前等待 |
| **数据库连接池** | 池空时等待 |
| **Future.get()** | 结果未就绪时等待 |
| **线程池** | 工作线程没任务时 take 阻塞 |

**Java 并发工具类底层几乎全在用它**。

---

## ⚠️ 什么时候别用

### 🚫 有现成的高级工具
用 `BlockingQueue` / `CountDownLatch` / `Semaphore` 等更稳更省事。

### 🚫 条件简单（只是等一个标志位）
用 `volatile` + 自旋 + `LockSupport.park()` 可能更合适。

### 🚫 跨进程 / 分布式场景
用 Redis 分布式锁 / ZooKeeper 等，Java 的 wait/notify 只管单进程。

---

## 📝 思考题

1. **为什么 `wait` 必须释放锁**？（答：否则死锁。A 持锁 wait，等待 B notify，但 B 拿不到锁无法 notify）
2. **`notify` 会释放锁吗**？（答：不会。被唤醒的线程还要等当前线程的 synchronized 块执行完）
3. **虚假唤醒何时发生**？（答：JVM/OS 调度偶发。所以永远 `while` 判断，不要 `if`）

---

## 🏁 并发模式收官 🎊

```
04-concurrent/ (6/6) ✅
├── 01-thread-pool              线程池
├── 02-future-promise           Future / CompletableFuture
├── 03-producer-consumer        生产者-消费者
├── 04-read-write-lock          读写锁
├── 05-double-checked-locking   双检锁
└── 06-guarded-suspension       保护性暂停 ← 今天
```

**学完 6 种并发模式，你对"多线程协作"的基础套路都熟悉了**：
- 任务排队 → 线程池
- 异步结果 → Future
- 队列解耦 → P-C
- 读多写少 → 读写锁
- 懒加载 → DCL
- 条件等待 → Guarded Suspension

---

## 🧭 下一阶段

```
✅ 第 1 阶段 · 创建型（5）
✅ 第 2 阶段 · 结构型（7）
✅ 第 3 阶段 · 行为型（11）
✅ 第 4 阶段 · 并发（6）         ← 今天收官
⏳ 第 5 阶段 · 数据访问（6）
⏳ 第 6 阶段 · 企业应用（6）
⏳ 第 7 阶段 · DDD（5）
⏳ 第 8 阶段 · EIP 集成（5）
⏳ 第 9 阶段 · 函数式反应式（5）
⏳ 第 10 阶段 · 云原生（8）
⏳ 第 11 阶段 · 架构（5）
```

**29 / 70 完成，41%。前 4 个阶段（GoF + 并发）= 打牢基础**。后续 7 个阶段进入**工程实战**领域。

---

## 🙋 下一步

- **"懂了，下一课"** → 第 30 课 · 第 5 阶段 · DAO（数据访问模式第一课）
- **"先 commit 收官 + 推一批"** → 4-5-6 并发模式一起推
- **"回顾一下 GoF + 并发"** → 可以做个阶段性总结
