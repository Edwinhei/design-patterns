# 第 4 课 · 抽象工厂 Abstract Factory ★★

> 类型：创建型 | 难度：★★ | GoF 经典

## 🎯 本课目标

- [x] 理解"产品族一致性"这个问题是什么
- [x] 看清楚 **抽象工厂** 和 **工厂方法** 的本质区别
- [x] 写出一个完整的抽象工厂结构

---

## 🎬 场景：宜家家具整套搭配

你要装修新家。宜家（IKEA）提供**两种风格**的家具：

- 🏡 **北欧风**：简约、浅色、木质 —— 北欧沙发 + 北欧椅子 + 北欧桌子
- 🏭 **工业风**：粗犷、深色、金属 —— 工业沙发 + 工业椅子 + 工业桌子

**每种风格都是一整套**：沙发、椅子、桌子必须**统一风格**。

**你能做的**：
- ✅ 全买北欧风 → 和谐
- ✅ 全买工业风 → 和谐
- ❌ 北欧沙发 + 工业椅子 + 北欧桌子 → 灾难（风格乱）

**问题**：怎么**从代码层面**保证客户拿到的是"**一整套**一致的家具"，而不是混搭？

---

## 🤔 土办法：客户自己负责拼

```java
class HomeDecorator {
    public void decorate() {
        Sofa sofa   = new NordicSofa();        // 客户自己 new
        Chair chair = new IndustrialChair();   // 🚨 风格混了！
        Table table = new NordicTable();       // 又换回北欧
        // ...
    }
}
```

**痛点**：
- 🙁 客户一不小心就**混搭**（一堆 `new` 散在各处，很难保证都是同一风格）
- 🙁 要**切换风格**（全屋从北欧改工业），得**找出所有 new 改一遍**
- 🙁 加新风格（比如"日式"）→ 客户所有地方都要加选择逻辑
- 🙁 没法在**运行时**根据用户选择动态切换

跑一下 [code/BadFurniture.java](code/BadFurniture.java) 感受。

---

## 💡 抽象工厂登场

**核心思想**：**用一个工厂造整族相关产品，保证同族内一致**。

```java
// 🏭 抽象工厂：声明"造什么家具"
interface FurnitureFactory {
    Sofa  createSofa();
    Chair createChair();
    Table createTable();
}

// 🏡 北欧风工厂：造出来的都是北欧风
class NordicFurnitureFactory implements FurnitureFactory {
    public Sofa  createSofa()  { return new NordicSofa(); }
    public Chair createChair() { return new NordicChair(); }
    public Table createTable() { return new NordicTable(); }
}

// 🏭 工业风工厂：造出来的都是工业风
class IndustrialFurnitureFactory implements FurnitureFactory {
    public Sofa  createSofa()  { return new IndustrialSofa(); }
    public Chair createChair() { return new IndustrialChair(); }
    public Table createTable() { return new IndustrialTable(); }
}
```

**客户代码**：

```java
class HomeDecorator {
    private final FurnitureFactory factory;

    public HomeDecorator(FurnitureFactory factory) {   // 依赖抽象工厂
        this.factory = factory;
    }

    public void decorate() {
        Sofa sofa   = factory.createSofa();    // 🎯 拿到的风格由工厂决定
        Chair chair = factory.createChair();   // 🎯 一定和 sofa 同风格
        Table table = factory.createTable();   // 🎯 一定和 sofa/chair 同风格
    }
}

// 使用
HomeDecorator nordic = new HomeDecorator(new NordicFurnitureFactory());
nordic.decorate();       // 全屋北欧风

HomeDecorator industrial = new HomeDecorator(new IndustrialFurnitureFactory());
industrial.decorate();   // 全屋工业风
```

**精髓**：
- 客户只知道 `FurnitureFactory` 接口，不知道具体是哪个工厂
- 一旦工厂确定，**整族产品必然一致**（不可能混搭）
- 切换风格 → **只换一个工厂参数**，业务代码完全不动

跑一下 [code/AbstractFactoryDemo.java](code/AbstractFactoryDemo.java)。

---

## 📐 UML 结构

```
┌──────────────────────┐          ┌─────────────┐
│  FurnitureFactory    │  造───→  │    Sofa     │
│  (抽象工厂)           │          └──────▲──────┘
├──────────────────────┤                 │
│ +createSofa(): Sofa  │          ┌──────┴────────┐
│ +createChair(): Chair│          │ NordicSofa    │
│ +createTable(): Table│          │ IndustrialSofa│
└──────────▲───────────┘          └───────────────┘
           │
┌──────────┼──────────────┐       ┌─────────────┐
│          │              │  造───→│   Chair     │
│  NordicFactory          │       └──────▲──────┘
│  IndustrialFactory      │              │ 子类：Nordic/Industrial
└─────────────────────────┘
                             …（Table 同理）
```

**三个维度**：
1. **抽象工厂** ↔ **具体工厂**（谁来造？）
2. **抽象产品** ↔ **具体产品**（造出来是啥？）
3. **产品族**（一个工厂造的所有产品属于同一族）

