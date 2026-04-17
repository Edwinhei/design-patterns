/**
 * ============================================================
 * 🚫 土办法示例：不用工厂方法会发生什么
 * ============================================================
 *
 * 场景：披萨店下单系统
 *   每当客人点一种披萨，业务代码里就要 new 一个对应类型。
 *
 * 运行方式（需要 JDK 11+）：
 *   java BadPizzaStore.java
 *
 * 观察点：
 *   1. orderPizza 方法里的 if-else 有多长？
 *   2. 如果要新增一种"海鲜披萨"，要改哪些地方？
 *   3. 如果要开分店（纽约风/芝加哥风），怎么办？
 */

// ----------------------------------------------------------------
// 主程序：披萨店下单系统
// ----------------------------------------------------------------
public class BadPizzaStore {

    public static void main(String[] args) {
        PizzaOrderSystem store = new PizzaOrderSystem();

        System.out.println("=== 土办法：业务代码直接 new + if-else ===\n");

        System.out.println("👤 客人甲点了 cheese：");
        store.orderPizza("cheese");

        System.out.println("\n👤 客人乙点了 pepperoni：");
        store.orderPizza("pepperoni");

        System.out.println("\n👤 客人丙点了 veggie：");
        store.orderPizza("veggie");

        // ---- 问题暴露 ----
        System.out.println("\n⚠️  问题暴露：");
        System.out.println("1. orderPizza 方法里的 if-else 会越来越长");
        System.out.println("2. 加一种披萨 → 必须改 orderPizza（违反开闭原则）");
        System.out.println("3. 开纽约分店 / 芝加哥分店做不同风味 → 代码翻倍或复制粘贴");
        System.out.println("4. 业务流程 和 'new 哪种披萨' 这两个职责耦合在一起");

        System.out.println("\n👉 下一步：看 FactoryMethodDemo.java 怎么用工厂方法优雅解决");
    }
}

// ----------------------------------------------------------------
// 披萨抽象基类
// ----------------------------------------------------------------
abstract class Pizza {
    protected String name;

    public void prepare() {
        System.out.println("  🥖 准备面团 + 配料：" + name);
    }

    public void bake() {
        System.out.println("  🔥 烘焙 20 分钟：" + name);
    }

    public void cut() {
        System.out.println("  🔪 切成 8 块：" + name);
    }

    public void box() {
        System.out.println("  📦 装盒：" + name);
    }
}

// 具体披萨：芝士
class CheesePizza extends Pizza {
    public CheesePizza() { this.name = "芝士披萨"; }
}

// 具体披萨：辣香肠
class PepperoniPizza extends Pizza {
    public PepperoniPizza() { this.name = "辣香肠披萨"; }
}

// 具体披萨：素菜
class VeggiePizza extends Pizza {
    public VeggiePizza() { this.name = "素菜披萨"; }
}

// ----------------------------------------------------------------
// 披萨订单系统 —— 职责全混在一起的土办法
// ----------------------------------------------------------------
class PizzaOrderSystem {

    public Pizza orderPizza(String type) {
        Pizza pizza;

        // 🚨 if-else 满天飞：加新披萨要改这里
        if (type.equals("cheese")) {
            pizza = new CheesePizza();
        } else if (type.equals("pepperoni")) {
            pizza = new PepperoniPizza();
        } else if (type.equals("veggie")) {
            pizza = new VeggiePizza();
        } else {
            throw new IllegalArgumentException("未知披萨类型: " + type);
        }

        // 流程：准备 → 烘焙 → 切块 → 装盒
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        return pizza;
    }
}
