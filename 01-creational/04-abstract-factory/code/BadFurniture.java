/**
 * ============================================================
 * 🚫 土办法示例：不用抽象工厂会有"风格混搭"的风险
 * ============================================================
 *
 * 场景：装修新家，买家具
 *   理想情况：全屋风格一致
 *   土办法：客户自己 new 各种家具 —— 容易混搭
 *
 * 运行方式：
 *   java BadFurniture.java
 */

public class BadFurniture {

    public static void main(String[] args) {
        System.out.println("=== 土办法：客户自己 new 不同风格的家具 ===\n");

        // 小王装修：他想要北欧风格，但代码里各处 new，一不小心混搭了
        Sofa sofa   = new NordicSofa();
        Chair chair = new IndustrialChair();     // 🚨 搞错了！应该是 NordicChair
        Table table = new NordicTable();

        System.out.println("小王家客厅：");
        System.out.println("  沙发: " + sofa.describe());
        System.out.println("  椅子: " + chair.describe());   // ← 风格不对
        System.out.println("  桌子: " + table.describe());

        // ---- 问题暴露 ----
        System.out.println("\n⚠️  问题暴露：");
        System.out.println("1. 客户自己 new → 容易混搭风格");
        System.out.println("2. 要换整屋风格（比如改工业风）→ 所有 new 都要改");
        System.out.println("3. 加新风格（日式）→ 客户代码到处要改");
        System.out.println("4. 编译器无法帮你检查'整套家具是否同风格'");

        System.out.println("\n👉 下一步：去看 AbstractFactoryDemo.java 怎么保证产品族一致");
    }
}

// ================================================================
// 抽象产品
// ================================================================
interface Sofa  { String describe(); }
interface Chair { String describe(); }
interface Table { String describe(); }

// ================================================================
// 具体产品 · 北欧风
// ================================================================
class NordicSofa  implements Sofa  { public String describe() { return "🛋 北欧风沙发（浅木色 + 亚麻布）"; } }
class NordicChair implements Chair { public String describe() { return "🪑 北欧风椅子（白橡木）"; } }
class NordicTable implements Table { public String describe() { return "🪟 北欧风桌子（圆角、低调）"; } }

// ================================================================
// 具体产品 · 工业风
// ================================================================
class IndustrialSofa  implements Sofa  { public String describe() { return "🛋 工业风沙发（深色皮革 + 金属框）"; } }
class IndustrialChair implements Chair { public String describe() { return "🪑 工业风椅子（黑色铁艺）"; } }
class IndustrialTable implements Table { public String describe() { return "🪟 工业风桌子（粗犷木纹 + 铁脚）"; } }
