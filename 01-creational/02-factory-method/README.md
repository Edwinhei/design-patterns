# 第 2 课 · 工厂方法 Factory Method ★

> 类型：创建型 | 难度：★ | GoF 经典

## 🎯 本课目标

学完这一课，你能做到：

- [x] 说清楚"代码里满天飞的 `new`"为什么是个问题
- [x] 理解 **简单工厂** 和 **工厂方法** 的区别（前者不是 GoF，后者是）
- [x] 能独立写出一个工厂方法结构的代码

---

## 🎬 场景：开披萨连锁店

老王开了家披萨店，生意红火，开始想扩张：

### 第 1 阶段：单店 + 多种披萨
一家店，卖 3 种披萨：
- 🍕 芝士披萨 Cheese
- 🍕 辣香肠披萨 Pepperoni
- 🍕 素菜披萨 Veggie

客人点哪种，就**当场 `new` 一个**对应的披萨对象。

### 第 2 阶段：开连锁店
生意做大，在纽约和芝加哥开了分店。问题来了：

- 纽约人喜欢**薄饼披萨**
- 芝加哥人喜欢**厚底披萨（深盘）**

同样是"芝士披萨"，两家店做出来**完全不一样**。如果用 `new CheesePizza()` 去搞，纽约店是纽约风、芝加哥店是芝加哥风 —— 怎么让**同一个下单流程**适配两家店？

---

## 🤔 第一步：跑"土办法"版本看痛点

打开 [code/BadPizzaStore.java](code/BadPizzaStore.java)，跑一下：

```bash
cd 01-creational/02-factory-method/code
java Ba<Tab>      # 补全成 java BadPizzaStore.java
```

代码核心：

```java
class PizzaOrderSystem {
    public Pizza orderPizza(String type) {
        Pizza pizza;
        if (type.equals("cheese"))         pizza = new CheesePizza();
        else if (type.equals("pepperoni")) pizza = new PepperoniPizza();
        else if (type.equals("veggie"))    pizza = new VeggiePizza();
        else throw new IllegalArgumentException("未知披萨: " + type);

        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        return pizza;
    }
}
```

看起来没毛病。**但想一想**：

- 🙁 **要加一种"海鲜披萨"** → 必须改 `orderPizza` 方法（违反**开闭原则**）
- 🙁 **要开纽约店**，同样的订单流程但做出纽约风披萨 → 新代码？复制粘贴一份？
- 🙁 **`orderPizza` 里既管流程又管创建** → 职责不清

**关键痛点**：**`new` 语句到处都是，和业务流程强耦合**。披萨种类一变，业务流程的代码就跟着改。

---

## 💡 进化一：简单工厂（Simple Factory）

把"造披萨"这件事**抽到一个专门的类**里：

```java
class SimplePizzaFactory {
    public Pizza createPizza(String type) {     // 把 if-else 从业务代码挪到这
        if (type.equals("cheese"))         return new CheesePizza();
        else if (type.equals("pepperoni")) return new PepperoniPizza();
        else if (type.equals("veggie"))    return new VeggiePizza();
        throw new IllegalArgumentException("未知披萨: " + type);
    }
}

class PizzaStore {
    private SimplePizzaFactory factory;

    public Pizza orderPizza(String type) {
        Pizza pizza = factory.createPizza(type);   // 🎯 流程里不再直接 new
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        return pizza;
    }
}
```

### ✅ 改进
- 职责分离：PizzaStore 管卖，SimplePizzaFactory 管造
- 要开分店，可以传不同的 factory

### 🙁 还不够
- **加新披萨还是要改 `SimplePizzaFactory.createPizza`** —— if-else 链只是搬了家
- **仍然违反开闭原则**
- **不是 GoF 官方 23 种模式之一**（很多人容易把这个当"工厂方法"，其实不是）

> ⚠️ "简单工厂"虽然不是 GoF 模式，但它是迈向工厂方法的**必经一步**。理解它能让你更容易看懂工厂方法。

---

## 💡 进化二：工厂方法（Factory Method · GoF 正解）

把"造哪种披萨"这件事**交给子类决定**。父类只管流程。

```java
abstract class PizzaStore {
    // 🎯 这是个"模板方法"，流程固定
    public final Pizza orderPizza(String type) {
        Pizza pizza = createPizza(type);   // 👈 造什么？父类不知道，问子类
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        return pizza;
    }

    // 👇 抽象工厂方法：具体造什么，子类说了算
    protected abstract Pizza createPizza(String type);
}

// 纽约店：做纽约风
class NewYorkPizzaStore extends PizzaStore {
    protected Pizza createPizza(String type) {
        if (type.equals("cheese")) return new NYCheesePizza();      // 薄饼
        // ...
        throw new IllegalArgumentException();
    }
}

// 芝加哥店：做芝加哥风
class ChicagoPizzaStore extends PizzaStore {
    protected Pizza createPizza(String type) {
        if (type.equals("cheese")) return new ChicagoCheesePizza();  // 深盘
        // ...
        throw new IllegalArgumentException();
    }
}
```

### 精髓所在

```
父类 PizzaStore
  ├── orderPizza() ← 流程固定（prepare → bake → cut → box）
  └── createPizza() ← 抽象方法，"这里填什么？"交给子类填

子类 NewYorkPizzaStore
  └── createPizza() ← 填：造纽约风披萨

子类 ChicagoPizzaStore
  └── createPizza() ← 填：造芝加哥风披萨
```

**父类定义"骨架"，子类填充"细节"**。这就是工厂方法的核心思想。

打开 [code/FactoryMethodDemo.java](code/FactoryMethodDemo.java)，跑一下：

