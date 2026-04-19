/**
 * ============================================================
 * ✅ 命令模式 · 餐厅点单 + 订单历史 + 取消功能
 * ============================================================
 *
 * 五个角色：
 *   Command       → OrderCommand 接口
 *   ConcreteCommand → SteakOrder / PastaOrder / SaladOrder
 *   Receiver      → Chef（真正做菜）
 *   Invoker       → Waiter（服务员，管订单队列）
 *   Client        → main（组装）
 *
 * 运行方式：
 *   java CommandDemo.java
 */

import java.util.*;

public class CommandDemo {

    public static void main(String[] args) {
        Chef chef = new Chef();
        Waiter waiter = new Waiter();

        // === 场景 1：正常点单 ===
        System.out.println("===== 客人点单 =====");
        waiter.placeOrder(new SteakOrder(chef));
        waiter.placeOrder(new PastaOrder(chef));
        waiter.placeOrder(new SaladOrder(chef));

        // === 场景 2：客人改主意，取消最后一单 ===
        System.out.println("\n===== 客人：沙拉不要了 =====");
        waiter.cancelLast();

        // === 场景 3：查看历史 ===
        System.out.println("\n===== 当前订单历史 =====");
        waiter.printHistory();

        // === 场景 4：宏命令（一次多点）===
        System.out.println("\n===== 宏命令：一键套餐 =====");
        MealCombo combo = new MealCombo(Arrays.asList(
            new SteakOrder(chef),
            new SaladOrder(chef)
        ));
        waiter.placeOrder(combo);

        System.out.println("\n✨ Command 的威力：");
        System.out.println("  ① 请求被封装成对象，可以排队 / 记录 / 撤销");
        System.out.println("  ② Waiter 不认识具体菜品，只知道 execute()");
        System.out.println("  ③ 加新菜 → 加一个 Command 类，不改 Waiter");
        System.out.println("  ④ 宏命令：组合多个 Command 成一个");
    }
}

// ================================================================
// 命令接口
// ================================================================
interface OrderCommand {
    void execute();
    void undo();
    String describe();
}

// ================================================================
// Receiver：厨师（真正做事的人）
// ================================================================
class Chef {
    public void cookSteak()  { System.out.println("  🥩 厨师: 做牛排"); }
    public void cookPasta()  { System.out.println("  🍝 厨师: 做意面"); }
    public void makeSalad()  { System.out.println("  🥗 厨师: 做沙拉"); }
    public void cancel(String dish) {
        System.out.println("  ❌ 厨师: 撤销 " + dish);
    }
}

// ================================================================
// 具体命令
// ================================================================
class SteakOrder implements OrderCommand {
    private final Chef chef;
    public SteakOrder(Chef chef) { this.chef = chef; }
    public void execute()     { chef.cookSteak(); }
    public void undo()        { chef.cancel("牛排"); }
    public String describe()  { return "牛排"; }
}

class PastaOrder implements OrderCommand {
    private final Chef chef;
    public PastaOrder(Chef chef) { this.chef = chef; }
    public void execute()     { chef.cookPasta(); }
    public void undo()        { chef.cancel("意面"); }
    public String describe()  { return "意面"; }
}

class SaladOrder implements OrderCommand {
    private final Chef chef;
    public SaladOrder(Chef chef) { this.chef = chef; }
    public void execute()     { chef.makeSalad(); }
    public void undo()        { chef.cancel("沙拉"); }
    public String describe()  { return "沙拉"; }
}

// ================================================================
// 宏命令：组合多个命令成一个
// ================================================================
class MealCombo implements OrderCommand {
    private final List<OrderCommand> orders;

    public MealCombo(List<OrderCommand> orders) {
        this.orders = orders;
    }

    public void execute() {
        System.out.println("  🎁 套餐开始:");
        for (OrderCommand cmd : orders) cmd.execute();
    }

    public void undo() {
        System.out.println("  🎁 套餐撤销:");
        for (int i = orders.size() - 1; i >= 0; i--) {
            orders.get(i).undo();
        }
    }

    public String describe() {
        StringBuilder sb = new StringBuilder("套餐(");
        for (OrderCommand o : orders) sb.append(o.describe()).append(",");
        return sb.append(")").toString();
    }
}

// ================================================================
// Invoker：服务员，维护订单历史
// ================================================================
class Waiter {
    private final Deque<OrderCommand> history = new ArrayDeque<>();

    public void placeOrder(OrderCommand cmd) {
        System.out.println("📋 客人点单: " + cmd.describe());
        cmd.execute();
        history.push(cmd);
    }

    public void cancelLast() {
        if (history.isEmpty()) {
            System.out.println("（没有可取消的订单）");
            return;
        }
        OrderCommand last = history.pop();
        System.out.println("📋 取消: " + last.describe());
        last.undo();
    }

    public void printHistory() {
        if (history.isEmpty()) {
            System.out.println("  （无订单）");
            return;
        }
        Iterator<OrderCommand> it = history.descendingIterator();
        int i = 1;
        while (it.hasNext()) {
            System.out.println("  " + (i++) + ". " + it.next().describe());
        }
    }
}
