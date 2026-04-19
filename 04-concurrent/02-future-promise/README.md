# 第 25 课 · Future / Promise ★★

> 类型：并发模式 | 难度：★★ | **异步编程的基础**

## 🎯 本课目标

- [x] 理解"**提交任务拿凭证，以后取结果**"
- [x] 掌握 `Future` 和 `CompletableFuture` 的基本用法
- [x] 能把**串行调用**重构成**并发调用**

---

## 🎬 场景：订单详情页

你打开淘宝订单详情，页面要显示：
- 👤 用户信息（调用户服务，1s）
- 📦 商品信息（调商品服务，1s）
- 🎫 优惠券信息（调优惠服务，1s）

**三个数据互不依赖**，能不能并发取？

---

## 🤔 土办法：串行同步调用

```java
UserInfo user    = fetchUser();       // 等 1s
Product  product = fetchProduct();    // 再等 1s
Coupon   coupon  = fetchCoupon();     // 再等 1s

return new OrderDetail(user, product, coupon);
// 总耗时：3 秒
```

跑 [code/BadFuture.java](code/BadFuture.java)。

**痛点**：
- 🙁 三个接口**等总时间 = 单次耗时之和**（3s）
- 🙁 用户干等页面加载
- 🙁 CPU 大部分时间闲着（等网络 I/O）

---

## 💡 Future 模式登场（异步凭证）

**生活类比**：
- 去咖啡店点餐 → 拿**号牌小票**
- 不用干等 → 做别的事（玩手机）
- 咖啡好了凭票取 → 拿咖啡

**`Future` 就是那张"小票"**：
- 提交任务时**立刻返回**一个 `Future` 对象（凭证）
- 任务在**后台线程**执行
- 需要结果时**调 `.get()`** 取（如果还没好会等）

### 代码

```java
ExecutorService pool = Executors.newFixedThreadPool(3);

// 立刻返回 Future（任务进入后台执行）
Future<UserInfo> userF    = pool.submit(this::fetchUser);
Future<Product>  productF = pool.submit(this::fetchProduct);
Future<Coupon>   couponF  = pool.submit(this::fetchCoupon);

// 三个任务并发跑着 —— 主线程只是等最慢那个
UserInfo user    = userF.get();        // 需要时才等
Product  product = productF.get();
Coupon   coupon  = couponF.get();

// 总耗时：max(1s, 1s, 1s) ≈ 1s
```

**耗时从 3s 降到 1s**。并发威力直接体现。

跑 [code/FutureDemo.java](code/FutureDemo.java) 看实测对比。

---

## 🚀 `CompletableFuture`（Java 8+ · 现代异步）

`Future` 有个缺陷：**`.get()` 会阻塞**。
如果想"任务完成后自动执行下一步"，用 **`CompletableFuture`**。

### 基本用法

```java
CompletableFuture<UserInfo> userF = CompletableFuture.supplyAsync(
    () -> fetchUser(), pool
);

// 链式组合：拿到 user 后加工
userF.thenApply(u -> u.getName())          // 转换：UserInfo → String
     .thenAccept(name -> print(name))      // 消费：无返回值
     .exceptionally(e -> {                   // 异常处理
         log.error(e);
         return null;
     });
```

### 最强威力：**合并多个异步任务**

```java
CompletableFuture<UserInfo> userF    = CompletableFuture.supplyAsync(this::fetchUser, pool);
CompletableFuture<Product>  productF = CompletableFuture.supplyAsync(this::fetchProduct, pool);
CompletableFuture<Coupon>   couponF  = CompletableFuture.supplyAsync(this::fetchCoupon, pool);

// 等所有完成（不阻塞单个）
CompletableFuture<OrderDetail> resultF = userF
    .thenCombine(productF, (u, p) -> new Partial(u, p))
    .thenCombine(couponF, (partial, c) -> new OrderDetail(partial, c));

OrderDetail detail = resultF.get();
```

**三个请求真并发，取最慢那个的时间**。

---

## 📋 `CompletableFuture` 常用 API 速查

| 方法 | 作用 |
|------|------|
| `supplyAsync(Supplier)` | 异步执行有返回值的任务 |
| `runAsync(Runnable)` | 异步执行无返回值 |
| `thenApply(Function)` | 转换结果（同步）|
| `thenApplyAsync(Function)` | 转换结果（放另一个线程）|
| `thenAccept(Consumer)` | 消费结果，无返回 |
| `thenRun(Runnable)` | 完成后执行一段代码 |
| `thenCompose(Function)` | 串行：前任务完成，拿结果启动下一个异步任务（flatMap）|
| `thenCombine(CF, BiFunction)` | 并行：合并两个任务的结果 |
| `allOf(CF...)` | 等所有任务完成 |
| `anyOf(CF...)` | 任一任务完成就返回 |
| `exceptionally(Function)` | 异常处理 |
| `handle(BiFunction)` | 同时处理成功和失败 |
| `get()` / `join()` | 阻塞取结果（get 抛受检，join 抛非受检） |

