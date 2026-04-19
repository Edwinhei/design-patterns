# 第 21 课 · 中介者 Mediator ★★★

> 类型：行为型 | 难度：★★★ | GoF 经典 | **用一个"协调者"消除 N 对 N 耦合**

## 🎯 本课目标

- [x] 理解"**对象之间不直接通信，通过中介协调**"
- [x] 看清"**N² 耦合 → N 耦合**"的威力
- [x] 识别工作中的中介者（MVC / 聊天室 / UI 控件联动）

---

## 🎬 场景：聊天室 vs 机场塔台

### 场景 1：聊天室 💬
10 个人在微信群里聊天。**他们不互相加好友**，**只通过"群"转发消息**。
- 👤 张三发消息 → 📢 群 → 所有人收到

### 场景 2：机场塔台 ✈️
几十架飞机同时在空域。**它们不直接对话**（乱套），**都听塔台**。
- ✈️ 飞机 → 🗼 塔台 → ✈️ 飞机

**共性**：**所有对象只和"一个协调者"通信**，对象之间**互不直接引用**。

---

## 🤔 土办法：对象之间直接互相引用

```java
class User {
    private List<User> friends;      // 持有所有其他用户

    public void sendMessage(String msg) {
        for (User f : friends) {
            f.receive(this.name, msg);
        }
    }
}
```

跑 [code/BadMediator.java](code/BadMediator.java)。

**痛点**：
- 🙁 每个用户要**持有所有其他用户的引用**
- 🙁 **N 个对象 → N² 条连线**（10 个人 = 45 条，100 个 = 4950 条）
- 🙁 加新用户 → **已有所有用户都要更新 friends 列表**
- 🙁 对象之间**紧耦合**

---

## 💡 中介者模式登场

**核心思想**：**所有对象只和"中介者"通信，不直接互相引用**。

```java
// 中介者接口
interface ChatRoom {
    void register(User user);
    void send(String from, String msg);
}

// 具体中介者：持有所有用户，转发消息
class ConcreteChatRoom implements ChatRoom {
    private List<User> users = new ArrayList<>();

    public void register(User user) {
        users.add(user);
        user.setRoom(this);        // 用户也要知道 room
    }

    public void send(String from, String msg) {
        for (User u : users) {
            if (!u.getName().equals(from)) {    // 不发给自己
                u.receive(from, msg);
            }
        }
    }
}

// 同事（Colleague）：只知道中介者，不知道其他用户
class User {
    private String name;
    private ChatRoom room;         // 🎯 只持有中介者

    public void setRoom(ChatRoom room) { this.room = room; }

    public void send(String msg) {
        room.send(name, msg);      // 不直接找别人，通过 room
    }

    public void receive(String from, String msg) {
        System.out.println(name + " 收到 [" + from + "]: " + msg);
    }
}
```

### 使用

```java
ChatRoom room = new ConcreteChatRoom();

User alice = new User("Alice");
User bob   = new User("Bob");
User tom   = new User("Tom");

room.register(alice);
room.register(bob);
room.register(tom);

alice.send("大家好");        // Bob 和 Tom 都能收到
bob.send("Hi Alice!");        // Alice 和 Tom 能收到
```

**威力**：
- ✅ **每个用户只持有一个 ChatRoom 引用**
- ✅ **N 个对象 → N 条连线**（不是 N²）
- ✅ 加新用户 → **只 `register` 一下**，其他用户不动
- ✅ 消息转发逻辑**集中在中介者**，易改

跑 [code/MediatorDemo.java](code/MediatorDemo.java)。

---

## 🔗 N² → N 的可视化

### 没中介者：所有对象互连

```
    User A ────── User B
      │ ╲       ╱ │
      │   ╲   ╱   │        10 个节点 = 45 条连线
      │     ╳     │        100 个节点 = 4950 条
      │   ╱   ╲   │
      │ ╱       ╲ │
    User C ────── User D
```

### 有中介者：星型结构

```
        User A
          │
  User B──Chat──User D
          │
        User C            10 个节点 = 10 条连线
                          100 个节点 = 100 条
```

**复杂度从 O(N²) 降到 O(N)**。

---

## 📐 UML 结构

```
┌──────────────┐         ┌──────────────────┐
│   Mediator   │◀──持有──│   Colleague      │
│  (接口)       │         │   (User)         │
├──────────────┤         ├──────────────────┤
│ +send(from,m)│         │ -mediator        │
└──────▲───────┘         │ +send(msg)       │
       │                 │ +receive(m)      │
       │                 └──────────────────┘
┌──────┴───────────┐
│ ConcreteMediator │──持有一堆 Colleague
│ (ChatRoom)       │
└──────────────────┘
```

**关键**：**Colleague 只依赖 Mediator 接口，不互相引用**。

---

## 🌍 真实应用（非常多）

| 场景 | 中介者 |
|------|-------|
| **MVC 的 Controller** | 协调 Model 和 View（V 不直接知道 M 更新）|
| **Spring ApplicationContext** | Bean 之间不直接引用，都通过 IoC 容器 |
| **事件总线 EventBus** | 组件之间通过 Bus 通信（Guava / Spring Events）|
| **消息队列** | 生产者/消费者不直接连接 |
| **航空塔台** | 飞机不直接沟通，都听塔台 |
| **聊天室** | 用户通过房间交互（微信群 / Discord）|
| **UI 控件联动** | 比如选了"国家" → 自动刷新"省"下拉，通过 Form 中介 |
| **IM 服务器** | WhatsApp / Slack 都是巨型中介者 |
| **游戏房间** | 玩家只和房间交互 |

---

## 🔀 中介者 vs 观察者（易混淆）

两者都涉及"对象间通知"，区别：

| | 中介者 | 观察者 |
|--|-------|-------|
| 通信模式 | **多对多**（通过中介） | **一对多**（Subject → 多个 Observer）|
| 典型 | 群聊 | 订阅公众号 |
| 中间人 | **必须有** | 没有（Subject 直接通知）|
| 数据流方向 | 双向（任何人发送 → 其他人收） | 单向（Subject → 观察者）|

**注意**：两者经常**一起用** —— 中介者内部可能用观察者实现通知。

---

## ⚠️ 什么时候别用

### 🚫 对象数量少（2-3 个）
直接引用就行，搞中介者是过度设计。

### 🚫 中介者成为**上帝对象**
如果中介者**承担所有业务逻辑**，会变成反模式 God Object。中介者应该**只协调**，不承担业务。

### 🚫 交互逻辑极其简单
两个对象一对一通信？用观察者或直接调用。

---

## 📝 思考题 & 小练习

### 思考题

1. **MVC 架构**里的 Controller 怎么算中介者？（答：协调 Model 和 View，它们不直接知道对方）
2. **Spring 的 ApplicationContext** 是中介者吗？（答：是。Bean 互相不 new 对方，都通过容器）
3. 中介者和"**God Object 反模式**"有什么界限？（答：中介者**只协调**，不承载业务；God Object 什么都做）

### 小练习

**扩展 MediatorDemo.java**
- 加一个**私聊功能**：`User.whisper(String to, String msg)`
- ChatRoom 需要能按名字找到用户，只发给那个人

---

## 🏁 学完后

- **"懂了，下一课"** → 第 22 课 · 访问者 Visitor（税务员查税）
- **"Mediator 和 Observer 再讲讲"** → 我对比
- **"先 commit"** → 我帮你

**中介者是"消除对象网状耦合"的利器**。现代架构（事件驱动 / 消息队列 / MVC / IoC 容器）本质都是中介者 🗼
