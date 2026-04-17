/**
 * ============================================================
 * ✅ 工厂方法模式（Factory Method）正解
 * ============================================================
 *
 * 场景：披萨连锁店
 *   - 纽约店做纽约风（薄饼）
 *   - 芝加哥店做芝加哥风（厚底深盘）
 *   - 业务流程（准备→烘焙→切块→装盒）两家完全一样
 *
 * 关键思想：
 *   - 父类 PizzaStore 定义流程（模板方法 orderPizza）
 *   - 抽象方法 createPizza 由子类实现 → "造哪种"交给子类
 *   - 加新分店 = 加新子类，不改父类
 *
 * 运行方式（需要 JDK 11+）：
 *   java FactoryMethodDemo.java
 */

// ----------------------------------------------------------------
// 主程序
// ----------------------------------------------------------------
public class FactoryMethodDemo {

    public static void main(String[] args) {
        // 同一个下单流程，两家不同的店 → 做出不同风味的披萨
        PizzaStore nyStore = new NewYorkPizzaStore();
        PizzaStore chStore = new ChicagoPizzaStore();

        System.out.println("===== 纽约店接到订单 =====");
        nyStore.orderPizza("cheese");

        System.out.println("\n===== 芝加哥店接到订单 =====");
        chStore.orderPizza("cheese");

        System.out.println("\n✅ 注意：");
        System.out.println("   - 两家店的 orderPizza 流程完全一样（prepare→bake→cut→box）");
        System.out.println("   - 但 createPizza 是各自子类实现 → 做出不同风味");
        System.out.println("   - 要开加州分店？加一个 CaliforniaPizzaStore 子类即可，PizzaStore 父类不动");
    }
}

// ================================================================
// 披萨抽象基类
// ----------------------------------------------------------------
// 所有披萨的共同行为
// ================================================================
abstract class Pizza {
    protected String name;
    protected String dough;    // 面团类型（薄饼/厚底）
    protected String sauce;    // 酱料

    public void prepare() {
        System.out.println("  🥖 准备披萨: " + name);
        System.out.println("     面团: " + dough);
        System.out.println("     酱料: " + sauce);
    }

    public void bake()  { System.out.println("  🔥 烘焙: " + name); }
    public void cut()   { System.out.println("  🔪 切块: " + name); }
    public void box()   { System.out.println("  📦 装盒: " + name); }
}

// ================================================================
// 纽约风 · 具体披萨
// ----------------------------------------------------------------
// 特点：薄饼 + 番茄酱（简单、经典）
// ================================================================
class NYCheesePizza extends Pizza {
    public NYCheesePizza() {
        name  = "纽约风芝士披萨";
        dough = "薄饼面团";
        sauce = "番茄酱";
    }
}

// ================================================================
// 芝加哥风 · 具体披萨
// ----------------------------------------------------------------
// 特点：厚底 + 番茄膏（深盘）
// ================================================================
class ChicagoCheesePizza extends Pizza {
    public ChicagoCheesePizza() {
        name  = "芝加哥风芝士披萨";
        dough = "厚底深盘面团";
        sauce = "番茄膏";
    }

    // 芝加哥风习惯切方块，重写 cut
    @Override
    public void cut() {
        System.out.println("  🔪 切成方块: " + name);
    }
}

// ================================================================
// 披萨店抽象类 —— 工厂方法的核心
// ================================================================
abstract class PizzaStore {

    /**
     * 🎯 模板方法：固定下单流程（所有分店通用）
     *
     * final 关键字防止子类修改流程。
     * 子类只能决定"造哪种披萨"，不能改"怎么下单"。
     */
    public final Pizza orderPizza(String type) {
        Pizza pizza = createPizza(type);  // 👈 关键：这一步由子类决定造什么
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        return pizza;
    }

    /**
     * 🏭 抽象工厂方法：具体造哪种披萨，由子类说了算
     *
     * 这是"工厂方法模式"的灵魂：
     *   把"创建对象"这件事抽象化，推迟到子类决定。
     */
    protected abstract Pizza createPizza(String type);
}

// ================================================================
// 纽约分店 —— 具体的工厂
// ================================================================
class NewYorkPizzaStore extends PizzaStore {
    @Override
    protected Pizza createPizza(String type) {
        if (type.equals("cheese")) {
            return new NYCheesePizza();
        }
        // 其他类型按同样模式扩展: NYPepperoniPizza, NYVeggiePizza...
        throw new IllegalArgumentException("纽约店不供应: " + type);
    }
}

// ================================================================
// 芝加哥分店 —— 具体的工厂
// ================================================================
class ChicagoPizzaStore extends PizzaStore {
    @Override
    protected Pizza createPizza(String type) {
        if (type.equals("cheese")) {
            return new ChicagoCheesePizza();
        }
        throw new IllegalArgumentException("芝加哥店不供应: " + type);
    }
}
