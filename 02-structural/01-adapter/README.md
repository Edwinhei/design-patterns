# 第 6 课 · 适配器 Adapter ★

> 类型：结构型 | 难度：★ | GoF 经典 | **结构型开篇**

## 🎯 本课目标

- [x] 理解"**让不兼容的接口能协作**"是什么问题
- [x] 区分**对象适配器** vs **类适配器**（后者 Java 几乎不用）
- [x] 能在真实代码里**一眼认出** Adapter 模式

---

## 🎬 场景：出国出差，笔记本要充电

你去欧洲出差，带着笔记本。问题来了：

- 📱 你的笔记本 → **中国插头（三扁脚 / 两扁脚）**
- 🔌 酒店房间插座 → **欧标插座（两圆脚）**
- **两边都不能改**：笔记本厂商不会为你换插头，酒店不会换插座

怎么办？

**去买一个插头转换器**。一头接中国三脚，另一头变成两圆脚，插欧标插座。

**关键点**：
- 🎭 转换器**自己不发电**
- 🎭 转换器只是**中间层**，让两边"对得上"
- 🎭 转换器对**笔记本说"我是插座"**，对**插座说"我是两脚插头"**

**这就是适配器模式**。

---

## 🤔 代码场景的"痛点"

假如你在开发一个系统：

```java
// 你的系统用这个接口：只认"两脚插座"
interface EuropeanSocket {
    void provideTwoRoundPins();
}

// 但你有一个第三方的"中国笔记本"：
class ChineseLaptop {
    public void plugThreePinsCN() {
        // 提供三脚中国插头
    }
}
```

你想**让笔记本接上系统用电**，但：

```java
// 试试直接连？
EuropeanSocket socket = new ChineseLaptop();
//                      ↑
//    ❌ 编译错误：ChineseLaptop 不是 EuropeanSocket

socket.provideTwoRoundPins();   // 🚨 笔记本根本没这方法
```

**两边接口不兼容，编译期就过不去**。而你又不能：
- ❌ 修改 ChineseLaptop（第三方库，动不了）
- ❌ 修改 EuropeanSocket（你系统已经用了到处都是，改不起）

跑一下 [code/BadAdapter.java](code/BadAdapter.java) 直观感受。

---

## 💡 Adapter 模式登场

**核心思想**：**写一个中间类，它实现你要的接口，内部持有旧对象，把调用"翻译"过去**。

```java
// 适配器：对外实现 EuropeanSocket 接口
class PowerAdapter implements EuropeanSocket {
    private final ChineseLaptop laptop;         // 🎯 持有被适配对象

    public PowerAdapter(ChineseLaptop laptop) {
        this.laptop = laptop;
    }

    @Override
    public void provideTwoRoundPins() {         // 🎯 实现目标接口
        System.out.println("🔄 适配器内部转换...");
        laptop.plugThreePinsCN();                // 🎯 委托给旧对象
    }
}

// 使用
EuropeanSocket socket = new PowerAdapter(new ChineseLaptop());
socket.provideTwoRoundPins();     // ✅ 通了！
```

**适配器做的三件事**：
1. **`implements` 目标接口**（让客户端以为它是合规的"插座"）
2. **持有被适配对象**（内部藏着 ChineseLaptop）
3. **翻译调用**（method 里转换 → 调旧对象）

客户端代码**完全不知道** ChineseLaptop 存在。它以为自己在操作一个 EuropeanSocket。

跑 [code/AdapterDemo.java](code/AdapterDemo.java) 眼见为实。

---

## 📐 UML 结构

```
┌────────────────────┐
│  EuropeanSocket    │ ← Target（你想要的接口）
│  (interface)       │
├────────────────────┤
│ +provideTwoRound() │
└────────▲───────────┘
         │ implements
         │
┌────────┴───────────┐         ┌────────────────────┐
│  PowerAdapter      │──持有──→│  ChineseLaptop     │  ← Adaptee（已有的不兼容类）
├────────────────────┤         ├────────────────────┤
│ -laptop            │         │ +plugThreePinsCN() │
│ +provideTwoRound() │ 内部    │                    │
│   {laptop.plug...} │ 调用    └────────────────────┘
└────────────────────┘
```

**三个角色**：
- **Target（目标）** —— 客户端要的接口（EuropeanSocket）
- **Adaptee（被适配者）** —— 已有的不兼容类（ChineseLaptop）
- **Adapter（适配器）** —— 中间翻译者（PowerAdapter）

---

## 🔀 对象适配器 vs 类适配器

Adapter 有两种实现方式：

### 🥇 对象适配器（推荐，99% 用这个）

