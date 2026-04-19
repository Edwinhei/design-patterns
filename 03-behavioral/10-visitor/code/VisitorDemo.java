/**
 * ============================================================
 * ✅ 访问者模式 · 税务员 + 卫生员 查不同业态
 * ============================================================
 *
 * 核心：双重分派
 *   b.accept(visitor)      → 第 1 次分派，按元素类型
 *   visitor.visit(this)    → 第 2 次分派，按访问者类型
 *
 * 运行方式：
 *   java VisitorDemo.java
 */

import java.util.List;

public class VisitorDemo {

    public static void main(String[] args) {
        // 三种业态
        List<Business> businesses = List.of(
            new Restaurant(100_000),
            new Supermarket(500_000),
            new ITCompany(2_000_000)
        );

        // === 访问者 1：税务员 ===
        System.out.println("===== 税务员上门查税 =====");
        Visitor taxInspector = new TaxInspector();
        for (Business b : businesses) {
            b.accept(taxInspector);   // 双重分派
        }

        // === 访问者 2：卫生员（同样的元素结构，不同的访问者）===
        System.out.println("\n===== 卫生员上门检查 =====");
        Visitor healthInspector = new HealthInspector();
        for (Business b : businesses) {
            b.accept(healthInspector);
        }

        System.out.println("\n✨ 访问者模式的威力：");
        System.out.println("  ① 业态类（Restaurant 等）的代码完全不动");
        System.out.println("  ② 加新访问者（审计员）→ 加一个类，元素不改");
        System.out.println("  ③ 双重分派自动路由到正确方法（无需 instanceof）");
        System.out.println("  ④ 一个对象结构，多种访问方式（税/卫生/审计/统计）");
    }
}

// ================================================================
// 访问者接口（对每种元素有一个 visit 方法）
// ================================================================
interface Visitor {
    void visit(Restaurant r);
    void visit(Supermarket s);
    void visit(ITCompany c);
}

// ================================================================
// 元素接口（所有业态都 accept 访问者）
// ================================================================
interface Business {
    void accept(Visitor visitor);
}

// ================================================================
// 具体元素
// ================================================================
class Restaurant implements Business {
    private final double revenue;

    public Restaurant(double revenue) { this.revenue = revenue; }

    public void accept(Visitor visitor) {
        visitor.visit(this);       // 🎯 双重分派：传递 this（类型 Restaurant）
    }

    public double getRevenue()    { return revenue; }
}

class Supermarket implements Business {
    private final double revenue;

    public Supermarket(double revenue) { this.revenue = revenue; }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public double getRevenue()    { return revenue; }
}

class ITCompany implements Business {
    private final double revenue;

    public ITCompany(double revenue) { this.revenue = revenue; }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public double getRevenue()    { return revenue; }
}

// ================================================================
// 具体访问者 1：税务员
// ================================================================
class TaxInspector implements Visitor {
    public void visit(Restaurant r) {
        System.out.printf("  🍜 餐饮业: 营收 %.0f，税率 6%%，应缴 %.0f%n",
                r.getRevenue(), r.getRevenue() * 0.06);
    }

    public void visit(Supermarket s) {
        System.out.printf("  🛒 零售业: 营收 %.0f，税率 5%%，应缴 %.0f%n",
                s.getRevenue(), s.getRevenue() * 0.05);
    }

    public void visit(ITCompany c) {
        System.out.printf("  💻 科技企业: 营收 %.0f，优惠税率 2.5%%，应缴 %.0f%n",
                c.getRevenue(), c.getRevenue() * 0.025);
    }
}

// ================================================================
// 具体访问者 2：卫生员（复用同样的元素结构）
// ================================================================
class HealthInspector implements Visitor {
    public void visit(Restaurant r) {
        System.out.println("  🍜 餐饮业: 检查厨房卫生、食材保质期");
    }

    public void visit(Supermarket s) {
        System.out.println("  🛒 超市: 检查食品货架温度、过期商品");
    }

    public void visit(ITCompany c) {
        System.out.println("  💻 科技企业: 检查办公区清洁（不涉及食品）");
    }
}
