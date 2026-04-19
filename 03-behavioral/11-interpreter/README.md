# 第 23 课 · 解释器 Interpreter ★★★

> 类型：行为型 | 难度：★★★ | GoF 经典 | **🎊 GoF 23 模式最后一课**

## 🎯 本课目标

- [x] 理解"**把一种简单语言的语法变成对象结构 + 递归解释**"
- [x] 看清**终结符 / 非终结符**
- [x] 知道工作中**什么时候用它 / 什么时候用 ANTLR 等专业工具**

---

## 🎬 场景：数学表达式求值

你要写一个计算器，支持表达式：

```
3 + 5 - 2          // = 6
(10 - 3) + 4       // = 11
a + b              // 含变量
```

**问题**：怎么把字符串**解析并求值**？

---

## 🤔 土办法：硬编码字符串解析

```java
int evaluate(String expr) {
    // 去空格、分割、判断操作符、递归…… 写不过来
    // 大量 if-else、indexOf、switch
    // 支持更多操作符？再加一堆分支
    // 支持括号？代码爆炸
    // 支持变量？继续爆炸
}
```

**痛点**：**文法稍微复杂，代码就失控**。

---

## 💡 解释器模式登场

**核心思想**：**把"文法规则"表示为**类结构**，用递归求值**。

### 文法定义（简化版）

```
Expression  ::=  Number | Variable | BinaryExpr
BinaryExpr  ::=  Expression Operator Expression
Operator    ::=  + | - | * | /
Number      ::=  整数
Variable    ::=  变量名
```

### 对应的类结构

```java
// 表达式接口（所有语法节点都实现它）
interface Expression {
    int interpret(Context ctx);
}

// 终结符（叶子节点）：数字
class NumberExpr implements Expression {
    private final int value;
    public NumberExpr(int v) { this.value = v; }

    public int interpret(Context ctx) {
        return value;     // 数字直接返回
    }
}

// 终结符：变量
class VariableExpr implements Expression {
    private final String name;

    public int interpret(Context ctx) {
        return ctx.get(name);    // 从上下文查值
    }
}

// 非终结符（组合节点）：加法
class AddExpr implements Expression {
    private final Expression left, right;

    public int interpret(Context ctx) {
        return left.interpret(ctx) + right.interpret(ctx);   // 递归求值
    }
}

// 非终结符：减法、乘法、除法类似
```

### 使用

```java
// 构造表达式: (3 + 5) - 2
Expression expr = new SubExpr(
    new AddExpr(
        new NumberExpr(3),
        new NumberExpr(5)
    ),
    new NumberExpr(2)
);

Context ctx = new Context();
System.out.println(expr.interpret(ctx));   // 6
```

**递归求值**：

```
SubExpr
 ├── AddExpr
 │    ├── NumberExpr(3)
 │    └── NumberExpr(5)
 └── NumberExpr(2)

求值过程：
  左子树 3 + 5 = 8
  右子树 2
  8 - 2 = 6
```

### 带变量 + Context

```java
// 表达式: a + b
Expression expr = new AddExpr(
    new VariableExpr("a"),
    new VariableExpr("b")
);

Context ctx = new Context();
ctx.set("a", 10);
ctx.set("b", 20);

System.out.println(expr.interpret(ctx));   // 30
```

跑 [code/InterpreterDemo.java](code/InterpreterDemo.java)。

---

## 🧩 两种节点

| 类型 | 本例 | 对应文法 |
|------|------|---------|
| **终结符**（Terminal）| `NumberExpr` / `VariableExpr` | 不能再展开的叶子 |
| **非终结符**（NonTerminal）| `AddExpr` / `SubExpr` | 可以包含其他节点 |

**整棵表达式树 = 一个抽象语法树（AST）**。

---

## 📐 UML 结构

```
┌──────────────────┐
│   Expression     │ ← 通用接口
│   (抽象)         │
├──────────────────┤
│ +interpret(ctx)  │
└────────▲─────────┘
         │
    ┌────┴────────┬─────────────┐
    │             │             │
NumberExpr  VariableExpr   AddExpr (持有两个 Expression)
（终结符）   （终结符）     （非终结符）
                              │
                         ├── left: Expression
                         └── right: Expression
                              ↑ 递归
```

