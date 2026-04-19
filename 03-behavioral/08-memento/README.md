# 第 20 课 · 备忘录 Memento ★★

> 类型：行为型 | 难度：★★ | GoF 经典 | **undo / 存档 / 快照的模式**

## 🎯 本课目标

- [x] 理解"**保存对象状态以便恢复**"
- [x] 掌握 Memento 的 3 个角色
- [x] 看清 Memento 和 Command（命令）的 undo 各自怎么实现

---

## 🎬 场景：游戏存档

你玩 RPG 游戏：
- 当前：10 级、HP 100、在村庄
- **存档**后继续玩
- 不小心被 Boss 秒杀 → HP 0，掉装备
- **读档** → 回到存档时的状态

**本质**：在某个时刻**保存对象的完整状态**，以后可以**恢复**。

类似场景：
- 📝 Word 的 **Ctrl+Z**
- 🎮 游戏的快速存档
- 🔄 IDE 的**撤销/重做**
- 💾 数据库的**快照 / 事务回滚**

---

## 🤔 土办法：暴露字段

```java
class Character {
    public int level;           // 🚨 暴露
    public int hp;
    public String location;
}

// 客户端手动保存
Character hero = ...;
int savedLevel = hero.level;    // 一个字段一个字段抄
int savedHp = hero.hp;
String savedLocation = hero.location;

// 玩一会儿...

// 手动恢复
hero.level = savedLevel;
hero.hp = savedHp;
hero.location = savedLocation;
```

跑 [code/BadMemento.java](code/BadMemento.java)。

**痛点**：
- 🙁 **破坏封装** —— 字段必须 public
- 🙁 **字段增加** → 所有保存/恢复的代码都要改
- 🙁 **存档逻辑散落**在调用方
- 🙁 无法**多份存档**（只能存一份变量）

---

## 💡 备忘录模式登场

**核心思想**：**让对象自己把状态打包成一个"备忘录"对象，交给管理者保管**。

### 三个角色

```java
// Originator（原发器）：要被保存状态的对象
class Character {
    private int level;
    private int hp;
    private String location;

    // 保存状态 → 产出一个 Memento
    public Memento save() {
        return new Memento(level, hp, location);
    }

    // 从 Memento 恢复
    public void restore(Memento m) {
        this.level = m.level;
        this.hp = m.hp;
        this.location = m.location;
    }
}

// Memento（备忘录）：保存状态的快照（通常不可变）
class Memento {
    final int level;
    final int hp;
    final String location;

    public Memento(int level, int hp, String location) {
        this.level = level;
        this.hp = hp;
        this.location = location;
    }
}

// Caretaker（管理者）：管理 Memento（只管存，不关心内部）
class SaveSlots {
    private Deque<Memento> saves = new ArrayDeque<>();

    public void save(Memento m) { saves.push(m); }
    public Memento load()       { return saves.pop(); }
}
```

### 使用

```java
Character hero = new Character();
hero.setLevel(10);
hero.setHp(100);
hero.setLocation("村庄");

SaveSlots manager = new SaveSlots();
manager.save(hero.save());                   // 存档

// 玩一会儿，挂了
hero.setLevel(15);
hero.setHp(0);
hero.setLocation("坟场");

// 读档
hero.restore(manager.load());
// 回到 10 级、HP 100、村庄
```

跑 [code/MementoDemo.java](code/MementoDemo.java) 看完整存档/读档流程。

---

## 🧩 三个角色

| 角色 | 本例 | 职责 |
|------|------|------|
| **Originator**（原发器）| `Character` | 产出 / 还原自己的状态 |
| **Memento**（备忘录）| `Memento` | 保存状态快照（通常不可变）|
| **Caretaker**（管理者）| `SaveSlots` | 存多个 Memento 管理（不看内部）|

**关键**：**Caretaker 不知道 Memento 里有什么**，只负责管理它。

---

## 🔒 封装性保证（进阶技巧）

纯粹的 Memento 模式要求：**只有 Originator 能读 Memento 的内容**。

实现技巧：**把 Memento 做成 Originator 的静态内部类**。

