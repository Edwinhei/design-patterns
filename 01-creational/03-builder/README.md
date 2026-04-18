# 第 3 课 · 建造者 Builder ★★

> 类型：创建型 | 难度：★★ | GoF 经典（但现代用法有演化）

## 🎯 本课目标

- [x] 看懂"超长构造器"和"JavaBean setter"这两种反模式的痛
- [x] 能独立写出一个**现代版 Builder**（静态内部类 + 链式调用）
- [x] 知道 GoF 原版 Builder 和现代版的区别（以及为什么现代版赢了）

---

## 🎬 场景：Subway 定制三明治

你去 Subway 点单：

```
店员：面包选什么？
你：  全麦
店员：加什么肉？
你：  烤鸡
店员：加奶酪吗？
你：  切达
店员：加什么蔬菜？
你：  生菜和番茄
店员：要什么酱？
你：  蛋黄酱
店员：要加热吗？
你：  要
```

最后得到一个**定制的三明治**。

**注意特点**：
- ✅ **有些是必选**（面包）
- ✅ **有些是可选**（奶酪、加热）
- ✅ **组合变化多**（能点出几百种不同组合）
- ✅ **点单过程中三明治还没做好**，最后"好的，这就为您做"

这个过程用代码怎么表达？

---

## 🤔 土办法 1：超长构造器

```java
SandwichV1 s1 = new SandwichV1("全麦", "烤鸡", "切达", "生菜", null, null, "蛋黄酱", true);
//                                                         ↑     ↑
//                                        这两个 null 代表啥？可能是 v2, v3 蔬菜槽位
```

**痛点**：
- 🙁 参数顺序记不住 → 传错不报错（类型相同的 String 换位置编译器发现不了）
- 🙁 不用的参数也要传 null
- 🙁 要支持"只有面包"、"面包+肉"、"面包+肉+奶酪"…… → 写一堆**重载构造器**
- 🙁 这就是著名的 **"Telescoping Constructor（可伸缩构造器）反模式"**

跑一下 [code/BadSandwich.java](code/BadSandwich.java) 感受。

---

## 🤔 土办法 2：JavaBean setter

```java
SandwichV2 s = new SandwichV2();
s.setBread("全麦");
s.setMeat("烤鸡");
// ... 还没设完，但这时候 s 已经是完整对象了
useIt(s);   // 🚨 有人可能拿到"半成品"三明治
s.setCheese("切达");
```

**痛点**：
- 🙁 **对象在构建过程中处于不一致状态**，其他代码可能拿去用
- 🙁 **对象可变**（谁都能随时修改），不适合多线程共享
- 🙁 没法声明"面包必选"—— setter 模式所有字段都是可选

---

## 💡 建造者模式（现代版 · Effective Java 推荐）

**核心思想**：
- 用一个**内部 Builder 类**收集各个配置
- 最后调 `build()` **一次性创建**完整对象
- 对象本身**不可变**（所有字段 `final`），构建过程可变

```java
SubwaySandwich s = SubwaySandwich.builder("全麦面包")   // 必选放在 builder 入口
        .meat("烤鸡")
        .cheese("切达")
        .addVegetable("生菜")
        .addVegetable("番茄")
        .sauce("蛋黄酱")
        .toasted(true)
        .build();                                        // 一次性创建
```

### 📋 三大优势

| 问题 | 建造者怎么解决 |
|------|--------------|
| 参数顺序 | 用**方法名**代替位置参数，不可能传错 |
| 可选参数 | 不需要的方法直接不调 |
| 对象一致性 | 构建过程中没有完整对象，`build()` 后才有 |
| 对象不可变 | 字段全 `final`，安全共享 |
| 必选字段 | 放 builder 构造器参数里，不传编译错 |

---

## 🛠 代码核心：静态内部类 Builder

```java
class SubwaySandwich {
    private final String bread;       // 🔒 全部 final
    private final String meat;
    private final String sauce;
    // ...

    private SubwaySandwich(Builder b) {   // 🔒 构造器私有，只能 Builder 调
        this.bread = b.bread;
        this.meat  = b.meat;
        // ...
    }

    public static Builder builder(String bread) {
        return new Builder(bread);            // 🚪 唯一入口
    }

    public static class Builder {             // 🏗 静态内部类
        private final String bread;           // 必选
        private String meat;                  // 可选
        // ...

        private Builder(String bread) {
            this.bread = Objects.requireNonNull(bread, "面包必选");
        }

        public Builder meat(String meat) {    // 每个方法返回 this → 链式调用
            this.meat = meat;
            return this;
        }

        public SubwaySandwich build() {
            // 可以在这里做整体校验
            return new SubwaySandwich(this);
        }
    }
}
```

**几个关键设计**：

1. `SubwaySandwich` 构造器是 `private` —— **只能通过 Builder 创建**
2. 每个配置方法 `return this;` —— 支持**链式调用**
3. `build()` 里可以做**全局校验**（比如"加热的必须配面包加热选项"）
4. `Builder` 是 `static` 内部类 —— 不依赖外部类实例，可独立 new

跑一下 [code/BuilderDemo.java](code/BuilderDemo.java) 看效果。

---