**非终结符持有其他 Expression** → 形成树结构。

---

## 🌍 真实应用

| 场景 | 解释器体现 |
|------|----------|
| **正则表达式** | 每个字符 / 量词是 Expression，`Pattern` 编译成树 |
| **SQL 解析** | SQL 语句解析为 AST，数据库解释执行 |
| **Spring EL** | `#{user.name}` 解析为表达式树 |
| **JavaScript 引擎** | V8 把 JS 代码解析成 AST 再执行 |
| **模板引擎** | FreeMarker / Thymeleaf 模板解析 |
| **脚本语言** | Lua / Python 的解释器本身 |
| **计算器 / 公式** | Excel 公式 / 金融计算引擎 |
| **配置 DSL** | Gradle / Jenkins 的 DSL |

---

## 🎁 现代替代：**ANTLR** / **JavaCC** 等解析生成器

**真实项目复杂文法不会手写解释器**：
- 手写 Parser 代码量爆炸
- 优先级、结合性处理复杂
- 错误处理很难

**用工具**：
- **ANTLR** —— 目前最流行的解析生成器
- **JavaCC** —— Java 传统选择
- **JFlex + CUP** —— 经典词法+语法分析
- **ParboiledJ** —— PEG 风格
- **Parboiled2 / FastParse**（Scala 生态）

**工作流**：
1. 写文法文件（`.g4` 语法）
2. 工具生成 Parser / Lexer 代码
3. 你写 Visitor 遍历 AST

**工作里基本不会手写 Interpreter**，但**理解原理**能帮你看懂编译器/解析器。

---

## ⚠️ 什么时候别用

### 🚫 文法复杂（> 10 个规则）
手写代码爆炸。用 ANTLR。

### 🚫 性能敏感
解释器模式每次 interpret 都要递归遍历树，比直接编译执行慢。
高性能场景用 JIT 或编译到原生代码。

### 🚫 标准语言（JSON / XML / SQL）
用**现成的库**，不要自己写。

### 🚫 简单字符串匹配
用 `String.replace` 或正则就行。

---

## 📝 思考题 & 小练习

### 思考题

1. 解释器模式和**组合模式**有什么关系？（答：表达式树就是 Composite 结构，解释器用 Composite 来表示文法）
2. 为什么解释器对复杂文法不友好？（答：每个规则一个类，100 条规则就 100 个类，难维护）
3. JVM 执行 .class 字节码是不是解释器？（答：是！JVM 本身就是一个字节码解释器 + JIT 编译器）

### 小练习

**扩展 InterpreterDemo.java**
- 加一个 `MulExpr`（乘法）
- 加一个 `DivExpr`（除法）
- 构造 `(a + b) * 2 / 3` 的表达式并求值

---

## 🎊 GoF 23 模式 · 全部完成！

```
【创建型 · 5/5】✅
  单例 / 工厂方法 / 建造者 / 抽象工厂 / 原型

【结构型 · 7/7】✅
  适配器 / 外观 / 代理 / 组合 / 装饰器 / 桥接 / 享元

【行为型 · 11/11】✅
  模板方法 / 策略 / 观察者 / 迭代器 / 责任链 /
  命令 / 状态 / 备忘录 / 中介者 / 访问者 / 解释器 🎊
```

**你已经掌握了 1994 年 GoF 总结的所有 23 种经典设计模式**。

**这是面向对象设计的完整基础**。接下来的阶段（企业应用 / DDD / 云原生 / 架构）都会建立在这套基础之上。

---

## 🏁 下一步

- **"GoF 收官，先庆祝 commit"** → 一个标志性的 commit + push
- **"继续下一阶段"** → 第 4 阶段 · 并发模式（线程池 / Future / 生产者消费者等 6 种）
- **"休息消化一下"** → GoF 23 种学完，值得回顾 + 休息

---

## 💡 一个回顾建议

GoF 23 种模式学完后，**最值得的不是记住每种模式**，而是：

1. **场景 → 模式** 的反射：看到问题能想到"这里可以用 XX 模式"
2. **识别** 别人代码里的模式：读源码时"哦这是装饰器"
3. **警惕过度设计**：**不是所有问题都需要模式**，有时候 `if-else` 才是最合适的

**设计模式是工具箱，不是教条**。🛠