```java
class PowerAdapter implements EuropeanSocket {   // 实现目标接口
    private final ChineseLaptop laptop;           // 👈 组合：持有被适配对象

    public void provideTwoRoundPins() {
        laptop.plugThreePinsCN();
    }
}
```

**用组合**。灵活、解耦、Java 无限制。

### 🥈 类适配器（Java 里不常用）

```java
class PowerAdapter extends ChineseLaptop implements EuropeanSocket {
    //                      ↑ 继承被适配类
    public void provideTwoRoundPins() {
        plugThreePinsCN();                        // 直接调父类方法
    }
}
```

**用继承**。Java 不支持多继承 → 只能适配一个类 → 灵活性差。C++ 多继承环境下常见，Java 里几乎不用。

### 对比表

| 对比项 | 对象适配器（组合） | 类适配器（继承）|
|--------|----------------|---------------|
| 关系 | has-a（持有）| is-a（继承）|
| 可适配多个类 | ✅（持有多个对象）| ❌（单继承限制）|
| 能复写 Adaptee 的方法 | ❌（它在里面藏着）| ✅（父类方法可重写）|
| 耦合度 | 低 | 高 |
| Java 实战 | ★★★★★ | ★ |

**结论**：**Java 里几乎只用对象适配器**。后面看到 Adapter 默认都指它。

---

## 🌍 真实应用

| 在哪里 | 谁是适配器 |
|--------|-----------|
| **JDK** | `Arrays.asList(arr)` —— 把数组适配成 List 接口 |
| **JDK** | `InputStreamReader` —— 把字节流 `InputStream` 适配成字符流 `Reader` |
| **JDK** | `java.awt.event.WindowAdapter` —— 把 WindowListener 接口适配成默认空实现 |
| **Spring** | `HandlerAdapter` —— 把各种 Controller（@Controller / HttpRequestHandler / ...）统一适配成 Spring MVC 处理接口 |
| **SLF4J** | 整个 SLF4J 就是一个巨大的 Adapter 家族 —— 把 Log4j / Logback / JUL 等各种日志实现统一成 SLF4J 接口 |
| **Android** | `RecyclerView.Adapter` —— 把数据源适配成 RecyclerView 能理解的视图项 |

**发现没有**：**Adapter 是工作中最高频的模式之一**。每次你看到 `XxxAdapter` 类名，大概率就是这个模式。

---

## ⚠️ 什么时候别用

### 🚫 两边接口本来就兼容
没有"不兼容"这个痛点就别硬套，直接用就行。

### 🚫 你能改其中一边的代码
能改就直接改，不需要 Adapter。Adapter 是"两边都动不了"时的无奈之举。

### 🚫 想重新设计接口
Adapter 是"补丁"，不是"重构"。如果整体接口设计有问题，该重构就重构。

---

## 🎯 Adapter vs 其他"长得像"的模式

你会发现 Adapter / Facade / Decorator / Proxy **表面很像**（都是"包一层"），但用途不同：

| 模式 | 用途 |
|------|------|
| **Adapter**（本课）| **让不兼容接口能协作**（翻译）|
| **Facade**（下一课）| **简化复杂子系统**（提供一个总开关）|
| **Decorator** | **给对象动态加功能**（一层一层套）|
| **Proxy** | **控制对对象的访问**（权限 / 延迟加载 / 缓存）|

**关键辨别**：看**意图**，不是看**结构**。结构都是"包装一层"，意图完全不同。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么 Java 强烈推荐对象适配器，而不是类适配器？
2. `Arrays.asList(arr)` 返回的 List 是真正的 ArrayList 吗？（提示：不是，返回的是 Arrays$ArrayList，就是一个 Adapter）
3. 如果两边接口只差一点点（比如方法名不同、参数多一个），还需要 Adapter 吗？（答：需要，这恰恰是 Adapter 的典型场景）

### 小练习

**练习 1：日志适配器**
你有一段老代码用的是 `System.out.println`，新的系统要求用 `SLF4J Logger`。写一个 Adapter：
- Target: `Logger { void info(String msg); }`
- Adaptee: `System.out`
- Adapter: `SystemOutLogger implements Logger`

**练习 2：看 JDK 源码**
打开 JDK 源码，看 `InputStreamReader.java`，找出它的：
- Target 是什么接口
- Adaptee 是什么
- 是对象适配器还是类适配器

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 7 课 · 外观 Facade（一键关全屋电器）
- **"XXX 不懂"** → 告诉我
- **"做了练习"** → 贴代码 review

**Adapter 是结构型最简单也是最常用的模式**。学完它你会立刻在工作代码里发现"哎这里就是 Adapter"。接下来的结构型 6 个模式会建立你对"**对象如何组合**"的完整认知 🙌
