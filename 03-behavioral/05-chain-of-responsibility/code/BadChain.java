/**
 * ============================================================
 * 🚫 不用责任链 · if-else 硬编码审批逻辑
 * ============================================================
 *
 * 场景：请假审批
 *
 * 运行方式：
 *   java BadChain.java
 */

public class BadChain {

    public static void main(String[] args) {
        System.out.println("=== 土办法：if-else 审批 ===\n");

        approve("张三", 2);
        approve("李四", 5);
        approve("王五", 15);
        approve("赵六", 40);

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 加新审批级别（主管）→ 要改 approve 方法");
        System.out.println("2. 流程硬编码，无法动态调整顺序");
        System.out.println("3. 判断逻辑和业务逻辑耦合");
        System.out.println("4. 违反开闭原则");

        System.out.println("\n👉 看 ChainDemo.java 如何用责任链解决");
    }

    static void approve(String name, int days) {
        System.out.println(name + " 请假 " + days + " 天：");

        // 🚨 硬编码的 if-else 链
        if (days <= 3) {
            System.out.println("  👨‍💼 组长批准");
        } else if (days <= 7) {
            System.out.println("  🎩 经理批准");
        } else if (days <= 30) {
            System.out.println("  👔 总监批准");
        } else {
            System.out.println("  ❌ 超出审批范围");
        }
    }
}
