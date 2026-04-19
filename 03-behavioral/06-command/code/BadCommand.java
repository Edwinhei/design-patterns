/**
 * ============================================================
 * 🚫 不用命令模式 · 客人直接喊厨师
 * ============================================================
 *
 * 场景：餐厅点单
 *
 * 运行方式：
 *   java BadCommand.java
 */

public class BadCommand {

    public static void main(String[] args) {
        NaiveChef chef = new NaiveChef();
        NaiveCustomer customer = new NaiveCustomer(chef);

        System.out.println("=== 土办法：客人直接对厨师喊话 ===\n");

        customer.orderSteak();
        customer.orderPasta();
        // customer.cancelLast();   // 🚨 做不到，没有"订单对象"可以撤销

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 客人和厨师【紧耦合】");
        System.out.println("2. 请求即执行，没法排队");
        System.out.println("3. 没法记录【订单历史】");
        System.out.println("4. 没法【撤销】（请求已发出，没有对象）");
        System.out.println("5. 厨师有新方法 → 客人也要改");

        System.out.println("\n👉 看 CommandDemo.java 如何把请求封装成对象");
    }
}

class NaiveChef {
    public void cookSteak() { System.out.println("🥩 做牛排"); }
    public void cookPasta() { System.out.println("🍝 做意面"); }
}

class NaiveCustomer {
    private final NaiveChef chef;

    public NaiveCustomer(NaiveChef chef) {
        this.chef = chef;
    }

    public void orderSteak() {
        chef.cookSteak();          // 🚨 直接调
    }

    public void orderPasta() {
        chef.cookPasta();          // 🚨 直接调
    }
}
