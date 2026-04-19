/**
 * ============================================================
 * 🚫 不用状态模式 · if-else 判断状态
 * ============================================================
 *
 * 场景：订单生命周期
 *
 * 运行方式：
 *   java BadState.java
 */

public class BadState {

    public static void main(String[] args) {
        System.out.println("=== 土办法：用 String 存状态 + if-else ===\n");

        NaiveOrder order = new NaiveOrder();
        order.pay();
        order.ship();
        order.complete();

        System.out.println("\n-- 尝试在已完成订单上付款 --");
        try {
            order.pay();
        } catch (IllegalStateException e) {
            System.out.println("❌ " + e.getMessage());
        }

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 每个方法都有 if-else 判断状态");
        System.out.println("2. 加新状态（比如已退款审核中）→ 所有方法都要改");
        System.out.println("3. 状态流转逻辑散落各处");
        System.out.println("4. 违反开闭原则");

        System.out.println("\n👉 看 StateDemo.java 如何用状态模式优雅解决");
    }
}

class NaiveOrder {
    private String status = "待付款";

    public void pay() {
        System.out.println("当前状态: " + status + " → 执行 pay");
        if (status.equals("待付款")) {
            System.out.println("  💰 支付成功");
            status = "已付款";
        } else {
            throw new IllegalStateException("不能在 " + status + " 付款");
        }
    }

    public void ship() {
        System.out.println("当前状态: " + status + " → 执行 ship");
        if (status.equals("已付款")) {
            System.out.println("  🚚 发货");
            status = "已发货";
        } else {
            throw new IllegalStateException("不能在 " + status + " 发货");
        }
    }

    public void complete() {
        System.out.println("当前状态: " + status + " → 执行 complete");
        if (status.equals("已发货")) {
            System.out.println("  ✅ 订单完成");
            status = "已完成";
        } else {
            throw new IllegalStateException("不能在 " + status + " 完成");
        }
    }
}
