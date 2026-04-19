# 第 22 课 · 访问者 Visitor ★★★

> 类型：行为型 | 难度：★★★ | GoF 经典 | **最难理解的 GoF 模式之一**

## 🎯 本课目标

- [x] 理解"**不改元素类，动态加新操作**"
- [x] 看懂**双重分派**（访问者的核心机制）
- [x] 识别编译器 AST / Files.walk 等真实场景

---

## 🎬 场景：税务员查不同业态

税务局要查税，辖区里有几种业态：

- 🍜 **餐馆** —— 按餐饮业税率
- 🛒 **超市** —— 按零售业税率
- 💻 **IT 公司** —— 按科技企业优惠税率

**痛点**：不同业态**算法完全不同**，但"被查"这件事是**统一的**。

---

## 🤔 土办法：Visitor 里用 `instanceof` 判断

```java
class TaxInspector {
    public void inspect(Business b) {
        if (b instanceof Restaurant) {
            Restaurant r = (Restaurant) b;
            System.out.println("餐饮税：" + r.getRevenue() * 0.06);
        } else if (b instanceof Supermarket) {
            Supermarket s = (Supermarket) b;
            System.out.println("零售税：" + s.getRevenue() * 0.05);
        } else if (b instanceof ITCompany) {
            // ...
        }
        // 加新业态要改这里
    }
}
```

跑 [code/BadVisitor.java](code/BadVisitor.java)。

**痛点**：
- 🙁 一堆 `instanceof` 判断
- 🙁 加新业态 → 要改 `inspect` 方法
- 🙁 类型判断和业务逻辑混一起

---

## 💡 访问者模式登场

**核心思想**：**两个接口 + 双重分派**。
- 元素类（业态）提供 `accept(Visitor)` 方法
- 访问者（税务员）提供对每种元素的 `visit(Element)` 方法
- 两者配合，**类型信息两次分派**

```java
// 访问者接口（每种业态一个方法）
interface Visitor {
    void visit(Restaurant r);
    void visit(Supermarket s);
    void visit(ITCompany c);
}

// 元素接口
interface Business {
    void accept(Visitor visitor);
}

// 具体元素：餐馆
class Restaurant implements Business {
    private double revenue;

    public void accept(Visitor visitor) {
        visitor.visit(this);      // 🎯 双重分派的核心
    }

    public double getRevenue() { return revenue; }
}

// 具体访问者：税务员
class TaxInspector implements Visitor {
    public void visit(Restaurant r) {
        System.out.println("餐饮税: " + r.getRevenue() * 0.06);
    }
    public void visit(Supermarket s) {
        System.out.println("零售税: " + s.getRevenue() * 0.05);
    }
    public void visit(ITCompany c) {
        System.out.println("科技税: " + c.getRevenue() * 0.025);
    }
}

// 另一种访问者：卫生检查员（复用同样的元素结构）
class HealthInspector implements Visitor {
    public void visit(Restaurant r)    { /* 检查厨房卫生 */ }
    public void visit(Supermarket s)   { /* 检查食品保质期 */ }
    public void visit(ITCompany c)     { /* 不适用 */ }
}
```

### 使用

```java
List<Business> businesses = List.of(
    new Restaurant(100_000),
    new Supermarket(500_000),
    new ITCompany(2_000_000)
);

Visitor tax = new TaxInspector();
for (Business b : businesses) {
    b.accept(tax);       // 每个元素接受访问者
}

Visitor health = new HealthInspector();
for (Business b : businesses) {
    b.accept(health);    // 同一个结构，不同的访问者
}
```

跑 [code/VisitorDemo.java](code/VisitorDemo.java)。

---

## 🧠 核心难点：双重分派（Double Dispatch）

这是访问者模式最抽象的一点，**一旦搞懂就全通**。

### Java 是"**单分派**"语言

```java
Business b = new Restaurant();
b.someMethod();
//  ↑
// Java 根据 b 的【实际类型】（Restaurant）调方法 → 单分派
```

### `accept` + `visit` = **双重分派**

```java
b.accept(visitor);
// 第一次分派：Java 根据 b 的类型调用对应的 accept
//             如果 b 是 Restaurant → Restaurant.accept

// 在 Restaurant.accept 里：
public void accept(Visitor v) {
    v.visit(this);       // this 类型已确定（Restaurant）
    // 第二次分派：Java 根据 v 的类型调用对应的 visit
    //           如果 v 是 TaxInspector → TaxInspector.visit(Restaurant)
    //           如果 v 是 HealthInspector → HealthInspector.visit(Restaurant)
}
```

**两步分派后**：自动路由到 **"具体访问者 × 具体元素"** 的正确方法。

**画图理解**：

```
b.accept(visitor)
     ↓
  Restaurant.accept(v)        ← 第 1 次分派（按元素类型）
     ↓
  v.visit(this)               ← this 已是 Restaurant
     ↓
  TaxInspector.visit(Restaurant)  ← 第 2 次分派（按访问者类型）
```