```bash
java FactoryMethodDemo.java
```

你会看到两家店用**完全相同的流程**，做出**完全不同的披萨**。

---

## 📐 UML 结构图

```
         ┌─────────────────────────┐
         │   PizzaStore（抽象）    │
         ├─────────────────────────┤
         │ + orderPizza(type)      │ ← 模板方法：流程固定
         │ + createPizza(type):    │ ← 抽象工厂方法：子类实现
         │   abstract              │
         └───────────▲─────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────────────────┐    ┌───────────────────┐
│ NewYorkPizzaStore │    │ ChicagoPizzaStore │
├───────────────────┤    ├───────────────────┤
│ + createPizza()   │    │ + createPizza()   │
│   → new NYCheese..│    │   → new ChicagoCh.│
└───────────────────┘    └───────────────────┘
```

---

## 🧠 工厂方法解决了什么

| 问题 | 土办法 | 简单工厂 | 工厂方法 |
|------|--------|---------|---------|
| 业务代码里 `new` 满天飞 | ❌ 是 | ✅ 解决 | ✅ 解决 |
| 加新产品不改已有代码 | ❌ | ❌ | ✅ 解决（加子类即可） |
| 不同变体用同一流程 | ❌ | ❌ | ✅ 解决（纽约风/芝加哥风） |
| 符合开闭原则 | ❌ | ❌ | ✅ |

---

## 🎯 简单工厂 vs 工厂方法 —— 一句话记忆

- **简单工厂**：一个工厂类 + 一堆 if-else / switch → **新增产品要改工厂代码**
- **工厂方法**：抽象工厂方法 + 子类实现 → **新增产品线 = 新增子类**（不改已有代码）

---

## 🌍 真实应用

| 在哪里 | 谁是工厂方法 |
|--------|-------------|
| JDK | `Calendar.getInstance()` —— 根据 Locale 返回不同日历（GregorianCalendar / BuddhistCalendar） |
| JDK | `Collection.iterator()` —— 每个集合类返回自己的 Iterator 实现 |
| JDK | `NumberFormat.getCurrencyInstance(locale)` |
| Spring | `BeanFactory.getBean()` |
| Mybatis | `SqlSessionFactory.openSession()` |
| JDBC | `DriverManager.getConnection(url)` —— 根据 url 返回不同数据库的 Connection |

**发现规律**：只要你看到 `getXxx()` / `createXxx()` / `newInstance()` 这种静态或实例方法返回一个接口/抽象类，**九成是工厂方法**。

---

## ⚠️ 什么时候别用

### 🚫 产品种类稳定，不会扩展
只有一种 Pizza，永远不会加新的 → 直接 `new` 就行，搞抽象类是过度设计。

### 🚫 产品变体之间没有共享流程
如果纽约店的流程和芝加哥店的流程**完全不一样**（一个是切小块、一个是卷起来），工厂方法也不合适 —— 父类定义不了共同流程。

### 🚫 为了"以后可能扩展"而提前引入
**"Don't pay for flexibility you don't need"**。等真的要扩展再重构，比一开始就抽象靠谱。

---

## 🧭 和其他模式的关系（以后会学到）

| 模式 | 差异 |
|------|------|
| **简单工厂**（非 GoF） | 一个工厂 + switch，没有继承 |
| **工厂方法**（本课） | 抽象工厂方法 + 子类实现，**每个工厂造一种产品族里的一类** |
| **抽象工厂**（下一课） | 一个工厂造一**整族**相关产品（披萨 + 饮料 + 甜品） |
| **建造者**（第 3 课） | 关注"怎么造"（一步步组装），不关注"造哪种" |

---

## 📝 思考题 & 小练习

### 思考题

1. `SimplePizzaFactory` 为什么不是 GoF 的"工厂方法"？（答：没有抽象 + 继承）
2. 为什么工厂方法模式里，**父类的 `orderPizza()` 是 `final` 的**？（答：防止子类改流程，只允许子类改"造什么"）
3. 工厂方法和**模板方法**模式很像（都是"父类定流程，子类填细节"），区别在哪？（答：模板方法管"流程的一步",工厂方法管"创建对象"）

### 小练习

**练习 1：扩展 `FactoryMethodDemo.java`**
在已有代码基础上，加一个"**加州披萨店 CaliforniaPizzaStore**"，支持：
- 加州风芝士披萨：用牧场奶酪 + 羽衣甘蓝配料

提示：只需要加一个 `CaliforniaPizzaStore` 子类 + 一个 `CaliforniaCheesePizza` 类，**不需要改 `PizzaStore` 父类**。这就是工厂方法"对扩展开放、对修改关闭"的体现。

**练习 2：重构一段现实代码**
假设有下面这段老代码：

```java
public Notification sendNotification(String type, String msg) {
    if (type.equals("email"))      return new EmailNotification(msg);
    else if (type.equals("sms"))    return new SmsNotification(msg);
    else if (type.equals("push"))   return new PushNotification(msg);
    throw new IllegalArgumentException();
}
```

用**工厂方法**重构。思考：
- 应该定义什么抽象类？
- 应该有哪些子类？
- 这样做值不值得？（如果 Notification 种类未来不会增加，不值）

---

## 🏁 学完后

- **"都懂了，下一课"** → [第 3 课 · 建造者 Builder](../03-builder/)
- **"XXX 不懂"** → 具体哪里，我单独拆解
- **"我做了练习题"** → 贴代码给我 review

**工厂方法真正的威力在"加新产品线不改已有代码"这件事上**。多写几次，体会到那种"哦，这样加功能好爽"的感觉，就说明你真懂了。🙌
