# 第 14 课 · 策略 Strategy ★

> 类型：行为型 | 难度：★ | GoF 经典 | **工作中最常用的模式之一**

## 🎯 本课目标

- [x] 理解"**把算法封装成类，可互换**"
- [x] 看清策略 vs 模板方法的区别（组合 vs 继承）
- [x] 掌握 Java 8+ 用 Lambda 简化策略

---

## 🎬 场景：今天怎么出门？

你每天上班，可能用不同方式：

- 🚗 天气好 → 开车
- 🚇 下雨 → 坐地铁
- 🚴 锻炼身体 → 骑车

**同一个"出行"动作**，用不同的**方式（策略）**实现。

---

## 🤔 土办法：if-else 判断

```java
void travel(String mode, String destination) {
    if (mode.equals("car")) {
        System.out.println("开车去 " + destination);
    } else if (mode.equals("subway")) {
        System.out.println("坐地铁去 " + destination);
    } else if (mode.equals("bike")) {
        System.out.println("骑车去 " + destination);
    } else if (mode.equals("walk")) {
        // ...
    }
}
```

跑 [code/BadStrategy.java](code/BadStrategy.java) 看这段代码的痛。

**痛点**：
- 🙁 加新出行方式（打车）→ 改这个方法
- 🙁 每种方式的逻辑复杂时，方法会变成几百行
- 🙁 字符串匹配容易出错（typo 不报错）
- 🙁 **违反开闭原则**（加新功能要改老代码）

---

## 💡 策略模式登场

**核心思想**：**把每种算法封装成一个独立的类（实现同一接口），让它们可以互换**。

```java
// 策略接口
interface TransportStrategy {
    void go(String destination);
}

// 具体策略 1：开车
class CarStrategy implements TransportStrategy {
    public void go(String dest) {
        System.out.println("🚗 开车去 " + dest);
    }
}

// 具体策略 2：地铁
class SubwayStrategy implements TransportStrategy {
    public void go(String dest) {
        System.out.println("🚇 坐地铁去 " + dest);
    }
}

// Context：持有策略引用，可以运行时切换
class TravelContext {
    private TransportStrategy strategy;

    public void setStrategy(TransportStrategy s) {
        this.strategy = s;
    }

    public void travel(String dest) {
        strategy.go(dest);
    }
}
```

### 使用

```java
TravelContext ctx = new TravelContext();

ctx.setStrategy(new CarStrategy());    // 开车
ctx.travel("公司");

ctx.setStrategy(new SubwayStrategy()); // 换地铁
ctx.travel("商场");
```

**威力**：
- ✅ 加新策略（`WalkStrategy`、`TaxiStrategy`） → **新加一个类**，不改已有代码
- ✅ 运行时**动态切换**算法
- ✅ 符合开闭原则

跑 [code/StrategyDemo.java](code/StrategyDemo.java) 看完整演示。

---

## 📐 UML 结构

```
┌──────────────────────┐      ┌────────────────────┐
│  TravelContext       │──持有─▶│ TransportStrategy  │ (接口)
│  (Context)           │      ├────────────────────┤
├──────────────────────┤      │ +go(dest): void    │
│ -strategy: Strategy  │      └──────────▲─────────┘
│ +setStrategy(s)      │                 │
│ +travel(dest)        │          ┌──────┼──────┬──────┐
└──────────────────────┘     ┌────┴─┐ ┌──┴──┐ ┌─┴───┐ ...
                              CarStrategy
                              SubwayStrategy
                              BikeStrategy
```

**三角色**：
1. **策略接口**（`TransportStrategy`）
2. **具体策略**（`CarStrategy` 等）
3. **上下文**（`TravelContext`，持有策略引用）

---

## 🎁 Java 8+ · Lambda 让策略更轻

策略接口只有一个方法？→ **函数式接口** → 可以用 Lambda：

```java
// 不用写类，直接 Lambda
ctx.setStrategy(dest -> System.out.println("🛵 骑摩托去 " + dest));
ctx.travel("公园");
```

**Lambda 让简单策略的代码量减少 90%**。

但是如果策略本身有状态 / 多方法，还是用类。

---

## 🔀 策略 vs 模板方法 —— 兄弟俩

两者都解决"算法变化"的问题，**方式不同**：

| 维度 | 模板方法 | 策略 |
|------|---------|------|
| 实现 | **继承**（父子类关系）| **组合**（持有引用）|
| 变化粒度 | 流程里的**某些步骤** | **整个算法** |
| 运行时改变 | ❌（继承固定）| ✅（setStrategy 切换）|
| 典型场景 | 流程主体固定，局部步骤变 | 多种完全不同的算法二选一 |

### 一句话对比

- **模板方法**：我定好流程，你填某些步骤（👨‍🏫 家长+孩子关系）
- **策略**：我有个能力位，你随便插个能力进来（🔌 USB 插口）

**两者往往一起用**：模板方法的"某个步骤"由策略决定。

---

## 🌍 真实应用

| 在哪里 | 策略是什么 |
|--------|----------|
| **JDK** | `Comparator` 接口 —— 每种比较逻辑是一种策略 |
| **JDK** | `ThreadFactory` —— 不同的线程创建策略 |
| **Spring** | `RedirectStrategy` —— 不同的重定向策略 |
| **Spring Security** | `AuthenticationProvider` —— 不同的认证策略 |
| **支付系统** | `PaymentStrategy`（微信 / 支付宝 / 信用卡）|
| **压缩库** | `CompressionAlgorithm`（ZIP / GZIP / LZMA）|
| **游戏 AI** | 不同难度等级的行为策略 |

### 最经典的 `Comparator` 例子

```java
List<User> users = ...;

// 按年龄排（一种策略）
users.sort(Comparator.comparingInt(User::getAge));

// 按姓名排（另一种策略）
users.sort(Comparator.comparing(User::getName));

// 自定义策略
users.sort((a, b) -> b.getScore() - a.getScore());
```

**`Comparator` 就是一个完整的策略模式应用**。

---

## ⚠️ 什么时候别用

### 🚫 算法只有 1-2 种且不会变
直接写代码，不用搞策略。

### 🚫 算法之间差异很小
只有几行代码不同 → 用模板方法或参数化更合适。

### 🚫 客户端要懂所有策略
策略模式要求客户端知道"有哪些策略可选"。如果策略太多（比如 20 种），会困扰使用者。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么 Java 8 的 Lambda 让策略模式"轻量化"？（答：单方法接口 + 函数式编程，不用写类）
2. 策略模式和工厂方法怎么配合？（答：工厂生成策略，策略再被 Context 使用）
3. 如何用"策略 + Map"消除 if-else？

### 小练习

**练习：用 Map + 策略消除 if-else**

```java
Map<String, TransportStrategy> strategies = Map.of(
    "car",    new CarStrategy(),
    "subway", new SubwayStrategy(),
    "bike",   new BikeStrategy()
);

// 客户端
String mode = getModeFromInput();
TransportStrategy strategy = strategies.get(mode);
strategy.go("目的地");
```

**这是"查表法"**：用 Map 查策略，**彻底消灭 if-else**。

扩展 `StrategyDemo.java` 加入这个写法。

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 15 课 · 观察者 Observer（订阅公众号）
- **"模板方法 vs 策略还想再对比"** → 我继续讲
- **"先 commit"** → 我帮你

**策略是工作中使用频率极高的模式**。学完后你会发现"**一堆 if-else 判断业务类型**"的代码都能重构成策略 🎯
