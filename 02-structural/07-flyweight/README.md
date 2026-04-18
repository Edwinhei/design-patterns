# 第 12 课 · 享元 Flyweight ★★★

> 类型：结构型 | 难度：★★★ | GoF 经典 | **结构型收官课**

## 🎯 本课目标

- [x] 理解"**共享对象节省内存**"
- [x] 掌握**内部状态 vs 外部状态**的区分
- [x] 识别工作中的享元：String 常量池 / Integer 缓存

---

## 🎬 场景：围棋棋盘

棋盘上摆满棋子。一盘激战中可能有 **200+ 个棋子**。

但实际上，**棋子只有两种** —— 黑子和白子。

### 关键观察

- ⚫ 每个黑子：**外观完全一样**（同色、同形）
- ⚪ 每个白子：同上
- 不同之处是**位置 (x, y)**

### 土办法的问题

```java
for (int i = 0; i < 200; i++) {
    stones.add(new GoStone("黑", x, y));     // 200 个对象
}
```

**浪费**：200 个对象，每个都存"黑"这个字符串和一堆共同属性。

---

## 🤔 土办法：每颗棋子一个对象

```java
class GoStone {
    String color;    // 颜色
    String texture;  // 纹理（假设几 KB）
    int x, y;        // 位置
}

// 棋盘上 200 个棋子
List<GoStone> stones = new ArrayList<>();
for (200 次) {
    stones.add(new GoStone("黑", "纹理数据...", x, y));
}
```

跑 [code/BadFlyweight.java](code/BadFlyweight.java) 看内存占用。

**痛点**：
- 🙁 200 个对象，每个都存 `color="黑"` + 重复的纹理数据
- 🙁 如果是**游戏里的森林**（10000 棵树）？或者**字符串缓冲区**（百万字符）？内存爆炸
- 🙁 绝大多数对象的**大部分字段都是一样的**

---

## 💡 享元模式登场

**核心思想**：**把"不变的共享部分"抽出来做成少量的"享元对象"**，**变化的部分**由客户端传入。

### 状态分两种

- **内部状态（Intrinsic）** → **可共享** —— 比如"黑"/"白"颜色、纹理数据
- **外部状态（Extrinsic）** → **不可共享** —— 比如位置 (x, y)

**内部状态存在享元对象里**。**外部状态作为方法参数传入**。

### 代码结构

```java
// 享元接口
interface GoStone {
    void draw(int x, int y);     // 外部状态 x, y 作为参数
}

// 具体享元（只存内部状态：颜色、纹理）
class ConcreteGoStone implements GoStone {
    private final String color;
    private final String texture;     // 假设纹理是大对象

    public ConcreteGoStone(String color) {
        this.color = color;
        this.texture = loadTexture(color);    // 重量级加载
    }

    @Override
    public void draw(int x, int y) {          // 外部状态传入
        System.out.println("在 (" + x + "," + y + ") 画 " + color);
    }
}

// 享元工厂：对象池，同颜色返回同对象
class GoStoneFactory {
    private static final Map<String, GoStone> pool = new HashMap<>();

    public static GoStone getStone(String color) {
        return pool.computeIfAbsent(color, ConcreteGoStone::new);
    }
}
```

### 使用

```java
GoStone black1 = GoStoneFactory.getStone("黑");
GoStone black2 = GoStoneFactory.getStone("黑");     // 返回同一对象！
GoStone white  = GoStoneFactory.getStone("白");

black1 == black2;   // true！全棋盘只有 2 个享元对象

// 画 200 颗棋子
for (200 次) {
    GoStoneFactory.getStone(color).draw(x, y);     // 外部状态作参数传入
}
```

**威力**：
- 棋盘上 200 颗棋子 → **内存里只有 2 个享元对象**（黑 + 白）
- 纹理大对象只加载 **2 次**（不是 200 次）
- 内存占用**大幅下降**

跑 [code/FlyweightDemo.java](code/FlyweightDemo.java) 看对比。

---

## 📐 享元的三个要素

```
┌──────────────────────────┐
│    Flyweight 接口         │
├──────────────────────────┤
│ +operation(extrinsic)     │  ← 外部状态作参数
└─────────▲────────────────┘
          │
┌─────────┴─────────────────┐
│  ConcreteFlyweight         │
├───────────────────────────┤
│ -intrinsic (共享)          │  ← 只存内部状态
└───────────────────────────┘

┌───────────────────────────┐
│  FlyweightFactory          │
├───────────────────────────┤
│ -pool: Map<Key, Flyweight> │
│ +getFlyweight(key)         │  ← 同 key 返回同对象
└───────────────────────────┘
```

