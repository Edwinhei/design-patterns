/**
 * ============================================================
 * 🚫 不用模板方法 · 重复代码两份
 * ============================================================
 *
 * 场景：咖啡和茶的制作流程几乎一样
 *
 * 土办法：每种饮料自己写完整流程 → boilWater / pourInCup 写两遍
 *
 * 运行方式：
 *   java BadTemplate.java
 */

public class BadTemplate {

    public static void main(String[] args) {
        System.out.println("=== 土办法：每种饮料独立写流程 ===\n");

        new NaiveCoffee().prepare();
        System.out.println();
        new NaiveTea().prepare();

        System.out.println("\n⚠️  问题：");
        System.out.println("1. boilWater / pourInCup 在两个类里重复写");
        System.out.println("2. 流程改了（比如烧水要煮沸 5 分钟）→ 两处都要改");
        System.out.println("3. 加新饮料（热巧克力）→ 又要抄一遍完整骨架");

        System.out.println("\n👉 看 TemplateDemo.java 如何用模板方法消除重复");
    }
}

// ================================================================
// 咖啡（自己实现完整流程）
// ================================================================
class NaiveCoffee {
    public void prepare() {
        // 🔴 重复代码
        System.out.println("🔥 烧水");

        // 独特步骤
        System.out.println("☕ 冲咖啡粉");

        // 🔴 重复代码
        System.out.println("🫗 倒入杯中");

        // 独特步骤
        System.out.println("🥛 加糖和奶");
    }
}

// ================================================================
// 茶（又把骨架抄一遍）
// ================================================================
class NaiveTea {
    public void prepare() {
        // 🔴 和咖啡一样
        System.out.println("🔥 烧水");

        // 独特步骤
        System.out.println("🍵 泡茶叶");

        // 🔴 和咖啡一样
        System.out.println("🫗 倒入杯中");

        // 独特步骤
        System.out.println("🍋 加柠檬");
    }
}
