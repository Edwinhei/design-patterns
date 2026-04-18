# 第 5 课 · 原型 Prototype ★

> 类型：创建型 | 难度：★ | GoF 经典（但 Java 实现有坑）

## 🎯 本课目标

- [x] 理解 "复制已有对象" 比 "重新 new" 在什么场景下更好
- [x] 看清 Java `Cloneable` 接口的真相和陷阱
- [x] **浅拷贝 vs 深拷贝** 的坑一次搞懂
- [x] 掌握现代推荐方案：**copy constructor**

---

## 🎬 场景：简历模板

你写了一份**写得很好的简历**：

```
姓名: 张三
邮箱: zhang3@example.com
技能: [Java, Spring, MySQL, Docker]
项目: [电商系统, 实时风控, 大数据平台]
```

现在你要**投递多家公司**：
- 投给阿里 → 强调 **大数据平台** 和 Docker
- 投给字节 → 强调 **实时风控** 和 Spring
- 投给 Google → 把项目用英文描述

每份简历**大部分相同**，只是**微调**。

### 你会怎么做？

**选项 A：每份从头 new + 填字段**
```java
Resume ali = new Resume();
ali.setName("张三");
ali.setEmail("...");
ali.setSkills(...);   // 把技能列表抄一遍
ali.setProjects(...); // 把项目抄一遍
// 再微调...
```
🙁 重复代码巨多，一旦漏填字段就露馅。

**选项 B：复印原版 + 微调**
```java
Resume ali = original.clone();   // 复印一份
ali.reorderSkills("大数据平台"); // 微调
```
✅ 干净得多。

**B 就是原型模式** —— "**以已有对象为原型，通过克隆创建新对象**"。

---

## 🤔 第一步：跑"土办法"感受痛点

跑一下 [code/BadPrototype.java](code/BadPrototype.java)：

```java
public Resume makeAliResume(Resume template) {
    Resume r = new Resume();
    r.setName(template.getName());
    r.setEmail(template.getEmail());
    r.setSkills(new ArrayList<>(template.getSkills()));       // 一个个字段抄
    r.setProjects(new ArrayList<>(template.getProjects()));   // 容易漏
    // ...
    return r;
}
```

**痛点**：
- 🙁 每加一个字段，所有复制代码都要同步改（违反 DRY）
- 🙁 漏抄一个字段就是 bug（且不报错）
- 🙁 业务代码里充斥着"复制字段"的噪音

---

## 💡 原型模式登场

**核心思想**：让类自己提供**克隆方法**，调用方只需一行就得到副本。

```java
Resume template = ...;       // 写好的模板
Resume ali = template.clone();   // 🎯 一行搞定
ali.reorderSkills("大数据"); // 只改需要改的
```

### 本质要解决的问题

1. ✅ **复制很复杂的对象**（不想手动抄字段）
2. ✅ **以"配置好的状态"为起点**做变体
3. ✅ 运行时**动态配置+克隆**（不是编译时决定所有参数）

---

## 🎭 Java 的"克隆"演化：三种方式

Java 里实现原型模式有三种方案，我们逐一看，**重点是理解每种的坑**。

### V1 ·（反面教材）`Cloneable` 浅拷贝

这是 **GoF 原书建议的做法**，但 Java 的 `Cloneable` 有**著名的设计问题**。

```java
class ResumeV1 implements Cloneable {
    String name;
    List<String> skills;

    @Override
    public ResumeV1 clone() throws CloneNotSupportedException {
        return (ResumeV1) super.clone();   // 调 Object.clone()
    }
}
```

**🚨 致命陷阱**：`Object.clone()` 默认是**浅拷贝** —— **引用类型字段**（如 `List skills`）**不会**被复制，而是**共享引用**！

```java
ResumeV1 template = new ResumeV1();
template.skills = new ArrayList<>(List.of("Java", "Spring"));

ResumeV1 copy = template.clone();
copy.skills.add("Kubernetes");   // 🚨 修改 copy 的 skills

System.out.println(template.skills);
// 输出：[Java, Spring, Kubernetes]    ← 原版也被改了！
```

**这就是浅拷贝的坑**：两个对象共享同一个 `List`。

### V2 · `Cloneable` 深拷贝

手动复制每个引用类型字段：

```java
@Override
public ResumeV2 clone() throws CloneNotSupportedException {
    ResumeV2 cloned = (ResumeV2) super.clone();
    cloned.skills   = new ArrayList<>(this.skills);     // 手动新建 List
    cloned.projects = new ArrayList<>(this.projects);   // 每个引用字段都要
    return cloned;
}
```

**✅ 修好了**但：
- 🙁 要检查 `CloneNotSupportedException`（受检异常）
- 🙁 如果字段本身还有嵌套引用，要一路克隆下去（递归深拷贝）
- 🙁 Cloneable 接口本身是标记接口，`clone()` 不在它上面（在 Object 上），怪异
- 🙁 **new 出来时不走构造器**，final 字段就懵了

### V3 · Copy constructor（现代推荐）

**不用 Cloneable，直接写一个"拷贝构造器"**：

```java
class ResumeV3 {
    String name;
    List<String> skills;

    public ResumeV3() {}                             // 普通构造器

    public ResumeV3(ResumeV3 other) {                // 👈 拷贝构造器
        this.name   = other.name;
        this.skills = new ArrayList<>(other.skills); // 深拷贝集合
    }
}

// 使用
ResumeV3 copy = new ResumeV3(template);   // 一行搞定
```

**✅ 优点**：
- 没有 `Cloneable` 那些诡异的坑
- 走正常构造器 → **final 字段也能处理**
- 代码一目了然
- 可以配合 Builder 模式使用

