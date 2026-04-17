/**
 * ============================================================
 * 🚫 土办法示例：不使用单例模式会发生什么
 * ============================================================
 *
 * 场景：办公室打印机
 *   理想情况：整个公司共用 1 台打印机
 *   土办法：  每个员工自己 new 一台 —— 钱白花 + 统计混乱
 *
 * 运行方式（需要 JDK 11+）：
 *   java BadSingleton.java
 *
 * ⚠️ 顺序约定：Java 单文件运行模式（JEP 330）要求"第一个类"是入口，
 *              所以 public class BadSingleton 放在文件最前，
 *              辅助类 Printer 放在下面。
 *
 * 观察点：
 *   1. "[构造]" 会被打印几次？  → 说明创建了几台打印机
 *   2. 三个人的 printedCount 分别是多少？  → 互不相识的独立计数器
 */

// ----------------------------------------------------------------
// 主程序：模拟 3 个员工各自拿到一台打印机
// ----------------------------------------------------------------
public class BadSingleton {

    public static void main(String[] args) {
        System.out.println("=== 土办法：每个用户自己 new 一台打印机 ===\n");

        // 小明自己 new 了一台
        Printer xiaoMingsPrinter = new Printer();
        xiaoMingsPrinter.print("小明", "季度报告.pdf");

        // 小红又 new 了一台
        Printer xiaoHongsPrinter = new Printer();
        xiaoHongsPrinter.print("小红", "合同.docx");

        // 小李也 new 了一台
        Printer xiaoLisPrinter = new Printer();
        xiaoLisPrinter.print("小李", "会议纪要.txt");

        // ---- 问题暴露：三台机器互相不知道对方 ----
        System.out.println("\n⚠️  问题暴露：");
        System.out.println("- 被创建了 3 台打印机（实际办公室只应有 1 台）");
        System.out.println("- 小明的机器打印量：" + xiaoMingsPrinter.getPrintedCount());
        System.out.println("- 小红的机器打印量：" + xiaoHongsPrinter.getPrintedCount());
        System.out.println("- 小李的机器打印量：" + xiaoLisPrinter.getPrintedCount());
        System.out.println("- 三台机器互不相识，月底统计要一台台查，数据还对不上！");

        // 更扎心的："身份"层面它们也不是同一个东西
        System.out.println("\n🔍 身份检查：");
        System.out.println("xiaoMingsPrinter == xiaoHongsPrinter ? "
                + (xiaoMingsPrinter == xiaoHongsPrinter));   // false
        System.out.println("xiaoHongsPrinter == xiaoLisPrinter ? "
                + (xiaoHongsPrinter == xiaoLisPrinter));     // false

        System.out.println("\n👉 下一步：去看 SingletonDemo.java 怎么把它变成"
                + "'全公司只有一台'");
    }
}

// ----------------------------------------------------------------
// Printer：办公室打印机
// 注意：这个类**没有**做任何限制，任何人都可以随便 new 一个。
//       这就是"土办法"的根源。
// ----------------------------------------------------------------
class Printer {
    // 本台打印机已打印文档数。每台机器独立计数。
    private int printedCount = 0;

    // ⚠️ 构造器是 public（默认），外部可以随便 new
    public Printer() {
        System.out.println("🖨  [构造] 新的打印机被创建！耗费资源");
    }

    public void print(String user, String doc) {
        printedCount++;
        System.out.println("  ↳ " + user + " 打印了：" + doc
                + "（本机已打印 " + printedCount + " 份）");
    }

    public int getPrintedCount() {
        return printedCount;
    }
}
