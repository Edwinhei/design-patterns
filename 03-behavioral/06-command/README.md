# 第 18 课 · 命令 Command ★★

> 类型：行为型 | 难度：★★ | GoF 经典 | **undo/redo 的基石**

## 🎯 本课目标

- [x] 理解"**把请求封装成对象**"的价值
- [x] 掌握 Command 模式的五个角色
- [x] 实现 **undo/redo** 功能

---

## 🎬 场景：餐厅点单

你去餐厅吃饭：
- 👤 **客人**（Client）：想吃什么
- 👨‍🍳 **厨师**（Receiver）：真正做菜的人
- 📋 **小票**（Command）：写清楚"做一份牛排"的纸条
- 🧑‍💼 **服务员**（Invoker）：把小票交给厨房

**关键观察**：客人**不直接对厨师喊话**，而是**开一张小票**。这张小票就是"请求的对象化"。

### 为什么要写小票？

- 📜 **可以排队**（按点单顺序做）
- 📝 **可以记录**（菜单历史、账单）
- ↩️ **可以取消**（客人改主意）
- 👥 **厨师不认识客人**（解耦）

---

## 🤔 土办法：客人直接叫厨师

```java
class Customer {
    Chef chef;

    void orderSteak() {
        chef.cookSteak();      // 客人直接调厨师方法
    }
}
```

跑 [code/BadCommand.java](code/BadCommand.java)。

**痛点**：
- 🙁 客人**直接调用**厨师方法 → 紧耦合
- 🙁 **没法排队**（请求即执行）
- 🙁 **没法取消**（请求已发出，没有"对象"可以撤销）
- 🙁 **没法记日志**（没留痕迹）

---

## 💡 命令模式登场

**核心思想**：**把"请求"封装成对象（Command）**，客人只需"开小票"，服务员负责交给厨师。

```java
// 命令接口
interface OrderCommand {
    void execute();     // 执行
    void undo();        // 撤销
    String describe();
}

// 接收者（真正做事的人）
class Chef {
    public void cookSteak()  { System.out.println("🥩 做牛排"); }
    public void cookPasta()  { System.out.println("🍝 做意面"); }
    public void cancelDish(String dish) { System.out.println("❌ 撤销 " + dish); }
}

// 具体命令
class SteakOrder implements OrderCommand {
    private final Chef chef;
    public SteakOrder(Chef chef) { this.chef = chef; }

    public void execute()   { chef.cookSteak(); }
    public void undo()      { chef.cancelDish("牛排"); }
    public String describe() { return "牛排"; }
}

// 调用者（服务员，维护订单历史）
class Waiter {
    private final Deque<OrderCommand> history = new ArrayDeque<>();

    public void placeOrder(OrderCommand cmd) {
        cmd.execute();
        history.push(cmd);
    }

    public void cancelLast() {
        if (!history.isEmpty()) {
            OrderCommand last = history.pop();
            last.undo();
        }
    }
}
```

### 使用

```java
Chef chef = new Chef();
Waiter waiter = new Waiter();

waiter.placeOrder(new SteakOrder(chef));         // 点牛排
waiter.placeOrder(new PastaOrder(chef));         // 点意面

waiter.cancelLast();                              // 客人改主意，取消意面
```

跑 [code/CommandDemo.java](code/CommandDemo.java) 看完整演示。

---

## 🧩 Command 模式的 5 个角色

| 角色 | 本例对应 |
|------|---------|
| **Command**（接口）| `OrderCommand` |
| **ConcreteCommand**（具体命令） | `SteakOrder` / `PastaOrder` |
| **Receiver**（接收者，真正做事） | `Chef` |
| **Invoker**（调用者） | `Waiter` |
| **Client**（组装者） | `main` 方法 |

**关键解耦**：**Invoker（服务员）不知道 Receiver（厨师）的细节**，只知道有个 `execute()` 可调。

---

## 📐 UML 结构

