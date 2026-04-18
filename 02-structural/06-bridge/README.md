# 第 11 课 · 桥接 Bridge ★★

> 类型：结构型 | 难度：★★ | GoF 经典 | **最抽象的结构型模式，需要一点"设计眼光"**

## 🎯 本课目标

- [x] 理解"**两个维度各自独立变化**"的问题是什么
- [x] 看清桥接模式和装饰器/适配器的区别
- [x] 能识别工作中"该用桥接"的场景

---

## 🎬 场景：遥控器和电视

你家里有几种组合：

**遥控器**（3 种）：
- 基础遥控 —— 开关机、换台
- 高级遥控 —— 加了静音 / 定时
- 智能遥控 —— 加了语音控制

**电视**（3 种）：
- 索尼
- 三星
- LG

每种遥控器要能**适配每种电视**（发送不同品牌的红外信号）。

---

## 🤔 土办法：为每种组合写一个类

```java
class BasicRemoteForSony { ... }
class BasicRemoteForSamsung { ... }
class BasicRemoteForLG { ... }

class AdvancedRemoteForSony { ... }
class AdvancedRemoteForSamsung { ... }
class AdvancedRemoteForLG { ... }

class SmartRemoteForSony { ... }
class SmartRemoteForSamsung { ... }
class SmartRemoteForLG { ... }

// 总共 3 × 3 = 9 个类
```

跑一下 [code/BadBridge.java](code/BadBridge.java)。

**痛点**：
- 🙁 **类爆炸**：M 种遥控器 × N 种电视 = **M × N 个类**
- 🙁 加一个品牌（夏普）→ **要加 3 个类**（基础/高级/智能 × 夏普）
- 🙁 加一种遥控器（游戏遥控）→ **要加 3 个类**（游戏 × 索尼/三星/LG）
- 🙁 变化乘法累积

**本质问题**：**"遥控器功能"和"电视品牌"两个维度被**耦合**在一起**。

---

## 💡 桥接模式登场

**核心思想**：**把"两个独立变化的维度"拆开，用组合关系连接（"架一座桥"）**。

### 两个维度

- **抽象维度**：遥控器（功能）—— 基础/高级/智能
- **实现维度**：电视（设备）—— 索尼/三星/LG

**每个维度独立演化，互不影响**。

### 代码结构

```java
// === 实现维度：电视接口 ===
interface TV {
    void on();
    void off();
    void setChannel(int n);
}

class SonyTV implements TV { ... }
class SamsungTV implements TV { ... }
class LGTV implements TV { ... }

// === 抽象维度：遥控器（持有电视）===
abstract class RemoteControl {
    protected TV tv;              // 🌉 桥！持有实现维度

    public RemoteControl(TV tv) {
        this.tv = tv;
    }

    public void turnOn()  { tv.on(); }
    public void turnOff() { tv.off(); }
    // 具体按键逻辑由子类实现
}

// 基础遥控
class BasicRemote extends RemoteControl {
    public BasicRemote(TV tv) { super(tv); }

    public void setChannel(int n) {
        tv.setChannel(n);
    }
}

// 高级遥控（多了 mute 方法）
class AdvancedRemote extends RemoteControl {
    public AdvancedRemote(TV tv) { super(tv); }

    public void mute() {
        System.out.println("静音");
        tv.setChannel(0);
    }
}
```

**使用**（任意组合）：

```java
TV sony = new SonyTV();
RemoteControl r1 = new BasicRemote(sony);        // 基础 × 索尼
RemoteControl r2 = new AdvancedRemote(sony);      // 高级 × 索尼

TV samsung = new SamsungTV();
RemoteControl r3 = new AdvancedRemote(samsung);   // 高级 × 三星
```

**关键威力**：
- 加新电视品牌（夏普） → **只加 1 个类**（SharpTV），遥控器不用动
- 加新遥控类型（游戏遥控）→ **只加 1 个类**（GameRemote），电视不用动
- **M + N 个类**代替 **M × N 个类**

跑 [code/BridgeDemo.java](code/BridgeDemo.java) 看效果。

---

## 🌉 "桥"的形象比喻

```
【抽象维度】              【实现维度】
  遥控器                      电视
    │                          │
    ├── BasicRemote            ├── SonyTV
    ├── AdvancedRemote         ├── SamsungTV
    └── SmartRemote            └── LGTV
           │                        │
           └──────── 桥 ───────────┘
               (遥控器持有电视)
```