**结果**：编译器无需 `instanceof`，直接调到正确的方法。

---

## 📐 UML 结构

```
┌────────────────┐         ┌─────────────────────┐
│   Visitor       │         │    Business         │
│   (接口)        │         │    (接口)           │
├────────────────┤         ├─────────────────────┤
│ +visit(Res.)    │         │ +accept(Visitor)    │
│ +visit(Super.)  │         └──────────▲──────────┘
│ +visit(IT.)     │                    │
└────────▲───────┘                     │
         │                  ┌──────────┼──────────┐
         │              ┌───┴─────┐ ┌──┴────────┐ ┌──┴────────┐
         │              │Restaurant│ │Supermarket│ │ITCompany  │
         │              └─────────┘ └───────────┘ └───────────┘
    ┌────┴──────────────┬────────────────────┐
    │                   │                    │
TaxInspector    HealthInspector      AuditVisitor
```

**访问者横切所有元素**。加新访问者 → 加一个类。加新元素 → 所有访问者都要加 visit 方法。

---

## 📊 访问者的权衡

| 变化 | 容易程度 |
|------|---------|
| **加新访问者**（比如审计）| ✅ **很容易**：加一个类 |
| **加新元素**（比如新业态）| ❌ **很难**：所有访问者都要加 visit 方法 |

**访问者模式适合"元素类型稳定 + 操作经常变"的场景**。反过来（元素经常变 + 操作稳定）不适合。

---

## 🌍 真实应用

| 场景 | 访问者体现 |
|------|----------|
| **编译器** | AST 遍历 —— 每种节点（Expr / Stmt / Decl）被各种 Visitor 访问（类型检查 / 代码生成 / 优化）|
| **Java NIO** | `Files.walkFileTree(path, FileVisitor)` —— 每个文件被访问 |
| **Spring** | `BeanPostProcessor` —— 访问所有 bean |
| **IDE 重构** | 访问代码 AST 做重构 |
| **JSON 解析** | Jackson 的 Tree Traversal |
| **ANTLR** | 语法树的 Visitor |

**编译器是访问者模式最经典的应用场景**。

---

## 🎁 现代替代：Java 21 **Pattern Matching for Switch**

访问者模式代码啰嗦。**Java 21** 的模式匹配可以部分替代：

```java
// Java 21+：sealed + pattern matching
sealed interface Business permits Restaurant, Supermarket, ITCompany {}
record Restaurant(double revenue) implements Business {}
record Supermarket(double revenue) implements Business {}
record ITCompany(double revenue) implements Business {}

double tax(Business b) {
    return switch (b) {
        case Restaurant r  -> r.revenue() * 0.06;
        case Supermarket s -> s.revenue() * 0.05;
        case ITCompany c   -> c.revenue() * 0.025;
        // 编译器检查穷尽性，加新 Business 不加 case 会报错
    };
}
```

**优点**：
- 代码少得多
- 不用写 `accept` 方法
- 编译器强制处理所有类型（sealed + 穷尽性检查）

**传统 Visitor 的价值**：在不支持模式匹配的老版本 Java / 场景复杂时仍然适用。

---

## ⚠️ 什么时候别用

### 🚫 元素种类会频繁增加
每加一种，所有访问者都要加 visit → 代码灾难。

### 🚫 只需一个操作
直接写方法就行。

### 🚫 元素结构简单
`if-else` 能解决的不用搞这么复杂。

### 🚫 Java 21+ 场景
优先考虑 sealed + pattern matching（更简洁）。

---

## 📝 思考题 & 小练习

### 思考题

1. **为什么必须双重分派**？不能只用 `visitor.visit(business)` 吗？（答：Java 的方法重载是**编译期**按**静态类型**选，动态时 b 的静态类型是 `Business`，不是 `Restaurant`。所以会调错）
2. 访问者和**策略**有什么区别？（答：策略只关心"一种算法"，访问者要处理"多种元素的多种算法"）
3. Java 21 的 pattern matching 完全取代访问者了吗？（答：没有。老项目、运行时动态加元素、需要状态积累的访问者场景仍然用）

### 小练习

**加一个 AuditVisitor**

基于 `VisitorDemo.java`：
- 加一个审计员 `AuditVisitor`
- `visit(Restaurant)`：检查流水
- `visit(Supermarket)`：检查库存
- `visit(ITCompany)`：检查财报

观察：**只改了访问者，不需要改元素类** —— 这就是访问者的价值。

---

## 🏁 学完后

- **"懂了，下一课"** → 第 23 课 · 解释器 Interpreter（**GoF 23 模式最后一个**）
- **"Java 21 pattern matching 想深入"** → 可以沉淀
- **"先 commit"** → 我帮你

**访问者是 GoF 23 模式里最抽象的一个**。看懂双重分派就算过关。现代 Java 有了 Pattern Matching 后，很多场景可以用更简洁的方式 🎯