```java
class Character {
    private int level;

    public static class Memento {
        private final int level;           // private 字段

        private Memento(int level) {       // private 构造
            this.level = level;
        }
    }

    public Memento save() {
        return new Memento(this.level);    // Character 自己能 new
    }

    public void restore(Memento m) {
        this.level = m.level;              // Character 自己能读（同类内）
    }
}

// Caretaker 只能【持有 Memento 引用】，但看不到内部
SaveSlots.save(hero.save());               // ✅ 能保存
// saves.get(0).level;                     // ❌ private 访问不到
```

**这样 Memento 只对 Originator 开放**，对外部**不透明**（Caretaker 只能"搬运"）。

---

## 📐 UML 结构

```
┌────────────────┐           ┌──────────────┐
│  Character     │──创建─────▶│  Memento     │
│  (Originator)  │           │  -state      │
├────────────────┤           └──────────────┘
│ +save()        │                   ▲
│ +restore(m)    │                   │ 保管（不看内部）
└────────────────┘                   │
                              ┌──────┴──────┐
                              │ SaveSlots   │
                              │ (Caretaker) │
                              ├─────────────┤
                              │ -stack      │
                              │ +save(m)    │
                              │ +load()     │
                              └─────────────┘
```

---

## 🎁 和 Command 模式的 undo 对比

**两种 undo 实现方式**：

| 方式 | Memento | Command |
|------|---------|---------|
| 实现 | **保存完整状态**，恢复时全覆盖 | 每个操作有 `undo()` 方法**反向操作** |
| 存储 | 完整快照 | 操作记录 |
| 典型场景 | 游戏存档、大对象快照 | 编辑器单次操作 |
| 空间 | 可能占大（每次存完整状态）| 省（只存动作）|
| 时间 | 快（直接替换） | 慢（要重放所有操作） |

**工作中常见的 undo 实现**：
- 文本编辑器：Command（每次输入/删除 = 一个 Command）
- 游戏存档：Memento（完整状态）
- 数据库事务：Memento（快照） + Command（日志）组合

---

## 🌍 真实应用

| 场景 | Memento 体现 |
|------|------------|
| **游戏存档** | 本课例子 |
| **IDE 撤销** | 每次操作存一份快照 |
| **数据库快照** | MVCC / 事务开始时的快照 |
| **Git** | 每个 commit 就是整个工作树的 Memento |
| **浏览器前进/后退** | 每个页面状态是 Memento |
| **虚拟机快照** | VMware / VirtualBox 快照机制 |

---

## ⚠️ 什么时候别用

### 🚫 状态特别大
完整拷贝一份巨大对象 → 内存压力。考虑增量式 Memento。

### 🚫 状态经常变
如果每毫秒都要存档，开销 = 灾难。

### 🚫 能用 Command undo 替代
小步操作（每次点键盘）用 Command 更省空间。

---

## 📝 思考题 & 小练习

### 思考题

1. Memento 为什么通常设计成**不可变**（字段 final）？（答：保证快照的安全性，多线程共享也无妨）
2. 如果对象有**很多字段**，Memento 是不是要复制一堆？能优化吗？（答：是的。可以用"写时复制"/"增量 Memento"/引用共享不变部分）
3. Memento 和"对象的深拷贝"什么关系？（答：Memento 本质就是深拷贝，只是做了封装 + 管理）

### 小练习

**加一个 redo 功能**

基于 `MementoDemo.java`，用两个栈（undoStack + redoStack）实现完整的 undo/redo：
- `save()` → 保存到 undoStack，清空 redoStack
- `undo()` → undoStack pop 进 redoStack
- `redo()` → redoStack pop 进 undoStack

---

## 🏁 学完后

- **"懂了，下一课"** → 第 21 课 · 中介者 Mediator（机场塔台指挥飞机）
- **"Memento vs Command 再对比"** → 我可以继续讲
- **"先 commit"** → 我帮你

**Memento 是"保存/恢复"场景的优雅抽象**。游戏 / 编辑器 / 事务 全能用 💾
