# 第 13 课 · 模板方法 Template Method ★

> 类型：行为型 | 难度：★ | GoF 经典 | **行为型开篇**

## 🎯 本课目标

- [x] 理解"**父类定流程，子类填空**"
- [x] 掌握三个角色：**模板方法 / 抽象方法 / 钩子方法**
- [x] 识别 Spring / Servlet / Java IO 里的模板方法

---

## 🎬 场景：咖啡 vs 茶

在星巴克工作的人都知道，做**咖啡**和**茶**的流程**几乎一样**：

### 咖啡流程
```
1. 烧水
2. 用咖啡粉冲泡
3. 倒入杯中
4. 加糖和奶
```

### 茶流程
```
1. 烧水        ← 一样
2. 泡茶叶       ← 只有这里不同
3. 倒入杯中    ← 一样
4. 加柠檬       ← 只有这里不同
```

**观察**：4 步里**有 2 步一样**，只有 2 步按饮料类型不同。

---

## 🤔 土办法：两个类各写一遍

```java
class Coffee {
    public void prepare() {
        boilWater();         // 重复
        brew();              // Coffee：冲咖啡粉
        pourInCup();         // 重复
        addCondiments();     // Coffee：加糖奶
    }
    // ...
}

class Tea {
    public void prepare() {
        boilWater();         // 重复
        brew();              // Tea：泡茶叶
        pourInCup();         // 重复
        addCondiments();     // Tea：加柠檬
    }
    // ...
}
```

跑 [code/BadTemplate.java](code/BadTemplate.java)。

**痛点**：
- 🙁 `boilWater` 和 `pourInCup` 在两个类里**各写一遍**
- 🙁 如果"烧水"的流程变了（比如加个"煮开 5 分钟"），**两个类都要改**
- 🙁 加新饮料（热巧克力）→ 又要抄一遍骨架

**本质问题**：**流程共性**没有被抽出来。

---

## 💡 模板方法模式登场

**核心思想**：**父类定义"流程骨架"，把变化的步骤留给子类实现**。

```java
abstract class Beverage {

    // 🎯 模板方法：流程骨架（final 防止子类篡改流程）
    public final void prepare() {
        boilWater();                              // 步骤 1：共同
        brew();                                    // 步骤 2：子类实现
        pourInCup();                               // 步骤 3：共同
        addCondiments();                           // 步骤 4：子类实现
    }

    // 共同步骤（父类实现）
    private void boilWater() { System.out.println("🔥 烧水"); }
    private void pourInCup() { System.out.println("🫗 倒入杯中"); }

    // 变化步骤（子类实现）
    protected abstract void brew();
    protected abstract void addCondiments();
}

// 咖啡
class Coffee extends Beverage {
    protected void brew()            { System.out.println("☕ 冲咖啡粉"); }
    protected void addCondiments()   { System.out.println("🥛 加糖和奶"); }
}

// 茶
class Tea extends Beverage {
    protected void brew()            { System.out.println("🍵 泡茶叶"); }
    protected void addCondiments()   { System.out.println("🍋 加柠檬"); }
}
```

### 使用

```java
Beverage coffee = new Coffee();
coffee.prepare();
// 🔥 烧水
// ☕ 冲咖啡粉
// 🫗 倒入杯中
// 🥛 加糖和奶

Beverage tea = new Tea();
tea.prepare();
// 🔥 烧水
// 🍵 泡茶叶
// 🫗 倒入杯中
// 🍋 加柠檬
```

**精髓**：**父类定流程，子类填变化**。共同步骤不重复。

---

## 🧩 模板方法的三个角色

### 1. 模板方法（Template Method）—— 流程骨架
```java
public final void prepare() {
    boilWater();
    brew();
    pourInCup();
    addCondiments();
}
```
**加 `final`** —— 防止子类改流程（只能改具体步骤）。

### 2. 抽象方法（Abstract Method）—— 必须重写
```java
protected abstract void brew();
```
**子类必须实现**，不实现就编译错误。

### 3. 钩子方法（Hook Method）—— 可选重写 ⭐

有时候，某些步骤是**可选**的（比如"加料"这步，客户可能不要）：

