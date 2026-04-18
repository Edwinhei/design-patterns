/**
 * ============================================================
 * 🚫 不用装饰器 · 一堆布尔字段的痛
 * ============================================================
 *
 * 场景：星巴克咖啡，可以加多种配料
 *
 * 土办法：父类加一堆 boolean 字段 + 长长的 if 链
 *
 * 运行方式：
 *   java BadDecorator.java
 */

public class BadDecorator {

    public static void main(String[] args) {
        // 一杯美式 + 奶 + 焦糖
        Beverage drink1 = new Beverage();
        drink1.baseCost = 2.00;
        drink1.description = "美式";
        drink1.hasMilk = true;
        drink1.hasCaramel = true;
        System.out.println(drink1.describe() + " $" + drink1.cost());

        // 一杯拿铁 + 奶油
        Beverage drink2 = new Beverage();
        drink2.baseCost = 3.50;
        drink2.description = "拿铁";
        drink2.hasWhip = true;
        System.out.println(drink2.describe() + " $" + drink2.cost());

        System.out.println("\n⚠️  问题暴露：");
        System.out.println("1. Beverage 父类被一堆 boolean 字段污染");
        System.out.println("2. 加新配料（比如蜂蜜）→ 要改父类 + 改 cost 方法");
        System.out.println("3. 不能加【两份】同样的配料（boolean 只能 true/false）");
        System.out.println("4. cost() 方法越来越长，if 链臃肿");
        System.out.println("5. 违反开闭原则 —— 扩展要改父类代码");

        System.out.println("\n👉 看 DecoratorDemo.java 如何用装饰器优雅解决");
    }
}

// ================================================================
// 饮料类（所有逻辑都塞进来）
// ================================================================
class Beverage {
    String description;
    double baseCost;

    // 🚨 一堆 boolean 字段
    boolean hasMilk;
    boolean hasSugar;
    boolean hasWhip;
    boolean hasCaramel;

    public double cost() {
        double total = baseCost;
        // 🚨 if 链越来越长
        if (hasMilk)    total += 0.50;
        if (hasSugar)   total += 0.10;
        if (hasWhip)    total += 0.80;
        if (hasCaramel) total += 0.60;
        return total;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder(description);
        if (hasMilk)    sb.append(" + 奶");
        if (hasSugar)   sb.append(" + 糖");
        if (hasWhip)    sb.append(" + 奶油");
        if (hasCaramel) sb.append(" + 焦糖");
        return sb.toString();
    }
}
