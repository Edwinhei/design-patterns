# 第 19 课 · 状态 State ★★

> 类型：行为型 | 难度：★★ | GoF 经典 | **状态机的优雅实现**

## 🎯 本课目标

- [x] 理解"**对象在不同状态下行为不同**"
- [x] 识别"**该用状态模式**"的场景
- [x] 分清 **State vs Strategy**（最易混淆的一对）

---

## 🎬 场景：订单的一生

电商订单有明确的生命周期：

```
  待付款  ────支付────▶  已付款  ────发货────▶  已发货  ────收货────▶  已完成
   │                      │                       │
   └────取消────▶  已取消   │                       │
                          └────取消(退款)────▶  已取消
```

**不同状态下，同一个操作行为完全不同**：
- 待付款 → `ship()`（发货）应该**拒绝**
- 已付款 → `ship()` 应该**允许**
- 已完成 → `pay()`（再支付）应该**拒绝**

---

## 🤔 土办法：if-else 判断状态

```java
class Order {
    private String status;

    public void pay() {
        if (status.equals("待付款")) {
            System.out.println("支付成功");
            status = "已付款";
        } else {
            throw new IllegalStateException("不能在 " + status + " 付款");
        }
    }

    public void ship() {
        if (status.equals("已付款")) {
            System.out.println("发货成功");
            status = "已发货";
        } else {
            throw new IllegalStateException("不能在 " + status + " 发货");
        }
    }

    public void cancel() {
        if (status.equals("待付款")) {
            status = "已取消";
        } else if (status.equals("已付款")) {
            // 退款
            status = "已取消";
        } else {
            throw new IllegalStateException("不能在 " + status + " 取消");
        }
    }

    // ... 每个方法都要重复判断
}
```

跑 [code/BadState.java](code/BadState.java)。

**痛点**：
- 🙁 每个方法都有一堆 **if-else 判断状态**
- 🙁 加新状态（比如"已退款审核中"）→ **每个方法都要改**
- 🙁 状态流转逻辑**散落各处**，难追踪
- 🙁 违反开闭原则

---

## 💡 状态模式登场

**核心思想**：**把每个状态封装成一个类**，每个状态类知道自己允许什么操作、转到什么状态。

```java
// 状态接口
interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void complete(Order order);
    void cancel(Order order);
    String name();
}

// 具体状态：待付款
class PendingPayment implements OrderState {
    public void pay(Order order) {
        System.out.println("💰 支付成功");
        order.setState(new Paid());       // 🔄 转到已付款
    }
    public void ship(Order order)    { reject("未付款"); }
    public void complete(Order order){ reject("未付款"); }
    public void cancel(Order order) {
        System.out.println("❌ 订单取消");
        order.setState(new Cancelled());
    }
    public String name() { return "待付款"; }
}

// 具体状态：已付款
class Paid implements OrderState {
    public void pay(Order order)     { reject("已付款"); }
    public void ship(Order order) {
        System.out.println("🚚 发货");
        order.setState(new Shipped());    // 🔄 转到已发货
    }
    public void complete(Order order){ reject("未发货"); }
    public void cancel(Order order) {
        System.out.println("💸 退款并取消");
        order.setState(new Cancelled());
    }
    public String name() { return "已付款"; }
}

// ... 其他状态类
```

### Context（订单）

```java
class Order {
    private OrderState state = new PendingPayment();   // 初始状态

    public void setState(OrderState state) {
        System.out.println("  [状态] " + this.state.name() + " → " + state.name());
        this.state = state;
    }

    // 所有操作都委托给当前状态
    public void pay()      { state.pay(this); }
    public void ship()     { state.ship(this); }
    public void complete() { state.complete(this); }
    public void cancel()   { state.cancel(this); }
}
```

### 使用

```java
Order order = new Order();
order.pay();        // 待付款 → 已付款
order.ship();       // 已付款 → 已发货
order.complete();   // 已发货 → 已完成

order.pay();        // 已完成状态下支付 → 拒绝
```

**威力**：
- ✅ **每个状态一个类**，状态转换逻辑**集中**
- ✅ 加新状态 → **加一个类**，不改已有代码
- ✅ 消灭 if-else
- ✅ Context 只负责委托

跑 [code/StateDemo.java](code/StateDemo.java) 看完整流程。

---

## 📐 UML 结构