---

## 🎯 抽象工厂 vs 工厂方法

这是最常被问到的区别：

| 对比项 | 工厂方法 | 抽象工厂 |
|--------|---------|---------|
| 造多少种产品 | **1 种**（每个工厂只造一类产品） | **多种**（一个工厂造一**族**相关产品） |
| 新增产品种类 | 容易（新工厂子类 + 新产品）| 麻烦（要改所有具体工厂都加新方法）|
| 新增产品族 | 不适用 | 容易（加一个新具体工厂实现接口） |
| 典型场景 | 纽约店 vs 芝加哥店 都只造"披萨" | 宜家"北欧家具"=沙发+椅子+桌子整套 |

### 图解

```
工厂方法：
  PizzaStore → 造 Pizza
     ├── NYStore → NYPizza
     └── ChicagoStore → ChicagoPizza

抽象工厂：
  FurnitureFactory → 造 (Sofa, Chair, Table) 一整族
     ├── NordicFactory → (NordicSofa, NordicChair, NordicTable)
     └── IndustrialFactory → (IndustrialSofa, IndustrialChair, IndustrialTable)
```

### 一句话区分
- **工厂方法**："我造**这一种**产品，但子类决定具体造哪个变体"
- **抽象工厂**："我造**整族相关产品**，保证它们互相匹配"

---

## 🌍 真实应用

| 在哪里 | 谁是抽象工厂 |
|--------|-------------|
| **JDK** | `javax.xml.parsers.DocumentBuilderFactory` —— 造 XML 解析相关的一组对象 |
| **JDK** | `java.sql.Connection` —— 每个数据库驱动的 Connection 是一个小工厂，能造 Statement、PreparedStatement、CallableStatement |
| **Spring** | `AbstractBeanFactory` —— 依赖注入容器的核心 |
| **Swing / AWT** | **跨平台 UI 组件**的 Look & Feel —— 同一个主题下的 Button、Scrollbar、Menu 样式一致 |
| **Java NIO** | `Selector.open()` 内部根据操作系统选不同实现（epoll / kqueue / select）|

**经典场景总结**：
- 跨平台（Windows/Mac/Linux 各自一套组件）
- 数据库无关（MySQL/Oracle/PostgreSQL 各自的一套驱动对象）
- 主题切换（深色/浅色模式下整套 UI 控件切换）

---

## ⚠️ 什么时候别用

### 🚫 产品族很稳定，不会新增产品种类
加一个新的产品种类（比如宜家要加"灯具"）→ 所有具体工厂都要加 `createLamp()` 方法。**破坏开闭原则**。

如果产品种类经常变，抽象工厂会成为维护噩梦。

### 🚫 只有一种产品
明显用**工厂方法**更合适，不用抽象工厂。

### 🚫 产品族只有一个
只有"北欧风"一种，没别的选择 → 直接 `new` 或简单工厂够了。

### 🚫 产品之间没有"必须搭配"的约束
如果 sofa 和 chair 可以随便混搭（不用强制风格一致），抽象工厂的价值就削弱了，用工厂方法更轻量。

---

## 🧭 和其他模式的关系

```
简单工厂  — 最朴素的 "一个工厂 + switch" —— 非 GoF
   ↓
工厂方法  — 每个工厂造一种产品，子类决定变体 —— GoF ✅
   ↓
抽象工厂  — 每个工厂造一族产品，保证族内一致 —— GoF ✅（本课）
```

**演化路径**：从"造一个"→"造一种"→"造一族"。

---

## 📝 思考题 & 小练习

### 思考题

1. 抽象工厂为什么"加新产品种类难，加新产品族容易"？（答：看接口签名是否变化）
2. 抽象工厂和工厂方法可以组合使用吗？（答：可以，抽象工厂的每个 `createXxx` 方法内部可以用工厂方法实现）
3. Spring 的 `@Bean` 方法，算什么工厂？

### 小练习

**练习 1：加"日式风格"**
基于 `AbstractFactoryDemo.java`，加一个 `JapaneseFurnitureFactory`，造日式沙发、椅子、桌子。看看需要改多少代码。

**练习 2：数据库驱动工厂**
设计一个 `DatabaseFactory`，能造 `Connection`、`Statement`、`ResultSet`。提供 `MySQLDatabaseFactory` 和 `PostgreSQLDatabaseFactory` 两个实现。
（只写签名和伪代码，不用真连数据库）

**练习 3：加产品 vs 加产品族**
在 `AbstractFactoryDemo.java` 基础上，分别尝试：
- a) 加一个新产品种类"灯具 Lamp"
- b) 加一个新产品族"日式风格"
感受两种变化对已有代码的影响差异。

---

## 🏁 学完后

- **"都懂了，下一课"** → [第 5 课 · 原型 Prototype](../05-prototype/)
- **"这里不懂"** → 告诉我具体卡哪
- **"我做了练习"** → 贴代码来 review

抽象工厂是"**产品族一致性**"的守护神，学完你每次看到跨平台 / 跨数据库的代码会更懂它的设计动机 🏡 🏭
