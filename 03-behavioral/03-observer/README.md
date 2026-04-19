# 第 15 课 · 观察者 Observer ★

> 类型：行为型 | 难度：★ | GoF 经典 | **事件驱动系统的基石**

## 🎯 本课目标

- [x] 理解"**一对多自动通知**"
- [x] 掌握观察者的三个角色：Subject / Observer / 具体观察者
- [x] 识别工作里的观察者：事件监听 / MVC / 响应式框架

---

## 🎬 场景：订阅公众号

你关注了一个技术公众号。**它发新文章 → 你手机立刻收到推送**。

关键特点：
- 📢 公众号（发布者）**不需要主动去联系每个读者**
- 👥 读者（订阅者）**主动订阅**，以后被动接收
- ➕ 可以随时**新增**订阅者
- ➖ 可以随时**取消**订阅
- 📣 公众号**一次发布，所有订阅者都收到**

**这就是观察者模式** —— "**一对多**的通知机制"。

---

## 🤔 土办法：发布者直接调用每个订阅者

```java
class BadPublisher {
    User zhang;
    User li;
    User wang;

    public void publishNews(String news) {
        zhang.receive(news);
        li.receive(news);
        wang.receive(news);   // 每加一个要改代码
    }
}
```

跑 [code/BadObserver.java](code/BadObserver.java) 看痛点。

**痛点**：
- 🙁 发布者**必须知道所有订阅者**（紧耦合）
- 🙁 加新订阅者**要改发布者代码**（违反开闭原则）
- 🙁 **无法动态**订阅/取消
- 🙁 订阅者**硬编码**，完全没法复用

---

## 💡 观察者模式登场

**核心思想**：**发布者持有一个观察者列表，状态变化时通知列表里所有人**。

```java
// 观察者接口：定义"收到通知后做什么"
interface Subscriber {
    void onNews(String news);
}

// 主题（发布者）：持有订阅者列表
class PublicAccount {
    private String name;
    private List<Subscriber> subscribers = new ArrayList<>();

    public void subscribe(Subscriber s) {           // 订阅
        subscribers.add(s);
    }

    public void unsubscribe(Subscriber s) {         // 取消订阅
        subscribers.remove(s);
    }

    public void publish(String news) {               // 发布 → 通知所有人
        System.out.println("[" + name + "] " + news);
        for (Subscriber s : subscribers) {
            s.onNews(news);
        }
    }
}

// 具体观察者
class User implements Subscriber {
    private String name;

    public void onNews(String news) {
        System.out.println("  📱 " + name + " 收到: " + news);
    }
}
```

### 使用

```java
PublicAccount tech = new PublicAccount("技术公众号");

tech.subscribe(new User("张三"));
tech.subscribe(new User("李四"));

tech.publish("Java 21 新特性");
// 张三、李四 都收到
```

**威力**：
- ✅ 加新订阅者 → **只需 `subscribe()` 一下**，不改发布者代码
- ✅ 发布者**不知道具体订阅者是谁**（只知道是 `Subscriber` 接口）
- ✅ 动态订阅 / 取消订阅
- ✅ 一次发布，所有人收到

跑 [code/ObserverDemo.java](code/ObserverDemo.java) 看完整演示。

---

## 📐 UML 结构

```
┌────────────────────────┐       ┌──────────────────────┐
│  PublicAccount (Subject)│──持有──▶│  Subscriber (接口)   │
├────────────────────────┤  list  ├──────────────────────┤
│ -subscribers: List     │        │ +onNews(news)        │
│ +subscribe(s)          │        └──────────▲───────────┘
│ +unsubscribe(s)        │                   │
│ +publish(news)         │          ┌────────┴────────┐
└────────────────────────┘          │                 │
                                ┌───────┐        ┌───────┐
                                │ User  │        │ Bot   │
                                │  张三  │        │ 爬虫   │
                                └───────┘        └───────┘
```

**三角色**：
1. **Subject**（`PublicAccount`）—— 主题 / 被观察者
2. **Observer**（`Subscriber` 接口）—— 观察者接口
3. **具体 Observer**（`User` / `Bot`）—— 实现接口，处理通知