```
┌────────────────┐      ┌─────────────────┐
│   Order        │─持有─▶│  OrderState     │
│   (Context)    │      │   (接口)        │
├────────────────┤      ├─────────────────┤
│ -state         │      │ +pay(order)     │
│ +pay()         │      │ +ship(order)    │
│ +ship()        │      │ +cancel(order)  │
│ +setState(s)   │      └────────▲────────┘
└────────────────┘               │
                      ┌──────────┼──────────┬──────────┐
                      │          │          │          │
              PendingPayment  Paid    Shipped     Completed
                      │                                 │
                      └─────── 彼此通过 setState 转换 ───┘
```

**核心**：状态类**自己决定转到下一个状态**。

---

## 🔀 State vs Strategy（最易混淆）

**两者形态极像**（都是"把行为封装成类"），**但意图完全不同**：

| 维度 | **State** | **Strategy** |
|------|----------|-------------|
| **切换** | **状态自行切换**（A 转到 B 由 A 决定）| **客户端选择**（用哪个策略由调用方决定）|
| **状态之间** | **有转换关系**（不是所有状态之间都能转）| 独立，随时替换 |
| **客户端感知** | 客户端**不关心**当前是什么状态 | 客户端**主动选**策略 |
| **典型场景** | 订单流转 / 工作流 / 状态机 | 支付方式 / 排序算法 / 出行方式 |

### 一句话对比
- **Strategy**：**"你选哪个？"**（客户端决定）
- **State**：**"现在是什么状态？"**（状态自己决定下一步）

---

## 🌍 真实应用

| 场景 | 状态流转 |
|------|---------|
| **订单系统**（本课）| 待付款 → 已付款 → 已发货 → 已完成 |
| **工作流** | 草稿 → 提交 → 审核中 → 已通过 / 已驳回 |
| **游戏角色** | 站立 → 跑 → 跳 → 攻击 → 死亡 |
| **TCP 连接** | LISTEN → SYN_RCVD → ESTABLISHED → CLOSED |
| **UI 按钮** | 默认 → 悬停 → 按下 → 禁用 |
| **任务调度** | 等待 → 运行 → 完成 / 失败 |
| **音视频播放器** | 停止 → 加载 → 播放 → 暂停 |

**任何"有明确生命周期"的业务都是状态模式的候选**。

---

## 🎁 增强版：**状态转换矩阵**

高级用法 —— 用 Map 定义状态转换表，**数据驱动**：

```java
Map<State, Map<Event, State>> transitions = Map.of(
    PendingPayment, Map.of(
        PAY_EVENT, PAID,
        CANCEL_EVENT, CANCELLED
    ),
    PAID, Map.of(
        SHIP_EVENT, SHIPPED,
        CANCEL_EVENT, CANCELLED
    ),
    ...
);

void fire(Event e) {
    State next = transitions.get(currentState).get(e);
    if (next != null) {
        currentState = next;
    } else {
        throw new IllegalStateException();
    }
}
```

**更灵活**：状态转换规则变了**只改配置，不改代码**。适合大型工作流引擎。

---

## ⚠️ 什么时候别用

### 🚫 状态只有 2-3 个且简单
`boolean isActive` 加几个 if 就够，不用搞状态模式。

### 🚫 状态之间没有明确流转
如果任意两个状态都能互转，不叫状态机，用别的模式。

### 🚫 状态转换太简单
"已初始化 vs 未初始化" 这种，`boolean` 够用。

---

## 📝 思考题 & 小练习

### 思考题

1. 状态模式里的**状态对象**通常是单例还是每次 new？（答：可以单例，因为状态类通常无实例数据。但每次 new 也不错，简单）
2. 如果一个订单**可以同时处于多个状态**（比如"已付款 + 处理中"）怎么办？（答：改成组合状态，或拆成多个独立状态机）
3. 状态模式和**有限状态机（FSM）**什么关系？（答：状态模式是 FSM 的面向对象实现）

### 小练习

**练习：加"已取消"到"已退款"的转换**

扩展 `StateDemo.java`：
- 加一个 `Refunded` 状态
- `Cancelled` 可以通过 `refund()` 转到 `Refunded`
- 其他状态不能 refund

观察：加新状态只需**加一个类 + 改两个相关状态**，Order（Context）一行不动。

---

## 🏁 学完后

- **"懂了，下一课"** → 第 20 课 · 备忘录 Memento（游戏存档 / Ctrl+Z 的底层）
- **"State vs Strategy 再讲讲"** → 我再对比
- **"先 commit"** → 我帮你

**状态模式是"状态机"的优雅实现**。以后遇到"订单流转 / 工作流 / 游戏角色"都可以考虑 🎯