## 📐 现代版 Builder 的 UML

```
┌─────────────────────────┐
│    SubwaySandwich       │   ← 目标类（不可变）
├─────────────────────────┤
│ -bread: String (final)  │
│ -meat : String (final)  │
│ -...                    │
├─────────────────────────┤
│ +builder(bread): Builder│
│ -SubwaySandwich(b)      │
└───────────▲─────────────┘
            │ 1
            │ 包含
            │ 1
┌─────────────────────────┐
│    Builder              │   ← 静态内部类
├─────────────────────────┤
│ -bread, -meat, ...      │   ← 配置字段（非 final）
├─────────────────────────┤
│ +meat(s): Builder       │   ← 链式配置方法
│ +cheese(s): Builder     │
│ +build(): SubwaySandwich│   ← 最终产出
└─────────────────────────┘
```

---

## 🎭 顺便：GoF 原版 Builder（Director + Builder）

GoF 1994 年的原书是这样设计的：

```
┌────────────┐         ┌───────────────┐
│  Director  │ 使用→   │ Builder（抽象）│
│ (指挥者)   │         │                │
└────────────┘         └───────▲───────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
    ┌──────────────────┐               ┌──────────────────┐
    │ ConcreteBuilderA │               │ ConcreteBuilderB │
    └──────────────────┘               └──────────────────┘
         造普通三明治                        造豪华三明治
```

**特点**：
- 有个 **Director**（指挥者）掌握"**构建流程**"
- Builder 是抽象接口，不同的 ConcreteBuilder 造不同产品
- Director 按流程调 Builder，最后拿结果

**适用场景**：产品有**多种变体**，但**构建流程相同**（比如造汽车：加底盘→装发动机→装车门，流程一样，零件不同）。

### 现代 Java 为什么不用 GoF 原版

- 太重：一个小 POJO 也要 Director + Builder 接口 + 具体 Builder，代码爆炸
- 多数场景只需要"灵活可选参数"，不需要"多种变体"
- Effective Java 推荐的简化版 Builder 够用了

**结论**：**现代 Java 99% 的 Builder 都是简化版**。知道 GoF 原版的存在就够了，不用死记。

---

## 🌍 真实应用

| 在哪里 | 谁是 Builder |
|--------|-------------|
| JDK | `StringBuilder` —— 高频使用的字符串拼接 |
| JDK | `Stream.Builder` —— 构建 Stream |
| JDK | `HttpRequest.newBuilder()` —— Java 11+ HTTP 客户端 |
| JDK | `Calendar.Builder` |
| Lombok | `@Builder` 注解 —— 自动生成 Builder 代码 |
| OkHttp | `Request.Builder()` |
| Protobuf | 所有 message 都用 Builder 构造 |
| Spring | `MockMvcBuilders`、`UriComponentsBuilder` |

**你几乎每天都在用 Builder，只是没意识到**。

---

## ⚠️ 什么时候别用

### 🚫 字段很少（2-3 个）
直接用构造器 `new Sandwich("全麦", "烤鸡")` 完全够用。Builder 反而增加复杂度。

### 🚫 所有字段都必填
没有可选项 → 超长构造器也可接受。Builder 主要价值在**处理可选组合**。

### 🚫 对象需要频繁创建+短生命周期
Builder 多一次 Builder 对象的分配 → 高频场景下性能略差（但通常可忽略）。

---

## 🎁 Lombok `@Builder` —— 现代开发的加速器

生产项目里其实不会手写上面那堆 Builder 代码，一个注解搞定：

```java
@Builder
@Value   // 让类不可变
public class SubwaySandwich {
    String bread;
    String meat;
    String cheese;
    // ...
}

// 用起来：
SubwaySandwich s = SubwaySandwich.builder()
        .bread("全麦")
        .meat("烤鸡")
        .build();
```

Lombok 编译期生成 Builder 内部类、build 方法、字段的 getter 等。**理解原理之后，用工具更轻松**。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么建造者模式的**目标类字段全 `final`**？不 final 行不行？
2. `Builder.build()` 里为什么可以做**跨字段校验**（比如"toasted 为 true 时面包必须支持加热"）？构造器里不能做吗？
3. 和工厂方法相比，Builder 关心"造什么"还是"怎么造"？

### 小练习

**练习 1：扩展 Sandwich**
给 `SubwaySandwich` 加两个可选字段：
- `size`（6 寸 / 12 寸）
- `extraSauce`（boolean，是否双倍酱）
然后写 3 组不同组合的下单代码。

**练习 2：给 HttpRequest 写 Builder**
模拟一个 HTTP 请求对象：
```
必选：url
可选：method（默认 GET）、headers（map）、body（字符串）、timeout（默认 30 秒）
```
写出它的 Builder，并展示怎么用。

---

## 🏁 学完后

- **"都懂了，下一课"** → [第 4 课 · 抽象工厂](../04-abstract-factory/)
- **"这里不懂"** → 告诉我具体卡哪
- **"我做了练习"** → 贴代码来 review

**Builder 是最高频的"日常使用型模式"**。今天学完，你每次看到 `.builder().xxx().xxx().build()` 的代码都会会心一笑 🥪