**《Effective Java》推荐**：**优先使用 copy constructor 而不是 Cloneable**。

跑一下 [code/PrototypeDemo.java](code/PrototypeDemo.java)，三种方式都在里面，**看输出感受坑的差异**。

---

## 📐 浅拷贝 vs 深拷贝 —— 图解

### 浅拷贝（Shallow Copy）

```
【原版】                    【克隆】
+-------------+            +-------------+
| name "张三" |            | name "张三" |
| skills -----+---┐    ┌---+--- skills   |
+-------------+   │    │   +-------------+
                  ↓    ↓
              +-----------+
              | List 对象  |    ← 两个对象共享同一个 List！
              | [Java]    |
              | [Spring]  |
              +-----------+
```

**一方改 List，另一方也变**。

### 深拷贝（Deep Copy）

```
【原版】                    【克隆】
+-------------+            +-------------+
| name "张三" |            | name "张三" |
| skills -----+---┐        | skills -----+---┐
+-------------+   │        +-------------+   │
                  ↓                          ↓
              +---------+                +---------+
              | List 1  |                | List 2  |  ← 独立的两个 List
              | [Java]  |                | [Java]  |
              | [Spring]|                | [Spring]|
              +---------+                +---------+
```

**互不影响**。

### 规则

| 字段类型 | 浅拷贝处理 | 深拷贝处理 |
|---------|----------|----------|
| 基本类型（int/boolean）| 复制值 | 复制值 |
| String（不可变） | 复制引用（安全，因为不可变） | 同浅拷贝 |
| 可变对象（List/Map/自定义类）| 复制引用（共享 🚨） | 递归克隆（独立） |

---

## 🌍 真实应用

| 在哪里 | 原型模式的身影 |
|--------|---------------|
| JDK | `ArrayList.clone()`, `HashMap.clone()` 等集合的 clone 方法 |
| Spring | Bean scope = `prototype` —— 每次从容器取都复制一份 |
| 游戏开发 | 批量生成敌人（克隆模板兵），性能远好于每个都重新构造 |
| 图形编辑器 | PPT/Photoshop 里的"复制粘贴"对象 |
| **JavaScript** | `Object.create(proto)` 基于原型创建对象（JS 天生是原型链语言） |

---

## ⚠️ 什么时候别用

### 🚫 对象简单，字段少
直接 `new` + setter 很快，不需要原型。

### 🚫 对象不可变
不可变对象复制没意义（本来就安全共享）。比如 String 不需要 clone。

### 🚫 字段中有不可克隆的资源
比如持有文件句柄、数据库连接，克隆了也没意义。

### 🚫 你不想折腾 Java Cloneable
**Bloch 在《Effective Java》里明确说过**："如果没有强烈需求，不要让新代码实现 Cloneable"。

→ 用 **copy constructor** 替代。

---

## 🎁 现代加强版：Record + `with` 方法

Java 14+ 的 Record 天然支持**函数式更新**（用一个字段新值生成新对象）：

```java
record Resume(String name, String email, List<String> skills) {}

Resume template = new Resume("张三", "zhang3@example.com", List.of("Java"));

// "修改"一个字段 → 返回新对象
Resume withNewEmail = new Resume(
    template.name(), "zhangsan@ali.com", template.skills()
);
```

配合 Builder，还能这样写：
```java
Resume ali = template.toBuilder().email("zhang@ali.com").build();
```

**不可变 + 函数式复制** 是现代 Java 的方向。

---

## 🧭 和其他创建型模式的关系

| 模式 | 核心 |
|------|------|
| 单例 | 只造一个实例 |
| 工厂方法 | 让子类决定造哪种 |
| 抽象工厂 | 造一族相关产品 |
| 建造者 | 一步步组装复杂对象 |
| **原型**（本课）| **以已有对象为起点复制** |

**所有创建型模式都在回答同一个问题**："**怎么更优雅地创建对象？**"
每个模式切一个不同维度。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么 String 做浅拷贝就够了？（答：不可变，无法被修改）
2. `Cloneable` 接口里没有任何方法，clone 方法是从哪来的？（答：`Object.clone()`，`Cloneable` 只是标记）
3. 如果 `Resume` 的 `skills` 是 `List<String>`，浅拷贝 copy 后修改 `copy.skills.add(...)` 会影响原版。那如果 `skills` 是 `List<Experience>`（Experience 是自定义对象），深拷贝后修改 `copy.skills.get(0).setTitle(...)` 会影响原版吗？（答：会，因为 List 深拷贝了但里面的 Experience 还是共享引用 —— 这就是"多层深拷贝"的复杂性）

### 小练习

**练习 1：实现 copy constructor**
基于 `PrototypeDemo.java` 里的 `ResumeV3`，加一个字段 `List<Experience>`（Experience 是自定义类，有 title + company 两个字段），正确实现深拷贝。

**练习 2：对比性能**
写一个 main，生成 100000 份简历，分别用 **new + 手动填字段** vs **copy constructor** 两种方式，看看耗时差异（提示：用 `System.nanoTime()`）。

---

## 🏁 学完后

- **"都懂了"** → 🎉 **创建型 5 个模式全部完成！** 下一步进入**第二阶段 · 结构型**
- **"Cloneable / 浅拷贝 想沉淀"** → 我加一篇 Java 笔记
- **"Record / toBuilder 想了解"** → 我开一篇
- **"想做练习题"** → 贴代码 review

**完成创建型 = 你对"对象是怎么来的"这件事有了完整认知** 🎊

下一阶段结构型讲的是"**对象之间怎么组合**"，第一个模式是**适配器**（出国用的充电转换头）🔌
