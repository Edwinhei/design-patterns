# 第 7 课 · 外观 Facade ★

> 类型：结构型 | 难度：★ | GoF 经典

## 🎯 本课目标

- [x] 理解"**给复杂子系统提供一个简化门面**"是什么意思
- [x] 看清 **Facade 和 Adapter** 的本质区别
- [x] 能在真实代码（Spring / JDK）里找出 Facade

---

## 🎬 场景：智能家居的"回家模式"

你刚搬进一套装了智能家居的房子。家里有一堆设备：

- 💡 智能灯
- ❄️ 空调
- 🔒 电子门锁
- 🪟 电动窗帘
- 🎵 音响系统

每次回家，你要**依次**：

```
1. 掏手机开锁 App → 解锁门
2. 开另一个 App → 开灯 → 调亮度到 80%
3. 又一个 App → 空调开机 → 调到 24 度
4. 另一个 App → 窗帘拉开
5. 最后一个 App → 音响开机 → 音量 30%
```

**5 个 App，10 多次操作**。晚上到家累得要死还要一个个点，烦得要命。

## 💡 你想要的理想状态

```
进门 → 按一下"**回家模式**"按钮 → 所有设备自动到位
```

**这就是 Facade 模式**：**把一堆复杂的子系统操作，包装成一个简单的开关**。

---

## 🤔 土办法：客户端管所有子系统

```java
// 客户端代码（一团乱麻）
SmartLight light = new SmartLight();
AirCon ac = new AirCon();
DoorLock lock = new DoorLock();
Curtain curtain = new Curtain();
MusicPlayer music = new MusicPlayer();

// 回家流程（一大堆操作）
lock.unlock();
light.turnOn();
light.setDim(80);
ac.powerOn();
ac.setTemp(24);
curtain.open();
music.start();
music.setVolume(30);
```

跑一下 [code/BadFacade.java](code/BadFacade.java) 感受痛点。

**问题**：
- 🙁 客户端要**了解所有子系统**（5 个对象 + 它们的所有方法）
- 🙁 **顺序 / 参数**全靠客户端自己记（"先开门还是先开灯"）
- 🙁 **同样的流程散落各处**（每个客户端代码都要重复写一遍）
- 🙁 **子系统升级**（比如灯换新型号），所有调用方都要改

**本质问题**：**客户端和子系统紧耦合**。

---

## 💡 Facade 模式登场

**核心思想**：**造一个"前台类"，封装所有子系统调用**，客户端只和前台打交道。

```java
class SmartHomeFacade {
    private final SmartLight light;
    private final AirCon ac;
    private final DoorLock lock;
    private final Curtain curtain;
    private final MusicPlayer music;

    public SmartHomeFacade() {
        this.light = new SmartLight();
        this.ac = new AirCon();
        this.lock = new DoorLock();
        this.curtain = new Curtain();
        this.music = new MusicPlayer();
    }

    // 🎯 一键模式：封装所有子系统调用
    public void arriveHome() {
        lock.unlock();
        light.turnOn();
        light.setDim(80);
        ac.powerOn();
        ac.setTemp(24);
        curtain.open();
        music.start();
        music.setVolume(30);
    }

    public void leaveHome() {
        music.stop();
        ac.powerOff();
        light.turnOff();
        curtain.close();
        lock.lock();
    }
}

// 客户端（极简）
SmartHomeFacade home = new SmartHomeFacade();
home.arriveHome();      // 就这一行
```

**客户端三行代码搞定之前的 10 行**。

### Facade 做的事

1. **持有子系统对象**（灯、空调、门锁等）
2. **提供简单的一键方法**（arriveHome / leaveHome / sleepMode）
3. **方法内按正确顺序调子系统**

**客户端再也不用管子系统**。需要修改流程？改 Facade 一处，所有调用方受益。

跑一下 [code/FacadeDemo.java](code/FacadeDemo.java)，感受"一键 vs 散装"的差异。

---

## 📐 UML 结构

```
┌────────────────┐
│   客户端代码    │
└───────┬────────┘
        │ 只和 Facade 打交道
        ↓
┌────────────────────────────────────┐
│      SmartHomeFacade（外观）        │
│ + arriveHome()                      │
│ + leaveHome()                       │
│ + sleepMode()                       │
└────┬──┬──┬──┬──┬──────────────────┘
     │  │  │  │  │
     │  │  │  │  └─→ MusicPlayer
     │  │  │  └────→ Curtain
     │  │  └───────→ DoorLock
     │  └──────────→ AirCon
     └─────────────→ SmartLight
     （所有子系统被 Facade 统一调度）
```

**关键观察**：**客户端只指向 Facade**，子系统都藏在 Facade 里面。

---

## 🔀 Facade vs Adapter —— 最易混淆的一对

都是"**包一层**"，意图完全不同：