三要素：
1. **享元接口**：声明"接受外部状态"的方法
2. **具体享元**：只存共享的内部状态
3. **享元工厂**：对象池，同 key 返回同对象

---

## 🌍 你每天在用的享元

### 1. String 常量池（最常用）

```java
String a = "hello";
String b = "hello";
System.out.println(a == b);     // true！同一个 String 对象
```

**JDK 在方法区维护一个 String 池，字符串字面量自动复用**。

### 2. Integer 缓存

```java
Integer a = 100;
Integer b = 100;
System.out.println(a == b);     // true

Integer c = 200;
Integer d = 200;
System.out.println(c == d);     // false！
```

**-128 ~ 127 之间的 Integer 被缓存**（Boolean.TRUE / FALSE 同理）。

### 3. 游戏引擎

森林里 10000 棵树 → **本质上只有 5 种模型**。每棵树只存"**位置 + 大小 + 引用某模型**"。

### 4. 数据库连接池

N 个业务线程共享 M 个 `Connection` 对象（M 远小于 N）。

### 5. 线程池

一个线程池共享 N 个线程，千万次任务共用这些线程。

---

## ⚠️ 什么时候别用

### 🚫 对象数量不多
只有几十个对象 → 享元优化可忽略不计。

### 🚫 没有"共享的内部状态"
如果每个对象都独一无二（没有共性），享元没意义。

### 🚫 外部状态管理成本 > 内存节省
享元把状态拆成两半后，客户端要自己管外部状态 → 代码变复杂。如果内存节省不明显，得不偿失。

### 🚫 状态可变
享元必须是**不可变**的（共享对象被多线程改会乱）。如果状态要改，改成多个实例。

---

## 🎯 识别"该用享元"的信号

- 需要**大量相似对象**（上千、上万）
- 对象**大部分状态相同**，只有少数字段不同
- 对象状态可以**清晰区分**内部/外部
- **内存成本是问题**（或初始化成本，比如纹理加载）

**典型案例**：
- 游戏里的粒子系统
- 编辑器里的字符（一篇文章几百万字符，只有几百种字体）
- 棋类游戏
- 大量相似 UI 元素（地图上的图标）

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么享元对象**必须不可变**？（答：多个客户共享，谁改谁影响全局）
2. `String.intern()` 做了什么？（答：手动把字符串放进 String 常量池 / 从池里取）
3. Integer 缓存为什么是 -128 ~ 127？（答：这个范围覆盖了大多数日常使用场景，超出的值用得少）

### 小练习

**改造 FlyweightDemo.java**
加一个 `LightGrayStone`（浅灰色备份子）。改完后观察：
- 享元池里现在有几个对象？
- 画 300 颗棋子（随机三种颜色）占用多少对象？

---

## 🏁 结构型七兄弟 · 全部完成 🎉

```
✅ 01-adapter      适配器 · 插头转换
✅ 02-facade       外观 · 智能家居
✅ 03-proxy        代理 · 明星经纪人
✅ 04-composite    组合 · 文件系统
✅ 05-decorator    装饰器 · 咖啡加料
✅ 06-bridge       桥接 · 遥控器×电视
✅ 07-flyweight    享元 · 围棋棋子
```

**结构型 7 / 7 · 完全搞定！** 🎊

下一个阶段：**行为型（11 个）** —— 讲对象之间**怎么互动**。

---

## 🙋 下一步

- **"都懂了，下一课"** → 第 13 课 · 模板方法 Template Method（行为型开篇）
- **"享元再举例"** → 我用别的场景讲
- **"先 commit 一下"** → 这批结构型课程全推上去

**结构型七模式 = 你对"对象如何组合"的完整认知** 👇

| 模式 | 一句话 |
|------|-------|
| Adapter | 让两边接口对上 |
| Facade | 简化复杂子系统 |
| Proxy | 代替真对象 |
| Composite | 树形嵌套 |
| Decorator | 动态加功能 |
| Bridge | 两维度独立演化 |
| Flyweight | 共享减少内存 |

**七种结构，覆盖了现实中 99% 的"对象组合"场景**。
