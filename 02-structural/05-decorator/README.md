# 第 10 课 · 装饰器 Decorator ★★

> 类型：结构型 | 难度：★★ | GoF 经典 | **工作中使用频率极高**

## 🎯 本课目标

- [x] 理解"**不改原类，一层层包装加功能**"
- [x] 看清装饰器和代理/适配器/外观的区别
- [x] 能看懂 `new BufferedReader(new InputStreamReader(...))` 这种 Java IO 嵌套

---

## 🎬 场景：星巴克点单

你在星巴克点咖啡：

```
基础咖啡（选 1 种）：
  - 美式 $2.00
  - 拿铁 $3.50
  - 卡布奇诺 $3.00

加料（随意组合）：
  - 加奶 +$0.50
  - 加糖 +$0.10
  - 加奶油 +$0.80
  - 加焦糖 +$0.60
```

**组合爆炸**：
- 只有 3 种基础 + 4 种加料
- 但组合可能是 **3 × 2⁴ = 48 种**（每种加料选或不选）
- 客人还能加**多次**（比如两份奶）

**怎么写代码**既能算钱又能描述？

---

## 🤔 土办法 1：每种组合一个子类（类爆炸）

```java
class Espresso { ... }
class EspressoWithMilk { ... }
class EspressoWithSugar { ... }
class EspressoWithMilkAndSugar { ... }
class EspressoWithMilkAndSugarAndWhip { ... }
// ... 再加 40+ 个类
```

**痛点**：**几十个类**。加一种新饮料或新加料 → 要加一大堆子类。

## 🤔 土办法 2：父类加一堆布尔字段

```java
class Beverage {
    double baseCost;
    boolean hasMilk;
    boolean hasSugar;
    boolean hasWhip;
    boolean hasCaramel;

    public double cost() {
        double total = baseCost;
        if (hasMilk)    total += 0.50;
        if (hasSugar)   total += 0.10;
        if (hasWhip)    total += 0.80;
        if (hasCaramel) total += 0.60;
        return total;
    }
}
```

跑 [code/BadDecorator.java](code/BadDecorator.java) 感受。

**痛点**：
- 🙁 加新配料（蜂蜜）→ 改父类 `Beverage` 和 `cost` 方法（**违反开闭原则**）
- 🙁 所有饮料都自带一堆 `boolean` 字段（哪怕这杯不用）
- 🙁 `cost` 方法越来越长（if 链不断变长）
- 🙁 **不能加两次**（`hasMilk` 只有 true/false）

---

## 💡 装饰器模式登场

**核心思想**：**把每种"加料"变成一个"装饰器类"，它包裹一杯饮料，加一点功能**。

```java
// 抽象组件
abstract class Beverage {
    public abstract String getDescription();
    public abstract double cost();
}

// 具体饮料
class Espresso extends Beverage {
    public String getDescription() { return "美式"; }
    public double cost() { return 2.00; }
}

// 装饰器基类：也是 Beverage
abstract class CondimentDecorator extends Beverage {
    protected Beverage beverage;        // 持有被装饰的饮料
    public CondimentDecorator(Beverage b) { this.beverage = b; }
}

// 具体装饰器
class Milk extends CondimentDecorator {
    public Milk(Beverage b) { super(b); }

    public String getDescription() {
        return beverage.getDescription() + " + 奶";    // 叠加描述
    }

    public double cost() {
        return beverage.cost() + 0.50;                   // 叠加价格
    }
}

class Whip extends CondimentDecorator {
    // 同样的套路
}
```

**使用**：

```java
// 一杯美式 + 奶 + 奶油 + 两份焦糖
Beverage drink = new Espresso();
drink = new Milk(drink);        // 包一层奶
drink = new Whip(drink);        // 再包一层奶油
drink = new Caramel(drink);     // 再包一层焦糖
drink = new Caramel(drink);     // 再包一层焦糖（双份）

System.out.println(drink.getDescription());    // "美式 + 奶 + 奶油 + 焦糖 + 焦糖"
System.out.println(drink.cost());              // 2.00 + 0.50 + 0.80 + 0.60 + 0.60 = 4.50
```

**精髓**：**一层一层包装**，每层加一点功能。

跑 [code/DecoratorDemo.java](code/DecoratorDemo.java) 看效果。

---

## 📐 装饰器结构（重点）