```java
abstract class Beverage {
    public final void prepare() {
        boilWater();
        brew();
        pourInCup();
        if (customerWantsCondiments()) {   // 👈 钩子控制
            addCondiments();
        }
    }

    // 🪝 钩子方法：有默认实现，子类可选择覆盖
    protected boolean customerWantsCondiments() {
        return true;    // 默认要加料
    }

    protected abstract void brew();
    protected abstract void addCondiments();
}

// 不要加料的咖啡
class BlackCoffee extends Coffee {
    @Override
    protected boolean customerWantsCondiments() {
        return false;   // 覆盖钩子
    }
}
```

**钩子 = 给子类的"插入点"**，让它们影响父类流程。

---

## 📐 UML 结构

```
┌──────────────────────────────┐
│       Beverage (抽象)         │
├──────────────────────────────┤
│ +prepare() final              │ ← 模板方法（不可改）
│ -boilWater()                  │ ← 共同步骤
│ -pourInCup()                  │ ← 共同步骤
│ #brew() abstract              │ ← 子类必须实现
│ #addCondiments() abstract     │ ← 子类必须实现
│ #customerWantsCondiments()    │ ← 钩子（可选）
└──────────▲───────────────────┘
           │
     ┌─────┴─────┐
     │           │
┌─────────┐  ┌────────┐
│ Coffee  │  │  Tea   │
└─────────┘  └────────┘
```

---

## 🎯 你已经见过它了 —— 工厂方法

回忆 **第 2 课 · 工厂方法**：

```java
abstract class PizzaStore {
    public final Pizza orderPizza(String type) {   // 模板方法
        Pizza pizza = createPizza(type);   // 抽象方法：子类实现
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        return pizza;
    }

    protected abstract Pizza createPizza(String type);
}
```

**工厂方法其实是模板方法的一个特化** —— 模板方法里某一步是"创建对象"。

---

## 🌍 真实应用（非常多）

| 在哪里 | 模板方法 |
|--------|---------|
| **Spring** | `JdbcTemplate.execute` —— 模板定义"连接 → 执行 → 关闭"骨架，你只写 SQL |
| **Spring** | `AbstractController` —— 定义请求处理骨架，子类实现具体逻辑 |
| **Servlet** | `HttpServlet.service()` —— 模板分发到 `doGet` / `doPost`，你只写后者 |
| **Java** | `AbstractList` —— 定义骨架，具体实现由 ArrayList / LinkedList 填 |
| **JUnit** | `@Before` / `@Test` / `@After` 是钩子，框架定义了"测试生命周期骨架" |
| **Hibernate** | `HibernateTemplate` 同 JdbcTemplate |

**规律**：**凡是"框架定义骨架，用户填空"**的 API，背后都是模板方法。

---

## 🔀 模板方法 vs 策略模式（下一课）

两者都解决"**算法变化**"的问题，但方式不同：

| 模式 | 怎么实现变化 |
|------|-----------|
| **模板方法** | **继承**，子类重写某些步骤 |
| **策略** | **组合**，注入不同算法对象 |

**模板方法**：父子类关系，**整体流程固定，局部变化**
**策略**：平级组合，**整个算法可替换**

---

## ⚠️ 什么时候别用

### 🚫 只有 1-2 个子类
抽象继承不划算，直接写两个函数更简单。

### 🚫 流程完全不同
模板方法要求**流程主体相同**，只有某些步骤变。如果流程完全不同，用不了。

### 🚫 子类需要"改变流程顺序"
模板方法 = 顺序固定。需要调整顺序？用**策略**或**责任链**。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么模板方法要加 `final`？（答：防止子类改流程。子类只能改步骤，不能改骨架）
2. 钩子方法和抽象方法的区别？（答：钩子有默认实现，子类可选覆盖；抽象必须实现）
3. 模板方法和"好莱坞原则"的关系？（答："Don't call us, we'll call you" —— 父类调用子类方法，不是子类主动调父类）

### 小练习

**练习：加一个"热巧克力"**
基于 `TemplateDemo.java`，加一个 `HotChocolate` 类：
- brew：化可可粉
- addCondiments：加棉花糖
- 默认不加料（覆盖钩子）

运行 `new HotChocolate().prepare()` 看是否符合预期。

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 14 课 · 策略 Strategy（和模板方法一对好兄弟）
- **"模板方法 vs 策略再讲讲"** → 我对比展开
- **"先 commit"** → 我帮你

**模板方法是行为型里最简单的模式**。理解它为后面的策略/状态/命令等模式打基础 🎯
