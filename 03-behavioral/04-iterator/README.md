# 第 16 课 · 迭代器 Iterator ★

> 类型：行为型 | 难度：★ | GoF 经典 | **Java 已内置到语言层面（`for-each`）**

## 🎯 本课目标

- [x] 理解"**顺序访问集合而不暴露内部结构**"
- [x] 掌握 Java 的 **`Iterator`** 和 **`Iterable`** 两个接口
- [x] 看懂 `for-each` 循环背后的机制

---

## 🎬 场景：音乐播放器的歌单

你打开音乐 App，有一个"播放列表"。你想：
- 从头到尾播放
- 用代码遍历做点什么（比如过滤、统计时长）

**问题**：播放列表内部可能用**数组**、**链表**、**数据库游标**存储，客户端怎么遍历？

### 土办法：客户端懂内部结构

```java
class NaivePlaylist {
    public Song[] songs;       // 🚨 公开暴露内部数组
}

// 客户端
for (int i = 0; i < playlist.songs.length; i++) {
    Song song = playlist.songs[i];
    // ...
}
```

**痛点**：
- 🙁 客户端必须**知道内部是数组**
- 🙁 换成 `List<Song>` 内部实现 → **所有客户端代码都要改**
- 🙁 不能加"只遍历前 10 首"这种逻辑（客户端手动 break）
- 🙁 破坏封装

跑 [code/BadIterator.java](code/BadIterator.java)。

---

## 💡 迭代器模式登场

**核心思想**：**提供一个"游标"对象，客户端只需问它"有没有下一个"，不管内部怎么存**。

### Java 已经帮你设计好两个接口

```java
// 游标接口
public interface Iterator<E> {
    boolean hasNext();      // 还有下一个吗？
    E next();               // 拿下一个
    default void remove() { ... }    // 可选：删当前
}

// "我可以被遍历"的标签
public interface Iterable<E> {
    Iterator<E> iterator();   // 返回一个游标
}
```

**标准流程**：
1. 你的集合 `implements Iterable<E>`
2. 提供 `iterator()` 方法返回一个 `Iterator<E>`
3. 客户端调 `iterator()`，然后 `while (it.hasNext()) { it.next(); }`

### 代码

```java
class Playlist implements Iterable<Song> {
    private Song[] songs;

    @Override
    public Iterator<Song> iterator() {
        return new Iterator<Song>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < songs.length;
            }

            @Override
            public Song next() {
                return songs[index++];
            }
        };
    }
}
```

**客户端**：
```java
Iterator<Song> it = playlist.iterator();
while (it.hasNext()) {
    Song song = it.next();
    System.out.println(song.getName());
}
```

---

## 🎁 `for-each` 就是迭代器的**语法糖**

```java
for (Song song : playlist) {
    System.out.println(song.getName());
}
```

**编译器自动展开成**：

```java
Iterator<Song> __iterator = playlist.iterator();
while (__iterator.hasNext()) {
    Song song = __iterator.next();
    System.out.println(song.getName());
}
```

**只要你的类 `implements Iterable<T>`，就能用 for-each**。JDK 所有集合都实现了它。

跑 [code/IteratorDemo.java](code/IteratorDemo.java) 看完整对比。

---

## 📐 UML 结构

```
┌────────────────────┐         ┌────────────────────┐
│ Iterable<E> (接口) │────────▶│ Iterator<E> (接口) │
├────────────────────┤  iterator├────────────────────┤
│ +iterator(): Iter  │         │ +hasNext(): boolean│
└─────────▲──────────┘         │ +next(): E         │
          │                    │ +remove()          │
          │                    └──────────▲─────────┘
          │                               │
┌─────────┴──────────┐         ┌──────────┴──────────┐
│  Playlist          │         │ PlaylistIterator    │
│  (具体集合)         │ 产生 ───▶│ (具体迭代器)         │
└────────────────────┘         └─────────────────────┘
```

---

## 🌍 Java 所有集合都是 Iterable

```java
List<String> list = new ArrayList<>();
Set<String> set = new HashSet<>();
Map<String, Integer> map = new HashMap<>();

// 都能用 for-each
for (String s : list) { ... }
for (String s : set) { ... }

// Map 稍特殊：要遍历 entries
for (Map.Entry<String, Integer> entry : map.entrySet()) { ... }
```

**这就是 Java 集合框架"一视同仁"的基础** —— 通过 `Iterable` 接口统一遍历。

---

## 🎯 为什么 Iterator 模式被语言"内置"了

Iterator 是最基础的模式之一 —— **所有编程语言都需要"遍历集合"**。

各语言的方案：
- **Java**：`Iterator` + `Iterable` + `for-each` 语法糖
- **Python**：`__iter__` + `__next__` + `for x in xs`
- **JavaScript**：`Symbol.iterator` + `for...of`
- **C#**：`IEnumerable` + `IEnumerator` + `foreach`
- **Rust**：`Iterator` trait + `for x in xs`

**看起来不同，本质完全一样**。

---

## 🔀 Iterator vs Stream（Java 8+）

Java 8 引入了 Stream API，可以视为"**函数式版的迭代器**"：

```java
// 迭代器方式（命令式）
Iterator<Song> it = playlist.iterator();
while (it.hasNext()) {
    Song s = it.next();
    if (s.getDuration() > 200) {
        System.out.println(s.getName());
    }
}

// Stream 方式（声明式）
playlist.stream()
        .filter(s -> s.getDuration() > 200)
        .forEach(s -> System.out.println(s.getName()));
```

**Stream 底层还是靠 Iterator**，但提供了更高级的抽象（filter / map / reduce）。

**工作中的选择**：
- 简单遍历 → `for-each`
- 复杂处理（过滤/映射/聚合）→ Stream

---

## ⚠️ 什么时候别用

### 🚫 集合已经实现了 Iterable
JDK 所有集合都实现了。你直接用 `for-each` 即可，**不需要自己写迭代器**。

### 🚫 非集合场景
如果你的类不是"一堆元素的容器"，强行加 Iterator 是过度设计。

### 🚫 需要并行遍历
Iterator 是**单线程**遍历模型。并行用 `Stream.parallel()` 或 `Spliterator`。

---

## 📝 思考题 & 小练习

### 思考题

1. 为什么遍历 `HashMap` 要用 `entrySet()` 而不能直接 `for (? : map)`？（答：Map 不是 Iterable，要通过 keySet / values / entrySet 转换）
2. 在 `for-each` 循环里调 `collection.remove(...)` 会出什么问题？（答：`ConcurrentModificationException`，因为 Iterator 内部有 modCount 校验）
3. Stream 是不是 Iterable？（答：不是。`Stream` 实现了 `BaseStream`，可以 `stream.iterator()` 但不能 for-each）

### 小练习

**练习：倒序迭代器**
基于 `IteratorDemo.java`，加一个 `reverseIterator()` 方法，返回一个**倒序**的迭代器。
使用：
```java
for (Song s : playlist.reversed()) {
    System.out.println(s);
}
```

提示：需要把"反向遍历"也做成 `Iterable`。

---

## 🏁 学完后

- **"懂了，下一课"** → 第 17 课 · 责任链 Chain of Responsibility（请假审批）
- **"想深入 Stream"** → 可以单独沉淀
- **"先 commit"** → 我帮你

**迭代器是设计模式里最"透明"的一个** —— Java 已经帮你内置好了。这一课主要是**揭开 for-each 的面纱** 🎵