```
┌───────────────────────┐
│  Beverage (抽象)      │ ← Component 统一接口
├───────────────────────┤
│ + cost()              │
│ + getDescription()    │
└──────────▲────────────┘
           │
    ┌──────┴────────────┐
    │                   │
┌──────────┐   ┌─────────────────────┐
│ Espresso │   │ CondimentDecorator  │ ← 装饰器基类（也是 Beverage）
│ (具体)   │   ├─────────────────────┤
└──────────┘   │ Beverage beverage   │ ← 持有一个 Beverage
               └──────────▲──────────┘
                          │
                ┌─────────┴──────────┐
                │                    │
           ┌────────┐            ┌────────┐
           │  Milk  │            │  Whip  │  ← 具体装饰器
           └────────┘            └────────┘
```

**关键洞察**：
- 装饰器**也是 Beverage**（继承自抽象组件）
- 装饰器**持有一个 Beverage**（被它装饰的对象）
- 所以装饰器可以**一层套一层**（因为每层都是 Beverage）

**层层包装的数学表达**：

```java
new Milk(new Whip(new Caramel(new Espresso())))
         ↑          ↑             ↑
        Milk    Milk 装饰     Milk 装饰（Whip 装饰
                 的 Whip      的 Caramel（的 Espresso）)
```

---

## 🔀 装饰器 vs 结构型其他模式（关键辨析）

| 模式 | 接口 | 关系 | 目的 |
|------|------|------|------|
| **Adapter** | **不同** | 一对一翻译 | 让不兼容接口能协作 |
| **Facade** | **可不同** | 聚合多个 | 简化复杂子系统 |
| **Proxy** | **相同** | 一对一 | 控制访问（权限/事务/缓存）|
| **Decorator**（本课）| **相同** | **一对多，可堆叠** | 动态加功能 |

**最易混淆：Decorator vs Proxy**
- Proxy：**一层**，关心"**控制**"（开事务、查权限）
- Decorator：**多层**，关心"**增强**"（加功能，能叠加）

---

## 🌍 真实应用（超多！）

### Java IO 流 —— 最经典的装饰器应用

```java
// 从文件读 → 加缓冲 → 解压缩 → 按字符读
Reader r = new BufferedReader(
              new InputStreamReader(
                new GZIPInputStream(
                  new FileInputStream("data.gz")
                )
              )
           );
```

一层套一层，每层加一个能力：
- `FileInputStream` — 基础字节读取
- `GZIPInputStream` — 加解压
- `InputStreamReader` — 字节 → 字符
- `BufferedReader` — 加缓冲

**Java IO 整个设计就是装饰器**。

### Java 集合

```java
List<String> list = new ArrayList<>();
List<String> sync = Collections.synchronizedList(list);    // 加线程安全
List<String> immutable = Collections.unmodifiableList(sync); // 加不可变
```

### 其他

| 地方 | 装饰器 |
|------|-------|
| Spring | `TransactionAwareCacheDecorator` |
| OkHttp | `Interceptor` 链 |
| Servlet | `HttpServletRequestWrapper`（装饰 request）|
| Tomcat | `StandardWrapper`（装饰 Servlet）|

---

## ⚠️ 什么时候别用

### 🚫 功能组合少，子类化足够
只有 2-3 种组合，写 2-3 个子类比装饰器简单。

### 🚫 装饰器逻辑过重
装饰器应该**轻量叠加**。如果一个装饰器干了很多活，考虑重构为独立类。

### 🚫 顺序敏感
装饰器堆叠的顺序**可能影响结果**（比如"先压缩后加密"和"先加密后压缩"不同）。
如果顺序极其敏感，装饰器模式可能不适合。

---

## 📝 思考题 & 小练习

### 思考题

1. 装饰器和代理模式**结构非常像**，最大区别在哪？（答：堆叠性 + 意图）
2. 为什么装饰器基类要 `extends Beverage` 而不是 `implements Beverage`？（都可以，用继承是因为可以复用 Beverage 的字段/方法）
3. 装饰器的顺序重要吗？什么情况下重要？（答：有时重要，比如"压缩+加密"）

### 小练习

**练习 1：加新装饰器**
基于 `DecoratorDemo.java`，加一个**蜂蜜** Honey 装饰器（+$0.30）。观察：需不需要改 `Beverage` / `Espresso` / 其他装饰器？（答：都不用改，只加一个 `Honey` 类）

**练习 2：不同基础饮料**
试着用**拿铁** `Latte` 为基础，加奶、加焦糖。看看描述和价格是否合理。

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 11 课 · 桥接 Bridge（遥控器和电视分别演化）
- **"还有问题"** → 问
- **"先 commit"** → 我帮你

**装饰器是工作中最常用的模式之一**。学完后你会在代码里**到处发现它**（Java IO / Servlet Filter / Spring AOP 的某些形态 / OkHttp / Netty ...）🙌