---

## 🎁 Java 8+ Lambda 简化

`Subscriber` 只有一个方法 → **函数式接口** → 可以用 Lambda：

```java
tech.subscribe(news -> System.out.println("匿名 1 收到: " + news));
tech.subscribe(news -> {
    if (news.contains("Java")) {
        System.out.println("Java 粉丝收到: " + news);
    }
});
```

**连定义 User 类都省了**。

---

## 🔀 观察者模式的"两种形态"

### 形态 1：推（Push）—— 发布者把数据推给所有观察者
```java
void publish(String news) {
    for (Subscriber s : subs) s.onNews(news);   // 直接推 news
}
```
**优点**：简单、直接
**缺点**：观察者可能不需要这么多数据

### 形态 2：拉（Pull）—— 发布者只通知"有变化"，观察者自己来拿
```java
void publish() {
    for (Subscriber s : subs) s.update(this);    // 把自己传过去
}
// 观察者：
void update(PublicAccount pa) {
    String news = pa.getLatestNews();            // 主动拉
}
```

**大多数场景用"推"更直接**。

---

## 🌍 真实应用（超多）

| 在哪里 | 观察者身影 |
|--------|----------|
| **Java Swing** | `ActionListener`（按钮点击 → 通知监听器）|
| **JavaScript DOM** | `addEventListener('click', handler)` |
| **Vue / React** | 响应式数据绑定（数据变 → UI 自动更新）|
| **消息队列** | Kafka / RabbitMQ 的订阅机制 |
| **Spring Event** | `ApplicationEvent` / `@EventListener` |
| **RxJava / Project Reactor** | Stream 订阅 |
| **MVC 模式** | Model 变化 → 通知 View 刷新 |
| **GUI 框架** | 几乎所有"监听器"都是观察者 |

**观察者是事件驱动系统的基石**。

---

## 🔀 观察者 vs 发布-订阅（常混淆）

| | 观察者 | 发布-订阅 |
|--|-------|---------|
| 中间人 | ❌ 主题直接持有观察者 | ✅ 有一个消息总线/Broker |
| 耦合度 | 发布者知道 Subscriber 接口 | 发布者完全不知道订阅者 |
| 典型 | 本课例子 | Kafka / EventBus |

**Pub-Sub 是观察者的分布式增强版**（后面第 10 阶段云原生会专门讲）。

---

## ⚠️ 什么时候别用

### 🚫 只有一个观察者 + 未来也不会增加
直接调方法就行，不用搞接口。

### 🚫 通知顺序敏感
观察者列表通常是无序的，如果必须按顺序通知，得自己管。

### 🚫 观察者反过来修改主题
容易循环通知死循环。

### 🚫 性能极敏感
通知 10000 个观察者 → 10000 次方法调用，可能是瓶颈。

---

## 📝 思考题 & 小练习

### 思考题

1. 如果一个观察者在 `onNews` 里**又订阅/取消**另一个观察者，会出什么问题？（答：ConcurrentModificationException，因为在遍历列表时改了它）
2. JDK 里早有 `java.util.Observable` 类，为什么 Java 9 废弃了？（答：实现粗糙，推荐用自定义接口 + Lambda）
3. 观察者和 Spring `@EventListener` 有什么关系？（答：Spring 实现了一个更强的观察者机制，基于发布-订阅）

### 小练习

**扩展 ObserverDemo.java**
加一个过滤订阅者 `KeywordSubscriber`：
- 构造器接收一个关键词
- 只有新闻包含这个关键词时才打印
- 使用：`tech.subscribe(new KeywordSubscriber("Java"))`

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 16 课 · 迭代器 Iterator（for-each 背后的秘密）
- **"想深入聊响应式编程"** → RxJava / Reactor 到时候专门开
- **"先 commit"** → 我帮你

**观察者是前端 / 后端 / 移动端都大量使用的模式**。学完你看事件系统会有全局感 👁