**"桥"**：遥控器对象**持有**电视对象。两个体系通过这个"持有"关系连接。

两个体系**各自独立演化**，互相不知道对方细节。

---

## 📐 UML 结构

```
┌──────────────────┐    ┌───────────────┐
│ RemoteControl    │───▶│     TV        │  ← 抽象维度依赖实现维度
│ (抽象)           │    │  (接口)       │
├──────────────────┤    ├───────────────┤
│ #tv: TV          │    │ +on()         │
│ +turnOn()        │    │ +off()        │
│ +turnOff()       │    │ +setChannel() │
└────────▲─────────┘    └───────▲───────┘
         │                      │
   ┌─────┴──────┐           ┌───┴──────┐
   │            │           │          │
BasicRemote AdvancedRemote SonyTV  SamsungTV
```

**两个继承树，通过"组合"连接**。

---

## 🔀 桥接 vs 装饰器 vs 适配器（辨析）

三者都"涉及两个对象组合"，但意图完全不同：

| 模式 | 意图 | 结构 |
|------|------|------|
| **Adapter** | 让不兼容接口能协作 | A 包 B，改 B 的接口 |
| **Decorator** | 动态加功能 | A 包 B，接口相同，可堆叠 |
| **Bridge**（本课）| **两维度独立演化** | A 持有 B，两个继承树各自生长 |

### 最易混淆：Bridge vs Decorator

**Decorator** 强调"**堆叠**"：
```java
new Milk(new Whip(new Espresso()))   // 一层套一层
```

**Bridge** 强调"**两条独立的线**"：
```java
new AdvancedRemote(new SonyTV())     // 遥控器的线 × 电视的线
```

Decorator **同一维度**的功能叠加；Bridge **两个不同维度**各自演化。

---

## 🌍 真实应用

| 在哪里 | 桥接的两个维度 |
|--------|-------------|
| **JDBC** | `DriverManager` / `Connection`（抽象）↔ 各数据库驱动（实现）|
| **SLF4J** | SLF4J API（抽象）↔ Logback / Log4j / JUL（实现）|
| **Java AWT/Swing** | `Component`（抽象）↔ 各平台 Peer（实现）|
| **XML 解析** | DOM/SAX API ↔ 各解析器实现 |
| **操作系统 GUI** | 应用层 GUI ↔ 底层图形库（DirectX / OpenGL / Metal）|
| **JVM 语言互操作** | Kotlin / Scala 代码（抽象）↔ JVM 字节码（实现）|

**规律**：**凡是"API 标准 + 多个实现"的架构**，背后都是桥接思想。

---

## ⚠️ 什么时候别用

### 🚫 只有一个维度变化
只有"遥控器"变化，"电视"只有一种 → 直接继承就行。

### 🚫 两个维度强相关
比如"红色苹果 / 绿色香蕉"—— 品种和颜色强关联。不该拆开。

### 🚫 过度设计
为"**可能会有**多种实现"而提前桥接 → Don't pay for flexibility you don't need.

---

## 🧠 识别"该用桥接"的信号

如果你的需求里有这种描述：

- "**X 种 A × Y 种 B**，每种组合要写一个类"
- "**API 和实现**应该分离，能各自替换"
- "**平台无关**的抽象"

→ 考虑桥接。

---

## 📝 思考题 & 小练习

### 思考题

1. 桥接和"依赖倒置原则"什么关系？（答：高度吻合 —— 高层不依赖低层实现，都依赖抽象）
2. 如果一共只有 1 种遥控器 × 3 种电视，还需要桥接吗？（答：不需要，直接持有 TV 接口即可，桥接是针对"多 × 多"的情况）
3. SLF4J 的设计为什么是经典桥接？（答：API 定义 Logger 接口 = 抽象，各日志库 = 实现，用户代码只依赖 SLF4J）

### 小练习

**扩展 BridgeDemo.java**
- 加一个 `SharpTV`（夏普电视）→ 看加几个类
- 加一个 `GameRemote`（游戏遥控，多一个"截图"按键）→ 看加几个类
- 对比：如果用土办法要加几个？

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 12 课 · 享元 Flyweight（结构型收官，围棋棋子共享）
- **"这里抽象，再讲一遍"** → 我用别的例子再讲
- **"还有问题"** → 问

**桥接是"面向对象设计高阶技巧"**。识别出"两个独立变化的维度"需要经验，学完这课你会开始有这个眼光 👁