```
┌─────────────┐             ┌────────────────┐
│   Client    │ 创建 ───────▶│ ConcreteCommand│
└─────────────┘             │ +execute()     │
                            │ +undo()        │
                            └───────┬────────┘
                                    │ 持有
                                    ▼
                            ┌────────────────┐
                            │  Receiver      │ ← Chef
                            │ +cookSteak()   │
                            │ +cancel...     │
                            └────────────────┘

┌─────────────┐ 持有 ┌────────────────┐
│  Invoker    │────▶│ Command (接口) │
│  Waiter     │     └────────▲───────┘
└─────────────┘              │
   (持有 Deque<Command>)      │ 实现
                        ┌────┴─────┐
                   SteakOrder  PastaOrder
```

---

## 🎁 undo/redo 的核心机制

**undo 的秘密** = **两个栈**：

```java
Deque<Command> undoStack = new ArrayDeque<>();
Deque<Command> redoStack = new ArrayDeque<>();

void execute(Command cmd) {
    cmd.execute();
    undoStack.push(cmd);
    redoStack.clear();         // 新操作清空 redo
}

void undo() {
    if (!undoStack.isEmpty()) {
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
    }
}

void redo() {
    if (!redoStack.isEmpty()) {
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
    }
}
```

**Word / IDE 的 Ctrl+Z / Ctrl+Y 就是这个机制**。

---

## 🌍 真实应用

| 场景 | 命令是什么 |
|------|----------|
| **文本编辑器** | 每次输入/删除 = 一个 Command（支持 undo）|
| **IDE 重构** | 每个重构动作是 Command |
| **数据库事务** | SQL 语句就是命令，可回滚 |
| **GUI 按钮** | 按钮关联一个 Command 对象 |
| **任务队列** | Runnable / Callable 是命令 |
| **线程池** | submit(Runnable) —— Runnable 就是命令 |
| **Redux** | dispatch(action) —— action 是命令 |
| **游戏录像回放** | 玩家操作录下来重放 = 命令重播 |
| **宏（Macro）** | 多个命令组合成一个大命令 |

---

## 🎯 Java 中的天然命令：`Runnable`

```java
Runnable task = () -> System.out.println("做事");

// 放进线程池
executor.submit(task);    // task 就是一个 Command

// 延迟执行
scheduler.schedule(task, 5, TimeUnit.SECONDS);

// 队列
queue.offer(task);
```

**Runnable 是 Java 最经典的命令接口**：单方法（`run()`），封装一次操作。

---

## 🔀 Command 在设计模式里的独特性

Command **是唯一一个"把方法调用封装成对象"的模式**。

其他模式都是"**造对象**"，Command 是"**造动作对象**"。

这让 Command 有其他模式没有的能力：
- ✅ 排队
- ✅ 撤销
- ✅ 记录
- ✅ 事务
- ✅ 宏（组合多个命令）

---

## ⚠️ 什么时候别用

### 🚫 操作简单不需要记录/撤销
直接调方法就行，搞 Command 是过度设计。

### 🚫 只有一种操作
写接口 + 实现类太重，直接调最快。

### 🚫 性能极敏感
每次创建 Command 对象有少量开销，千万级 QPS 场景要评估。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么 `Runnable` 可以看作 Command 的简化版？（答：单方法接口，封装一次操作，可以排队/传递）
2. Command 和 Strategy 都把"行为"封装成对象，区别在哪？（答：Strategy 聚焦"算法可替换"，Command 聚焦"请求可记录/撤销/排队"）
3. 数据库事务里的 SQL，怎么理解为 Command？（答：每条 SQL 是一个 Command，事务 = Command 队列，回滚 = undo）

### 小练习

**练习：实现 redo**
基于 `CommandDemo.java`，给 `Waiter` 加一个 `redoLast()` 方法。用两个栈实现：undoStack + redoStack。

---

## 🏁 学完后

- **"懂了，下一课"** → 第 19 课 · 状态 State（订单流转）
- **"Redux / Redo 想深入"** → 以后可以专题
- **"先 commit"** → 我帮你

**Command 是"让动作变成对象"的模式**。undo/redo / 任务队列 / 事务 全靠它 🎯
