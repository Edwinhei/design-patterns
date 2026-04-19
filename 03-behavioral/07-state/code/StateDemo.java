/**
 * ============================================================
 * ✅ 状态模式 · 订单生命周期
 * ============================================================
 *
 * 设计：
 *   - Order（Context）持有当前状态
 *   - 每个状态一个类，实现 OrderState 接口
 *   - 状态类决定什么操作允许、切换到什么状态
 *
 * 运行方式：
 *   java StateDemo.java
 */

public class StateDemo {

    public static void main(String[] args) {
        Order order = new Order();

        System.out.println("===== 正常流程：付款 → 发货 → 完成 =====");
        order.pay();
        order.ship();
        order.complete();

        System.out.println("\n===== 尝试在已完成订单上 pay =====");
        order.pay();      // 应该被拒绝

        // === 演示取消流程 ===
        System.out.println("\n===== 新订单：付款后取消 =====");
        Order order2 = new Order();
        order2.pay();
        order2.cancel();

        System.out.println("\n===== 新订单：直接取消（未付款）=====");
        Order order3 = new Order();
        order3.cancel();

        System.out.println("\n===== 尝试在已取消订单上 ship =====");
        order3.ship();   // 拒绝

        System.out.println("\n✨ 状态模式的威力：");
        System.out.println("  ① 每个状态是一个独立的类");
        System.out.println("  ② 状态类自己决定允许什么操作、转到什么状态");
        System.out.println("  ③ Context（Order）只负责委托给当前状态");
        System.out.println("  ④ 消灭 if-else，状态流转集中");
        System.out.println("  ⑤ 加新状态（如 Refunded）只需加一个类");
    }
}

// ================================================================
// 状态接口
// ================================================================
interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void complete(Order order);
    void cancel(Order order);
    String name();
}

// 默认行为（拒绝）
abstract class BaseState implements OrderState {
    public void pay(Order order)      { reject(order, "pay"); }
    public void ship(Order order)     { reject(order, "ship"); }
    public void complete(Order order) { reject(order, "complete"); }
    public void cancel(Order order)   { reject(order, "cancel"); }

    protected void reject(Order order, String action) {
        System.out.println("❌ 当前状态 [" + name() + "] 不允许: " + action);
    }
}

// ================================================================
// 具体状态：待付款
// ================================================================
class PendingPayment extends BaseState {
    @Override
    public void pay(Order order) {
        System.out.println("💰 支付成功");
        order.setState(new Paid());
    }

    @Override
    public void cancel(Order order) {
        System.out.println("❌ 订单已取消");
        order.setState(new Cancelled());
    }

    @Override
    public String name() { return "待付款"; }
}

// ================================================================
// 具体状态：已付款
// ================================================================
class Paid extends BaseState {
    @Override
    public void ship(Order order) {
        System.out.println("🚚 订单已发货");
        order.setState(new Shipped());
    }

    @Override
    public void cancel(Order order) {
        System.out.println("💸 退款并取消");
        order.setState(new Cancelled());
    }

    @Override
    public String name() { return "已付款"; }
}

// ================================================================
// 具体状态：已发货
// ================================================================
class Shipped extends BaseState {
    @Override
    public void complete(Order order) {
        System.out.println("✅ 订单已完成");
        order.setState(new Completed());
    }

    @Override
    public String name() { return "已发货"; }
}

// ================================================================
// 终态：已完成
// ================================================================
class Completed extends BaseState {
    @Override
    public String name() { return "已完成"; }
    // 所有操作都走 BaseState 的默认 reject
}

// ================================================================
// 终态：已取消
// ================================================================
class Cancelled extends BaseState {
    @Override
    public String name() { return "已取消"; }
}

// ================================================================
// Context：订单
// ================================================================
class Order {
    private OrderState state = new PendingPayment();      // 初始状态

    public void setState(OrderState state) {
        System.out.println("  [状态转换] " + this.state.name() + " → " + state.name());
        this.state = state;
    }

    // 所有操作都委托给当前状态
    public void pay()      { state.pay(this); }
    public void ship()     { state.ship(this); }
    public void complete() { state.complete(this); }
    public void cancel()   { state.cancel(this); }
}