| | Adapter | Facade |
|--|---------|--------|
| 目的 | 让**不兼容接口能协作** | 让**复杂子系统简单使用** |
| 包谁 | 一个不兼容的类 | 一群子系统 |
| 改变接口 | ✅ 强制要符合目标接口 | 🔸 可改可不改，主要是简化 |
| 子系统数量 | 1 个 | 通常多个 |
| 典型句子 | "让 A 用起来像 B" | "把 A/B/C/D 统一成一个简单入口" |

### 一句话辨别
- **Adapter**：**"翻译器"**（让两边能对话）
- **Facade**：**"前台 / 接待员"**（简化复杂事务）

---

## 🌍 真实应用

| 在哪里 | 谁是 Facade |
|--------|------------|
| **JDK** | `java.net.URL` —— 隐藏 Socket / 协议解析 / 字节流等一堆细节 |
| **JDK** | `javax.faces.context.FacesContext` —— JSF 的上下文门面 |
| **Spring** | `JdbcTemplate` —— 隐藏 Connection / Statement / ResultSet / Exception 一堆底层 JDBC |
| **Spring** | `RestTemplate` —— 隐藏 HttpClient 一堆细节 |
| **SLF4J** | `Logger` 接口 —— 统一门面，底层可换 Log4j / Logback / JUL |
| **操作系统** | 系统调用 API（如 `open()` / `read()`）—— 隐藏内核实现 |
| **JVM** | JDBC 的 `DataSource` —— 隐藏连接池管理 |
| **前端** | jQuery `$.ajax()` —— 包装 XMLHttpRequest 一堆麻烦 |

**规律**：**凡是你看到 "一行代码做了一堆事"，背后大概率是 Facade**。

---

## 🎯 Facade 的两种层次

### 层次 1：**简化调用**（最常见）

```java
jdbcTemplate.queryForList("SELECT * FROM users");
// 背后自动：取连接 / 创建 Statement / 执行 / 读结果 / 处理异常 / 关连接
```

一行代替一堆样板代码。

### 层次 2：**作为统一入口 / 隔离层**

```java
// 业务代码
public class OrderService {
    private final ExternalPaymentFacade payment;

    public void placeOrder(Order o) {
        payment.pay(o);   // 不关心微信支付 / 支付宝 / 银联
    }
}

// ExternalPaymentFacade 内部根据配置调不同支付网关
```

业务代码完全不知道底层用哪家支付。**支付商变更** → 只改 Facade 内部，业务不动。

这其实是 **Facade + Strategy** 的组合拳。

---

## ⚠️ 什么时候别用

### 🚫 子系统本身就简单
只有 1-2 个类要调，搞 Facade 是过度设计。

### 🚫 客户端需要灵活操作子系统
如果调用方需要**精细控制**（自己决定开灯的亮度），Facade 反而是障碍。

### 🚫 Facade 变成超级大类
如果 Facade 里堆了几十个方法，说明**子系统职责不清**，该拆 Facade 或重构子系统。

---

## 🎁 `@Deprecated` 的朋友：**反模式 "God Object"**

如果你的 Facade 成长为"**什么都能做**"的超级类，就变成了反模式 **God Object（上帝对象）**：

```java
// 🚨 反模式
class SystemFacade {
    void login() {}
    void logout() {}
    void sendEmail() {}
    void chargeCard() {}
    void createOrder() {}
    void generateReport() {}
    void backupDatabase() {}
    // ... 100 个方法
}
```

这违反了**单一职责原则**。Facade 应该按**主题**拆分（`UserFacade`、`OrderFacade`、`PaymentFacade`），不是啥都塞一个类里。

---

## 📝 思考题 & 小练习

### 思考题

1. Facade 和**单例模式**组合起来有什么效果？（答：很常见，Facade 通常是单例，方便全局调用）
2. 如果我把 Facade 里的子系统声明 `public`，客户端还能直接访问子系统吗？（可以，但破坏了封装性）
3. Spring 的 `JdbcTemplate` 是 Facade 还是 Adapter？为什么？（答：主要是 Facade，简化 JDBC 使用）

### 小练习

**练习 1：家庭影院 Facade**
经典场景：
- 子系统：`Amplifier` / `DvdPlayer` / `Projector` / `Screen` / `Lights` / `PopcornPopper`
- 封装两个模式：`watchMovie()` / `endMovie()`

**练习 2：改造 FacadeDemo.java**
加一个新子系统 `WaterHeater`（热水器），并在 `arriveHome()` 里加一步"开热水器"。
观察：**客户端代码需不需要改**？（答：不需要）

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 8 课 · 代理 Proxy（明星的经纪人）
- **"Facade 和 Adapter 还是分不清"** → 我再单独讲
- **"做了练习"** → 贴代码 review

**Facade 是工作中最常用的模式之一**。学完后你会发现"**凡是一行代码做了一堆事**"背后多半是它 🙌
