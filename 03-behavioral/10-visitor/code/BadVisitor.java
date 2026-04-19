/**
 * ============================================================
 * 🚫 不用访问者 · instanceof 判断类型
 * ============================================================
 *
 * 运行方式：
 *   java BadVisitor.java
 */

import java.util.List;

public class BadVisitor {

    public static void main(String[] args) {
        System.out.println("=== 土办法：instanceof 判断类型 ===\n");

        List<NaiveBusiness> businesses = List.of(
            new NaiveRestaurant(100_000),
            new NaiveSupermarket(500_000),
            new NaiveITCompany(2_000_000)
        );

        NaiveTaxInspector inspector = new NaiveTaxInspector();
        for (NaiveBusiness b : businesses) {
            inspector.inspect(b);
        }

        System.out.println("\n⚠️  问题：");
        System.out.println("1. inspect 里一堆 instanceof");
        System.out.println("2. 加新业态（房地产）→ 要改 inspect 方法");
        System.out.println("3. 加新访问者（卫生员）→ 又要写一遍 instanceof");
        System.out.println("4. 类型判断和业务逻辑混在一起");

        System.out.println("\n👉 看 VisitorDemo.java 如何用访问者模式优雅处理");
    }
}

// 元素（业态）
interface NaiveBusiness {
    double getRevenue();
}

class NaiveRestaurant implements NaiveBusiness {
    private final double revenue;
    NaiveRestaurant(double revenue) { this.revenue = revenue; }
    public double getRevenue() { return revenue; }
}

class NaiveSupermarket implements NaiveBusiness {
    private final double revenue;
    NaiveSupermarket(double revenue) { this.revenue = revenue; }
    public double getRevenue() { return revenue; }
}

class NaiveITCompany implements NaiveBusiness {
    private final double revenue;
    NaiveITCompany(double revenue) { this.revenue = revenue; }
    public double getRevenue() { return revenue; }
}

// 访问者（税务员）
class NaiveTaxInspector {
    public void inspect(NaiveBusiness b) {
        // 🚨 一堆 instanceof
        if (b instanceof NaiveRestaurant r) {
            System.out.println("🍜 餐饮税: " + (r.getRevenue() * 0.06));
        } else if (b instanceof NaiveSupermarket s) {
            System.out.println("🛒 零售税: " + (s.getRevenue() * 0.05));
        } else if (b instanceof NaiveITCompany c) {
            System.out.println("💻 科技税: " + (c.getRevenue() * 0.025));
        } else {
            System.out.println("❌ 未知业态");
        }
    }
}
