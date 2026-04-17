# 设计模式：从生活场景到代码实践（完整版）

> 一份循序渐进、场景化、可运行的设计模式学习项目。覆盖 **11 个阶段、约 70 个模式**，从经典 GoF 到云原生 + 宏观架构。

## 我们怎么学？

这个项目不是把模式一口气背下来，而是像游戏开地图一样，**一个模式一个模式地推进**。每个模式按这样的节奏走：

1. **🎬 生活场景开场** —— 用一个你生活里见过的故事开场（点外卖、班级选班长、订报纸……）
2. **🤔 痛点暴露** —— 先写一段"土办法"代码，让你亲眼看到它为什么会痛
3. **💡 模式登场** —— 在痛点上引出模式本身，讲清楚它解决什么、由谁提出、核心结构
4. **🛠 代码实践** —— 一份可直接运行的 Java 示例，注释详细，对照 UML
5. **🌍 真实应用** —— 这个模式在 JDK/Spring/开源项目中哪里用过
6. **⚠️ 什么时候别用** —— 过度设计是新手最容易踩的坑
7. **📝 思考题 + 小练习** —— 留给你消化

**学习节奏约定**：一次只带你学**一个**模式。你学完告诉我"OK 懂了"或者"这里还有疑问"，我再决定是继续下一个还是停下来把这个讲透。

---

## 为什么用 Java？

- GoF 原书的教学传统语言，网上资料最多
- 类型系统能清晰表达"接口/抽象类/继承"这些模式骨架
- 只需 JDK 11+，每个示例都能用 `java XxxDemo.java` **单文件直接运行**，无需 Maven/Gradle

> 现代/反应式/并发模式部分会酌情用 Java 8+ Stream、CompletableFuture、Project Reactor 等标准库或常见库演示。

**环境要求**：
```bash
java --version   # 需要 11 及以上
```

---

## 🗺 完整学习路线（11 阶段 · 约 70 个模式）

按**难度递增 + 主题相关**排列，而非 GoF 原书顺序。

### 🌱 第一阶段 · 创建型（GoF）—— 对象怎么造

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [单例 Singleton](01-creational/01-singleton/) | 班级里只能有一个班长 | ★ |
| 2 | [工厂方法 Factory Method](01-creational/02-factory-method/) | 披萨店按订单做披萨 | ★ |
| 3 | [建造者 Builder](01-creational/03-builder/) | Subway 点一份定制三明治 | ★★ |
| 4 | [抽象工厂 Abstract Factory](01-creational/04-abstract-factory/) | 宜家风格整套家具 | ★★ |
| 5 | [原型 Prototype](01-creational/05-prototype/) | 复印一份文件当起稿 | ★ |

### 🌿 第二阶段 · 结构型（GoF）—— 对象怎么组合

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [适配器 Adapter](02-structural/01-adapter/) | 出国用的插头转换器 | ★ |
| 2 | [外观 Facade](02-structural/02-facade/) | "回家模式"一键关全屋 | ★ |
| 3 | [代理 Proxy](02-structural/03-proxy/) | 明星的经纪人 | ★ |
| 4 | [组合 Composite](02-structural/04-composite/) | 文件夹里装文件和文件夹 | ★ |
| 5 | [装饰器 Decorator](02-structural/05-decorator/) | 给咖啡加奶、加糖、加奶油 | ★★ |
| 6 | [桥接 Bridge](02-structural/06-bridge/) | 遥控器与电视两条线各自演化 | ★★ |
| 7 | [享元 Flyweight](02-structural/07-flyweight/) | 围棋棋子只造两种 | ★★★ |

### 🌳 第三阶段 · 行为型（GoF）—— 对象怎么互动

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [模板方法 Template Method](03-behavioral/01-template-method/) | 做菜流程固定，食材可换 | ★ |
| 2 | [策略 Strategy](03-behavioral/02-strategy/) | 今天上班开车还是坐地铁 | ★ |
| 3 | [观察者 Observer](03-behavioral/03-observer/) | 订阅公众号，一推送全收到 | ★ |
| 4 | [迭代器 Iterator](03-behavioral/04-iterator/) | 遥控器一下一下切频道 | ★ |
| 5 | [责任链 Chain of Responsibility](03-behavioral/05-chain-of-responsibility/) | 请假审批：组长→经理→总监 | ★★ |
| 6 | [命令 Command](03-behavioral/06-command/) | 餐厅点单小票给后厨 | ★★ |
| 7 | [状态 State](03-behavioral/07-state/) | 订单：待付款→已付款→已发货 | ★★ |
| 8 | [备忘录 Memento](03-behavioral/08-memento/) | 游戏存档 / Ctrl+Z 撤销 | ★★ |
| 9 | [中介者 Mediator](03-behavioral/09-mediator/) | 机场塔台指挥飞机 | ★★★ |
| 10 | [访问者 Visitor](03-behavioral/10-visitor/) | 税务员访问不同业态商户 | ★★★ |
| 11 | [解释器 Interpreter](03-behavioral/11-interpreter/) | 翻译一句外语 | ★★★ |

