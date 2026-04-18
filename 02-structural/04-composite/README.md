# 第 9 课 · 组合 Composite ★

> 类型：结构型 | 难度：★ | GoF 经典 | **树形结构的克星**

## 🎯 本课目标

- [x] 理解"**让客户端统一处理单个对象和对象组合**"
- [x] 能用 Composite 写出优雅的树形结构代码
- [x] 识别工作中哪些场景适合用 Composite

---

## 🎬 场景：文件系统

打开你电脑的文件夹，观察一下：

```
📁 我的项目/
├── 📁 docs/
│   ├── 📄 报告.pdf (1024 KB)
│   └── 📄 笔记.txt (512 KB)
├── 📁 photos/
│   └── 📄 vacation.jpg (2048 KB)
└── 📄 README.md (256 KB)
```

**关键观察**：
- 📁 **文件夹**里可以放 📄 文件
- 📁 **文件夹**里也可以放 📁 **另一个文件夹**
- 我想知道"我的项目"总共多大 → 需要**递归累加**所有文件大小
- 我想删除"我的项目" → 需要**递归删除**所有内容

**问题**：文件和文件夹是**两种不同的东西**。

```
文件：     有大小 size
文件夹：   没自己的大小，是子项大小的总和
```

**怎么让代码"统一处理"这两种东西**？就是 Composite 解决的。

---

## 🤔 土办法：文件 vs 文件夹分开处理

```java
class File {
    String name;
    int size;
}

class Folder {
    String name;
    List<File> files;          // 子文件
    List<Folder> subFolders;    // 子文件夹
}

// 计算文件夹总大小
int computeSize(Folder f) {
    int total = 0;
    for (File file : f.files) {
        total += file.size;
    }
    for (Folder sub : f.subFolders) {
        total += computeSize(sub);   // 递归
    }
    return total;
}
```

跑一下 [code/BadComposite.java](code/BadComposite.java) 感受。

**痛点**：
- 🙁 `Folder` 要维护**两个列表**（files / subFolders）
- 🙁 客户端处理时要**区分**（我现在拿到的是文件还是文件夹？）
- 🙁 想加新节点类型（比如"软链接"），改动遍布所有代码
- 🙁 代码里到处 `for (File ...)` + `for (Folder ...)`，冗余

**本质**：**"单个"和"组合"被区别对待**，导致客户端代码复杂。

---

## 💡 Composite 模式登场

**核心思想**：**让单个对象和组合对象实现同一个接口，客户端一视同仁**。

```java
// 统一接口
abstract class FileSystemNode {
    protected String name;

    public abstract int getSize();
    public abstract void print(int indent);
}

// 叶子：文件
class MyFile extends FileSystemNode {
    private int size;

    @Override
    public int getSize() { return size; }
}

// 组合：文件夹
class Folder extends FileSystemNode {
    private List<FileSystemNode> children = new ArrayList<>();
    //             ↑
    //  注意：装的是 FileSystemNode，不是 File 或 Folder
    //  ————文件和文件夹都能放进来

    public void add(FileSystemNode node) {
        children.add(node);
    }

    @Override
    public int getSize() {
        return children.stream()
                       .mapToInt(FileSystemNode::getSize)     // 👈 统一调 getSize
                       .sum();
    }
}

// 客户端
FileSystemNode root = buildFileTree();
System.out.println("总大小: " + root.getSize());    // 😍 一视同仁
```

**客户端代码变得超简单** —— 不用区分 root 是文件还是文件夹，**统一调 `getSize()` 就行**。

### Composite 做的三件事

1. **抽象一个统一接口/基类** (`FileSystemNode`)
2. **叶子类** 实现这个接口 (`MyFile`)
3. **组合类** 实现这个接口，且**内部持有子节点列表**（列表类型是**统一接口**）

关键是：**组合类的子节点列表里既可以放叶子，也可以放另一个组合**。

跑 [code/CompositeDemo.java](code/CompositeDemo.java) 看树形结构的完整输出。

---

## 📐 UML 结构

```
┌───────────────────────────┐
│  FileSystemNode (抽象)    │  ← Component 接口
├───────────────────────────┤
│ + getSize(): int          │
│ + print(indent: int)      │
└────────▲──────────────────┘
         │
    ┌────┴─────────────────┐
    │                      │
┌──────────┐        ┌──────────────────┐
│ MyFile   │        │ Folder           │
│ (叶子)   │        │ (组合)            │
├──────────┤        ├──────────────────┤
│ size: int│        │ children: List   │
│          │        │   <FileSystemNode│
│          │        │   >              │
│+getSize()│        │ + add(node)      │
│          │        │ + getSize():     │
│          │        │   递归求和        │
└──────────┘        └──────────────────┘
                             │ 1..*
                             │ 包含
                             ▼
                    FileSystemNode
                    (自身或 MyFile)
                    ↑ 递归结构
```

**核心**：**Folder 持有 FileSystemNode 列表 → 形成递归结构（树）**。

---

## 🌍 真实应用

| 在哪里 | 谁是 Composite |
|--------|---------------|
| **文件系统** | File / Directory（本课场景）|
| **DOM 树** | HTML 元素都是 Node，div 里能装 span，span 里能装 div |
| **GUI 组件** | JPanel 里可以放 JButton，也可以放另一个 JPanel |
| **菜单系统** | MenuItem 里可以是单项，也可以是 SubMenu |
| **组织架构** | 员工 / 部门（部门里有员工或子部门）|
| **JSON** | JsonObject 里可以装基本值或另一个 JsonObject |
| **AST** | 编译器的语法树，每个节点可以是单一或复合表达式 |
| **React / Vue 组件树** | 组件里可以嵌套组件 |

**规律**：**任何"树形嵌套"结构的背后，基本都是 Composite**。

---

## ⚠️ 什么时候别用

### 🚫 结构不是树形
Composite **为树形结构而生**。如果你的数据是扁平列表或图，Composite 反而是累赘。

### 🚫 叶子和组合行为差异太大
如果 File 和 Folder 能做的事几乎完全不同，强行共用接口反而**抽象泄露**。

### 🚫 性能敏感的场景
递归遍历树性能不是最优（频繁对象创建、栈深度等）。大文件系统的遍历一般有专门的优化算法。

---

## 📝 思考题 & 小练习

### 思考题

1. 组合模式里，叶子（File）要不要提供 `add()` 方法？（答：两种设计，各有取舍。GoF 原版让叶子提供但抛异常；安全做法是只让 Folder 有）
2. 递归调用会不会栈溢出？什么场景下会？（答：极深的树会，一般不会，除非你做编译器或操作系统）
3. 客户端在调 `node.getSize()` 时，怎么区分 node 是 File 还是 Folder？（答：**不用区分**，这就是 Composite 的威力）

### 小练习

**练习 1：加一个"软链接"节点**
扩展 `CompositeDemo.java`，加一个 `Symlink extends FileSystemNode`，指向另一个 Node。它的 `getSize()` 返回 0，`print` 显示 `→ 目标名`。

**练习 2：给组合加个"查找"方法**
给 Folder 加一个 `findByName(String name)` 方法，递归查找所有子节点里叫这个名字的节点，返回列表。

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 10 课 · 装饰器 Decorator（咖啡加奶加糖）
- **"还有问题"** → 继续问
- **"先 commit 一下"** → 我帮你

**Composite 是树形结构的"万金油"**。以后你看到任何嵌套结构的代码，都会想到它 🌳
