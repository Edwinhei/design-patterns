/**
 * ============================================================
 * ✅ 组合模式 Composite · 文件系统场景
 * ============================================================
 *
 * 核心思路：
 *   - 抽象一个统一接口 FileSystemNode
 *   - 叶子（MyFile）和组合（Folder）都实现这个接口
 *   - Folder 内部装的是 FileSystemNode 列表（统一类型）
 *   - 客户端只和 FileSystemNode 打交道，不关心是文件还是文件夹
 *
 * 运行方式：
 *   java CompositeDemo.java
 */

import java.util.*;

public class CompositeDemo {

    public static void main(String[] args) {
        // ===== 构造树形结构 =====
        Folder root = new Folder("我的项目");

        Folder docs = new Folder("docs");
        docs.add(new MyFile("报告.pdf", 1024));
        docs.add(new MyFile("笔记.txt", 512));

        Folder photos = new Folder("photos");
        photos.add(new MyFile("vacation.jpg", 2048));

        root.add(docs);
        root.add(photos);
        root.add(new MyFile("README.md", 256));

        // ===== 客户端统一处理（关键！）=====

        System.out.println("📊 总大小（一行搞定，自动递归）:");
        System.out.println("   " + root.getSize() + " KB\n");

        System.out.println("📂 目录树结构:");
        root.print(0);

        // ===== 最关键的一点：客户端代码 =====
        System.out.println("\n✨ 关键观察：");
        System.out.println("  ① 计算大小：root.getSize() 一行搞定");
        System.out.println("  ② 打印目录：root.print(0) 一行搞定");
        System.out.println("  ③ 不用区分 root 是文件还是文件夹");
        System.out.println("  ④ 加新节点类型（如 Symlink）→ 只加一个类，客户端不用改");
    }
}

// ================================================================
// 统一接口：FileSystemNode（Component）
// ================================================================
abstract class FileSystemNode {
    protected String name;

    public FileSystemNode(String name) {
        this.name = name;
    }

    public abstract int getSize();
    public abstract void print(int indent);

    protected void indent(int n) {
        for (int i = 0; i < n; i++) System.out.print("  ");
    }
}

// ================================================================
// 叶子：MyFile
// ================================================================
class MyFile extends FileSystemNode {
    private final int size;

    public MyFile(String name, int size) {
        super(name);
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;           // 叶子直接返回自己的大小
    }

    @Override
    public void print(int indent) {
        indent(indent);
        System.out.println("📄 " + name + " (" + size + " KB)");
    }
}

// ================================================================
// 组合：Folder
// ----------------------------------------------------------------
// 关键：children 的类型是 FileSystemNode，不是 Folder 或 MyFile
//       这就是 Composite 模式的精髓
// ================================================================
class Folder extends FileSystemNode {

    // 👇 存的是统一接口，文件和文件夹都能放
    private final List<FileSystemNode> children = new ArrayList<>();

    public Folder(String name) {
        super(name);
    }

    public void add(FileSystemNode node) {
        children.add(node);
    }

    @Override
    public int getSize() {
        // 👇 递归求和，每个子节点都调 getSize()
        //   子节点是文件 → 返回文件大小
        //   子节点是文件夹 → 递归求子文件夹的 size
        return children.stream()
                       .mapToInt(FileSystemNode::getSize)
                       .sum();
    }

    @Override
    public void print(int indent) {
        this.indent(indent);
        System.out.println("📁 " + name + "/  (total: " + getSize() + " KB)");

        // 递归打印所有子节点
        for (FileSystemNode child : children) {
            child.print(indent + 1);
        }
    }
}