### 🧵 第四阶段 · 并发模式 —— 多线程协作

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [线程池 Thread Pool](04-concurrent/01-thread-pool/) | 工人排队接任务 | ★ |
| 2 | [Future / Promise](04-concurrent/02-future-promise/) | 取号排队等结果 | ★ |
| 3 | [生产者-消费者 Producer-Consumer](04-concurrent/03-producer-consumer/) | 传送带解耦上下游 | ★ |
| 4 | [读写锁 Read-Write Lock](04-concurrent/04-read-write-lock/) | 读者同看，写者独占 | ★★ |
| 5 | [双检锁 Double-Checked Locking](04-concurrent/05-double-checked-locking/) | 先瞄一眼再上锁 | ★★ |
| 6 | [保护性暂停 Guarded Suspension](04-concurrent/06-guarded-suspension/) | 条件没到先睡着 | ★★ |

### 🗄 第五阶段 · 数据访问模式（PoEAA）—— 怎么和数据库打交道

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [DAO](05-data-access/01-dao/) | 数据库操作专用窗口 | ★ |
| 2 | [Active Record](05-data-access/02-active-record/) | 对象自己会存自己 | ★ |
| 3 | [Data Mapper](05-data-access/03-data-mapper/) | 专人搬家，对象不碰库 | ★★ |
| 4 | [Identity Map](05-data-access/04-identity-map/) | 同一主键只加载一次 | ★★ |
| 5 | [Lazy Load 延迟加载](05-data-access/05-lazy-load/) | 用到再查，不急着加载 | ★★ |
| 6 | [Query Object](05-data-access/06-query-object/) | 查询条件也是对象 | ★★★ |

### 🏢 第六阶段 · 企业应用模式 —— 工作中真的会用

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [空对象 Null Object](06-enterprise/01-null-object/) | 没访客时用"假访客"代替 | ★ |
| 2 | [依赖注入 DI / IoC](06-enterprise/02-dependency-injection/) | 食材由供应商送上门 | ★★ |
| 3 | [仓储 Repository](06-enterprise/03-repository/) | 图书馆管理员替你找书 | ★★ |
| 4 | [工作单元 Unit of Work](06-enterprise/04-unit-of-work/) | 购物车结算一次下单 | ★★ |
| 5 | [规格 Specification](06-enterprise/05-specification/) | 相亲条件组合 | ★★★ |
| 6 | [MVC / MVP / MVVM](06-enterprise/06-mvc-mvp-mvvm/) | 舞台 / 导演 / 演员 | ★★ |

### 🧩 第七阶段 · DDD 战术模式 —— 领域建模基本功

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [实体 Entity](07-ddd/01-entity/) | 有身份证的人 | ★ |
| 2 | [值对象 Value Object](07-ddd/02-value-object/) | 两张 100 块没区别 | ★ |
| 3 | [聚合 Aggregate](07-ddd/03-aggregate/) | 族长代表全家对外 | ★★ |
| 4 | [领域事件 Domain Event](07-ddd/04-domain-event/) | "订单已支付"广播 | ★★ |
| 5 | [防腐层 Anti-Corruption Layer](07-ddd/05-anti-corruption-layer/) | 翻译外系统"脏话" | ★★★ |

### 📬 第八阶段 · 企业集成模式（EIP）—— 系统之间怎么说话

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [管道-过滤器 Pipes and Filters](08-eip/01-pipes-and-filters/) | 流水线一道工序一道 | ★★ |
| 2 | [消息路由器 Message Router](08-eip/02-message-router/) | 邮递员按地址分拣 | ★★ |
| 3 | [内容路由器 Content-Based Router](08-eip/03-content-based-router/) | 按内容决定去哪 | ★★ |
| 4 | [分裂器/聚合器 Splitter & Aggregator](08-eip/04-splitter-aggregator/) | 大拆小 / 小合大 | ★★ |
| 5 | [散发-收集 Scatter-Gather](08-eip/05-scatter-gather/) | 群发请求统一收答复 | ★★★ |

### 🌀 第九阶段 · 函数式/反应式模式 —— 现代范式

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [记忆化 Memoization](09-functional-reactive/01-memoization/) | 算过的答案背下来 | ★★ |
| 2 | [柯里化 Currying](09-functional-reactive/02-currying/) | 参数一个一个喂 | ★★ |
| 3 | [惰性求值 Lazy Evaluation](09-functional-reactive/03-lazy-evaluation/) | 按需才算 | ★★ |
| 4 | [Functor / Monad](09-functional-reactive/04-functor-monad/) | 带语境的容器 | ★★★ |
| 5 | [反应式流 Reactive Streams](09-functional-reactive/05-reactive-streams/) | 数据流+防淹没背压 | ★★★ |

