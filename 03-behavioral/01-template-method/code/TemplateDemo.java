/**
 * ============================================================
 * ✅ 模板方法 Template Method · 饮料制作
 * ============================================================
 *
 * 三个角色：
 *   ① 模板方法 prepare() —— 流程骨架（final）
 *   ② 抽象方法 brew / addCondiments —— 子类必须实现
 *   ③ 钩子方法 customerWantsCondiments —— 子类可选覆盖
 *
 * 运行方式：
 *   java TemplateDemo.java
 */

public class TemplateDemo {

    public static void main(String[] args) {
        System.out.println("===== 普通咖啡 =====");
        new Coffee().prepare();

        System.out.println("\n===== 普通茶 =====");
        new Tea().prepare();

        System.out.println("\n===== 不要加料的黑咖啡（钩子起作用）=====");
        new BlackCoffee().prepare();

        System.out.println("\n✨ 模板方法的威力：");
        System.out.println("  ① 父类定义流程骨架（prepare 是 final）");
        System.out.println("  ② 共同步骤（烧水/倒杯）只写一次");
        System.out.println("  ③ 不同步骤（brew/addCondiments）由子类实现");
        System.out.println("  ④ 钩子方法让子类影响流程（要不要加料）");
        System.out.println("  ⑤ 加新饮料只写子类，父类一行不改");
    }
}

// ================================================================
// 抽象基类：饮料
// ================================================================
abstract class Beverage {

    // 🎯 模板方法：定义制作流程
    // final 关键字 → 子类不能改流程顺序
    public final void prepare() {
        boilWater();

        brew();                                 // 抽象：子类实现

        pourInCup();

        if (customerWantsCondiments()) {        // 🪝 钩子方法
            addCondiments();                    // 抽象：子类实现
        }
    }

    // 共同步骤（父类实现）
    private void boilWater() {
        System.out.println("🔥 烧水");
    }

    private void pourInCup() {
        System.out.println("🫗 倒入杯中");
    }

    // 抽象方法：子类必须实现
    protected abstract void brew();
    protected abstract void addCondiments();

    // 🪝 钩子方法：子类可选覆盖（有默认实现）
    protected boolean customerWantsCondiments() {
        return true;          // 默认：要加料
    }
}

// ================================================================
// 具体子类：咖啡
// ================================================================
class Coffee extends Beverage {
    @Override
    protected void brew() {
        System.out.println("☕ 冲咖啡粉");
    }

    @Override
    protected void addCondiments() {
        System.out.println("🥛 加糖和奶");
    }
}

// ================================================================
// 具体子类：茶
// ================================================================
class Tea extends Beverage {
    @Override
    protected void brew() {
        System.out.println("🍵 泡茶叶");
    }

    @Override
    protected void addCondiments() {
        System.out.println("🍋 加柠檬");
    }
}

// ================================================================
// 黑咖啡：覆盖钩子方法，不要加料
// ================================================================
class BlackCoffee extends Coffee {

    @Override
    protected boolean customerWantsCondiments() {
        return false;          // 覆盖钩子：不加料
    }
}