### `thenApply` vs `thenCompose` 辨析

```java
// thenApply 转换成普通值
CompletableFuture<String> f1 = userF.thenApply(u -> u.getName());

// thenCompose 转换成另一个 Future（拉平嵌套）
CompletableFuture<Product> f2 = userF.thenCompose(u ->
    CompletableFuture.supplyAsync(() -> fetchProductOfUser(u))
);
```

**`thenCompose` 相当于 `flatMap`，避免嵌套的 `CompletableFuture<CompletableFuture<T>>`**。

---

## 🔀 Future vs CompletableFuture

| | `Future` (Java 5) | `CompletableFuture` (Java 8+) |
|--|-------------------|------------------------------|
| 取结果 | 只能阻塞 `.get()` | 链式回调，不阻塞 |
| 组合多个 | 很难 | `thenCombine` / `allOf` / `anyOf` |
| 异常处理 | `try-catch` on `get()` | `exceptionally` / `handle` |
| 手动完成 | 不支持 | 支持 `complete(value)` |

**现代代码 99% 用 `CompletableFuture`**。

---

## 🌍 真实应用

| 场景 | Future 体现 |
|------|-----------|
| **微服务网关**（Zuul / Spring Cloud Gateway）| 并发调多个下游服务 |
| **RPC 客户端**（Dubbo / gRPC）| 异步调用返回 Future |
| **HTTP 客户端**（OkHttp / HttpClient）| `sendAsync()` 返回 CompletableFuture |
| **数据库 R2DBC** | 响应式数据库访问 |
| **MQ 消费** | 处理消息异步化 |
| **BFF 聚合层** | 合并多个接口数据 |

---

## 🎁 Promise 是什么？

"**Promise**" 在不同语言里略有差异：
- **JavaScript Promise**：本质和 Java 的 CompletableFuture 一样
- **Java 没有 Promise 这个名字**，就叫 Future / CompletableFuture
- **Guava** 有个 `SettableFuture`（可以当 Promise 用）

**Future**（只读凭证）vs **Promise**（可写凭证）：
- Future：消费者拿，只能读结果
- Promise：生产者拿，能写结果
- **Java 里 `CompletableFuture` 两者合一**（`complete(value)` 能手动写）

---

## ⚠️ 注意事项

### 1. 异常要处理
`Future.get()` 不处理 → 程序死在那。

### 2. 线程池要隔离
不要用默认的 `ForkJoinPool.commonPool()` 跑**阻塞任务**。会饿死其他任务。

```java
// 推荐：传自己的线程池
CompletableFuture.supplyAsync(task, myPool);
```

### 3. 别嵌套 CompletableFuture
```java
// 🚨 嵌套地狱
CompletableFuture<CompletableFuture<String>> bad = userF.thenApply(u -> ...);

// ✅ 用 thenCompose
CompletableFuture<String> good = userF.thenCompose(u -> ...);
```

### 4. 控制超时
```java
future.get(3, TimeUnit.SECONDS);      // 超时抛 TimeoutException
// 或 CompletableFuture#orTimeout（Java 9+）
```

---

## 📝 思考题 & 小练习

### 思考题

1. `Future.get()` 阻塞会不会死锁？（答：可能。A.get() 等 B，B 又用了同一线程池 → 可能死锁）
2. `CompletableFuture` 的 `thenApply` 和 `thenApplyAsync` 区别？（答：前者用完成的线程，后者切线程池）
3. 为什么阿里推荐用 `CompletableFuture.supplyAsync(task, customPool)`？（答：避免默认公用池挤占）

### 小练习

基于 `FutureDemo.java`：
- 用 `allOf` 等所有任务完成
- 加上 **3 秒超时**（超时返回默认值）

---

## 🏁 学完后

- **"懂了，下一课"** → 第 26 课 · 生产者-消费者
- **"CompletableFuture 想深入"** → 可以沉淀一篇
- **"先 commit"** → 我帮你

**Future 是异步编程的核心抽象**。从 Spring Reactor 到 Kotlin 协程，思想都延续 Future 🎫
