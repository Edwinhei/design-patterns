/**
 * ============================================================
 * ✅ 抽象工厂模式 Abstract Factory
 * ============================================================
 *
 * 场景：宜家家具，整套搭配
 *   - 北欧风工厂：造北欧沙发 + 北欧椅子 + 北欧桌子
 *   - 工业风工厂：造工业沙发 + 工业椅子 + 工业桌子
 *   - 切换风格 = 切换工厂，整套家具自动一致
 *
 * 核心设计：
 *   1. 抽象工厂 FurnitureFactory：声明造什么族
 *   2. 具体工厂：实现抽象工厂，造一族同风格产品
 *   3. 抽象产品：Sofa / Chair / Table
 *   4. 具体产品：每种风格的具体实现
 *   5. 客户 HomeDecorator：只依赖抽象工厂，不关心风格
 *
 * 运行方式：
 *   java AbstractFactoryDemo.java
 */

public class AbstractFactoryDemo {

    public static void main(String[] args) {
        // === 场景 1：选择北欧风工厂 ===
        System.out.println("===== 小王家：选择【北欧风】 =====");
        HomeDecorator wang = new HomeDecorator(new NordicFurnitureFactory());
        wang.decorate();

        System.out.println();

        // === 场景 2：选择工业风工厂 ===
        System.out.println("===== 老李家：选择【工业风】 =====");
        HomeDecorator li = new HomeDecorator(new IndustrialFurnitureFactory());
        li.decorate();

        System.out.println();

        // === 场景 3：运行时动态切换（根据配置/用户输入）===
        System.out.println("===== 小张家：运行时决定 =====");
        String userChoice = "nordic";     // 假设从配置或用户输入来
        FurnitureFactory factory = chooseFactory(userChoice);
        new HomeDecorator(factory).decorate();

        System.out.println("\n✅ 亮点：");
        System.out.println("  - HomeDecorator 代码完全不用动，只换工厂参数");
        System.out.println("  - 同一家的沙发+椅子+桌子一定风格一致（由工厂保证）");
        System.out.println("  - 加日式风？再加一个 JapaneseFurnitureFactory 即可，");
        System.out.println("    HomeDecorator 和已有工厂代码都不用改");
    }

    // 根据输入选择工厂（简单的工厂选择逻辑）
    private static FurnitureFactory chooseFactory(String style) {
        return switch (style) {
            case "nordic"     -> new NordicFurnitureFactory();
            case "industrial" -> new IndustrialFurnitureFactory();
            default -> throw new IllegalArgumentException("未知风格: " + style);
        };
    }
}

// ================================================================
// 抽象产品
// ================================================================
interface Sofa  { String describe(); }
interface Chair { String describe(); }
interface Table { String describe(); }

// ================================================================
// 具体产品 · 北欧风（一族）
// ================================================================
class NordicSofa  implements Sofa  { public String describe() { return "🛋 北欧风沙发（浅木色 + 亚麻布）"; } }
class NordicChair implements Chair { public String describe() { return "🪑 北欧风椅子（白橡木）"; } }
class NordicTable implements Table { public String describe() { return "🪟 北欧风桌子（圆角、低调）"; } }

// ================================================================
// 具体产品 · 工业风（一族）
// ================================================================
class IndustrialSofa  implements Sofa  { public String describe() { return "🛋 工业风沙发（深色皮革 + 金属框）"; } }
class IndustrialChair implements Chair { public String describe() { return "🪑 工业风椅子（黑色铁艺）"; } }
class IndustrialTable implements Table { public String describe() { return "🪟 工业风桌子（粗犷木纹 + 铁脚）"; } }

// ================================================================
// 抽象工厂：定义能造哪些家具（一整族）
// ================================================================
interface FurnitureFactory {
    Sofa  createSofa();
    Chair createChair();
    Table createTable();
}

// ================================================================
// 具体工厂 · 北欧风 —— 保证造出来都是北欧风
// ================================================================
class NordicFurnitureFactory implements FurnitureFactory {
    public Sofa  createSofa()  { return new NordicSofa(); }
    public Chair createChair() { return new NordicChair(); }
    public Table createTable() { return new NordicTable(); }
}

// ================================================================
// 具体工厂 · 工业风 —— 保证造出来都是工业风
// ================================================================
class IndustrialFurnitureFactory implements FurnitureFactory {
    public Sofa  createSofa()  { return new IndustrialSofa(); }
    public Chair createChair() { return new IndustrialChair(); }
    public Table createTable() { return new IndustrialTable(); }
}

// ================================================================
// 客户代码：HomeDecorator（装修师）
// ----------------------------------------------------------------
// 关键：它只依赖抽象 FurnitureFactory，不知道具体是哪个工厂
//       → 不论哪种风格，装修流程代码完全相同
// ================================================================
class HomeDecorator {
    private final FurnitureFactory factory;

    public HomeDecorator(FurnitureFactory factory) {
        this.factory = factory;
    }

    public void decorate() {
        // 用工厂造一整套家具（保证同一族，风格一致）
        Sofa  sofa  = factory.createSofa();
        Chair chair = factory.createChair();
        Table table = factory.createTable();

        System.out.println("  客厅:");
        System.out.println("    " + sofa.describe());
        System.out.println("    " + chair.describe());
        System.out.println("    " + table.describe());
    }
}
