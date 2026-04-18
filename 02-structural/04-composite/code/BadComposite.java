/**
 * ============================================================
 * 🚫 不用 Composite · 文件和文件夹分开处理
 * ============================================================
 *
 * 场景：想统计文件夹总大小
 *
 * 土办法：
 *   - Folder 分两个列表装（files / subFolders）
 *   - 客户端要区分处理
 *   - 递归代码冗余
 *
 * 运行方式：
 *   java BadComposite.java
 */

import java.util.*;

public class BadComposite {

    public static void main(String[] args) {
        // 构造文件树
        Folder root = new Folder("我的项目");

        Folder docs = new Folder("docs");
        docs.files.add(new SimpleFile("report.pdf", 1024));
        docs.files.add(new SimpleFile("notes.txt", 512));

        Folder photos = new Folder("photos");
        photos.files.add(new SimpleFile("vacation.jpg", 2048));

        root.subFolders.add(docs);
        root.subFolders.add(photos);
        root.files.add(new SimpleFile("README.md", 256));

        // 🚨 客户端计算总大小：要手写递归
        System.out.println("总大小: " + computeSize(root) + " KB");

        System.out.println("\n⚠️  问题：");
        System.out.println("1. Folder 维护两个列表（files + subFolders）");
        System.out.println("2. 客户端要区分处理文件和文件夹");
        System.out.println("3. 加新类型节点（软链接）→ 到处改");
        System.out.println("4. 统计 / 遍历 / 搜索 每个操作都要写两遍");

        System.out.println("\n👉 看 CompositeDemo.java 如何统一抽象");
    }

    // 计算文件夹总大小（土办法：分开处理）
    static int computeSize(Folder f) {
        int total = 0;
        for (SimpleFile file : f.files) {
            total += file.size;
        }
        for (Folder sub : f.subFolders) {
            total += computeSize(sub);
        }
        return total;
    }
}

// ================================================================
// 文件
// ================================================================
class SimpleFile {
    String name;
    int size;

    public SimpleFile(String name, int size) {
        this.name = name;
        this.size = size;
    }
}

// ================================================================
// 文件夹（维护两个列表，粗糙）
// ================================================================
class Folder {
    String name;
    List<SimpleFile> files = new ArrayList<>();        // 子文件
    List<Folder> subFolders = new ArrayList<>();        // 子文件夹

    public Folder(String name) { this.name = name; }
}