### ☁️ 第十阶段 · 云原生 & 分布式模式 —— 微服务必备

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [重试 Retry](10-cloud-native/01-retry/) | 电话占线再拨 | ★ |
| 2 | [熔断器 Circuit Breaker](10-cloud-native/02-circuit-breaker/) | 保险丝跳闸保护全屋 | ★★ |
| 3 | [舱壁 Bulkhead](10-cloud-native/03-bulkhead/) | 船的水密舱 | ★★ |
| 4 | [限流 Throttle](10-cloud-native/04-throttle/) | 景区每小时限客 | ★★ |
| 5 | [发布订阅 Pub-Sub](10-cloud-native/05-pub-sub/) | 电台广播 | ★★ |
| 6 | [事件溯源 Event Sourcing](10-cloud-native/06-event-sourcing/) | 记账本只增不改 | ★★★ |
| 7 | [CQRS](10-cloud-native/07-cqrs/) | 收款与查账窗口分开 | ★★★ |
| 8 | [Saga 分布式事务](10-cloud-native/08-saga/) | 机+酒+车一家失败全回滚 | ★★★ |

### 🏛 第十一阶段 · 宏观架构模式 —— 俯瞰全局

| # | 模式 | 生活比喻 | 难度 |
|---|------|---------|------|
| 1 | [分层架构 Layered](11-architecture/01-layered/) | 蛋糕一层一层 | ★ |
| 2 | [六边形 Hexagonal / Ports & Adapters](11-architecture/02-hexagonal/) | 内核+插座适配外界 | ★★ |
| 3 | [整洁架构 Clean Architecture](11-architecture/03-clean-architecture/) | 洋葱，依赖向内指 | ★★ |
| 4 | [微服务 Microservices](11-architecture/04-microservices/) | 独立小团队协作 | ★★★ |
| 5 | [事件驱动架构 Event-Driven](11-architecture/05-event-driven/) | 系统像神经网络 | ★★★ |

---

## 📈 学习路径图

```
  【对象层面】                【工程层面】             【系统层面】
                                                    
  01 创建型 ─┐                                       
             ├→ 04 并发 ─┐                           
  02 结构型 ─┤           ├→ 06 企业应用 ─┐            
             │           │                ├→ 10 云原生 ─┐
  03 行为型 ─┘           │   07 DDD ──────┤             ├→ 11 架构
                         └── 05 数据访问 ─┘             │
                                                        │
                                08 EIP ─────────────────┤
                                09 函数式/反应式 ───────┘
```

**建议学习节奏**：
- 第 1-3 阶段（GoF 23 种）：打地基，**必学**
- 第 4-6 阶段：Java 后端工程师**强烈建议**
- 第 7-8 阶段：做微服务/领域建模时**再来看**
- 第 9 阶段：接触 RxJava/Reactor/前端 Redux 时**补**
- 第 10-11 阶段：中高级/架构师方向的**升级包**

---

## 📁 项目目录结构

```
design-patterns/
├── README.md                        ← 你现在看的这份总大纲
├── 01-creational/        (5 个模式)
├── 02-structural/        (7 个模式)
├── 03-behavioral/        (11 个模式)
├── 04-concurrent/        (6 个模式)
├── 05-data-access/       (6 个模式)
├── 06-enterprise/        (6 个模式)
├── 07-ddd/               (5 个模式)
├── 08-eip/               (5 个模式)
├── 09-functional-reactive/ (5 个模式)
├── 10-cloud-native/      (8 个模式)
├── 11-architecture/      (5 个模式)
└── assets/               ← UML 图 / 示意图

每个模式目录里都是：
  xx-pattern-name/
  ├── README.md        ← 故事 + 理论 + 思考题
  └── code/            ← 可运行 Java 代码
      ├── BadXxx.java       ← "土办法"版，暴露痛点
      └── XxxDemo.java      ← 模式版，详细注释
```

每个模式子目录都是**独立可读、独立可跑**的小单元。

---

## 🎯 学习的六大原则

1. **先看懂场景，再看代码** —— 场景没 get，代码就是天书
2. **一定要自己敲一遍** —— 光看不练会产生"我懂了"的幻觉
3. **先让代码跑起来再理解** —— 有实际输出后，反推代码逻辑会快很多
4. **理解"为什么要这个模式"比记结构重要** —— 结构会忘，动机不会
5. **警惕过度设计** —— 简单问题千万别上模式，三行 if-else 比一个工厂好
6. **模式不是银弹** —— 真实代码里常常是几个模式混用 + 一堆没名字的普通设计

---

## 🚀 下一步

🎯 **第一课：单例模式（Singleton）** → [01-creational/01-singleton/](01-creational/01-singleton/)

如果这份完整大纲你看着顺眼，告诉我 "**开始吧**"，我就开写第一课。

如果还想调整：
- 换编程语言（Go / Python / TypeScript / C#）
- 某个阶段想先跳过，以后回来补
- 某个模式换个生活化比喻
- 加一个目前没列的模式

**随时说，按你的节奏来。** 🙌
