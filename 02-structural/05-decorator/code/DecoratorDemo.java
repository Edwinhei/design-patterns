/**
 * ============================================================
 * ✅ 装饰器模式 Decorator · 星巴克咖啡场景
 * ============================================================
 *
 * 设计：
 *   - Beverage 抽象类（组件）
 *   - Espresso / Latte（具体饮料）
 *   - CondimentDecorator 装饰器基类（也是 Beverage）
 *   - Milk / Sugar / Whip / Caramel（具体装饰器）
 *
 * 精髓：装饰器【也是 Beverage】 + 【持有一个 Beverage】
 *       → 一层层包装
 *
 * 运行方式：
 *   java DecoratorDemo.java
 */

public class DecoratorDemo {

    public static void main(String[] args) {
        // === 场景 1：美式（无加料）===
        Beverage drink1 = new Espresso();
        printOrder(drink1);

        // === 场景 2：美式 + 奶 ===
        Beverage drink2 = new Espresso();
        drink2 = new Milk(drink2);
        printOrder(drink2);

        // === 场景 3：拿铁 + 奶油 + 焦糖 ===
        Beverage drink3 = new Latte();
        drink3 = new Whip(drink3);
        drink3 = new Caramel(drink3);
        printOrder(drink3);

        // === 场景 4：美式 + 两份焦糖（优势展示）===
        Beverage drink4 = new Espresso();
        drink4 = new Caramel(drink4);
        drink4 = new Caramel(drink4);       // 可以加两次！boolean 做不到
        printOrder(drink4);

        // === 场景 5：全配料 ===
        Beverage drink5 = new Latte();
        drink5 = new Milk(drink5);
        drink5 = new Sugar(drink5);
        drink5 = new Whip(drink5);
        drink5 = new Caramel(drink5);
        printOrder(drink5);

        System.out.println("\n✨ 装饰器的威力：");
        System.out.println("  ① 一层一层包装，描述/价格自动叠加");
        System.out.println("  ② 可以加【两次】同一种配料");
        System.out.println("  ③ 加新配料只需新增一个装饰器类，Beverage 不用动");
        System.out.println("  ④ 客户端自由组合，任意顺序");
    }

    static void printOrder(Beverage beverage) {
        System.out.printf("  %-40s $%.2f%n", beverage.getDescription(), beverage.cost());
    }
}

// ================================================================
// 组件抽象类
// ================================================================
abstract class Beverage {
    public abstract String getDescription();
    public abstract double cost();
}

// ================================================================
// 具体饮料：美式
// ================================================================
class Espresso extends Beverage {
    public String getDescription() { return "美式"; }
    public double cost() { return 2.00; }
}

// ================================================================
// 具体饮料：拿铁
// ================================================================
class Latte extends Beverage {
    public String getDescription() { return "拿铁"; }
    public double cost() { return 3.50; }
}

// ================================================================
// 装饰器基类
// ----------------------------------------------------------------
// 关键：
//   1. 继承自 Beverage → 装饰器本身也是 Beverage
//   2. 持有一个 Beverage → 可以包装任意 Beverage
//   3. 二者结合 → 可以一层层堆叠
// ================================================================
abstract class CondimentDecorator extends Beverage {
    protected final Beverage beverage;       // 被装饰的饮料

    public CondimentDecorator(Beverage beverage) {
        this.beverage = beverage;
    }
}

// ================================================================
// 具体装饰器：奶（+$0.50）
// ================================================================
class Milk extends CondimentDecorator {
    public Milk(Beverage beverage) { super(beverage); }

    public String getDescription() {
        return beverage.getDescription() + " + 奶";       // 委托 + 叠加
    }

    public double cost() {
        return beverage.cost() + 0.50;
    }
}

// ================================================================
// 具体装饰器：糖（+$0.10）
// ================================================================
class Sugar extends CondimentDecorator {
    public Sugar(Beverage beverage) { super(beverage); }

    public String getDescription() {
        return beverage.getDescription() + " + 糖";
    }

    public double cost() {
        return beverage.cost() + 0.10;
    }
}

// ================================================================
// 具体装饰器：奶油（+$0.80）
// ================================================================
class Whip extends CondimentDecorator {
    public Whip(Beverage beverage) { super(beverage); }

    public String getDescription() {
        return beverage.getDescription() + " + 奶油";
    }

    public double cost() {
        return beverage.cost() + 0.80;
    }
}

// ================================================================
// 具体装饰器：焦糖（+$0.60）
// ================================================================
class Caramel extends CondimentDecorator {
    public Caramel(Beverage beverage) { super(beverage); }

    public String getDescription() {
        return beverage.getDescription() + " + 焦糖";
    }

    public double cost() {
        return beverage.cost() + 0.60;
    }
}
