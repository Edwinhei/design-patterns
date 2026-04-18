/**
 * ============================================================
 * 🚫 土办法示例：不用 Builder 时的两种反模式
 * ============================================================
 *
 * 场景：Subway 定制三明治
 *
 * 反模式 1：Telescoping Constructor（可伸缩构造器）
 *   → 一堆参数顺序极易出错、一堆重载构造器
 *
 * 反模式 2：JavaBean setter
 *   → 对象在构建过程中处于"不一致状态"，可能被别人拿去用
 *
 * 运行方式（需要 JDK 11+）：
 *   java BadSandwich.java
 */

public class BadSandwich {

    public static void main(String[] args) {

        // ================================================================
        // 反模式 1：Telescoping Constructor（超长构造器 + 一堆重载）
        // ================================================================
        System.out.println("=== 反模式 1：超长构造器 ===\n");

        // 🚨 痛点：参数顺序记不住，传错了编译器不会报错（都是 String）
        //         不用的参数也得传 null
        SandwichV1 s1 = new SandwichV1("全麦", "烤鸡", "切达", "生菜", null, null, "蛋黄酱", true);
        System.out.println(s1.describe());

        // 更扎心的：如果客人只想要面包 + 肉？
        SandwichV1 s2 = new SandwichV1("黑麦", "火腿", null, null, null, null, null, false);
        //                                    ↑    一堆 null，很丑
        System.out.println(s2.describe());

        System.out.println("\n⚠️  问题：");
        System.out.println("- 参数顺序如果传错了（都是 String），编译器发现不了");
        System.out.println("- 不用的字段要传 null，代码很丑");
        System.out.println("- 要支持更多组合？得重载一堆构造器");

        // ================================================================
        // 反模式 2：JavaBean setter 模式
        // ================================================================
        System.out.println("\n\n=== 反模式 2：JavaBean setter ===\n");

        SandwichV2 s3 = new SandwichV2();          // 🚨 此刻 s3 已是"完整对象"，但字段全是 null
        s3.setBread("全麦");
        useIt(s3);                                  // 🚨 此刻有人拿到"只有面包没有肉"的三明治

        s3.setMeat("烤鸡");
        s3.setCheese("切达");
        s3.setToasted(true);
        useIt(s3);                                  // 后来才完整，但对象还是可变

        System.out.println("\n⚠️  问题：");
        System.out.println("- 对象在构建过程中处于不一致状态，可能被误用");
        System.out.println("- 对象是可变的（任何人随时 setXxx），不适合多线程共享");
        System.out.println("- 没法声明'必选字段'（比如面包必选），setter 全是可选");

        System.out.println("\n👉 下一步：看 BuilderDemo.java 怎么用 Builder 模式优雅解决");
    }

    static void useIt(SandwichV2 s) {
        System.out.println("  [使用者拿到三明治]: " + s.describe());
    }
}

// ================================================================
// 反模式 1：超长构造器版三明治
// ================================================================
class SandwichV1 {
    private String bread;
    private String meat;
    private String cheese;
    private String vegetable1;
    private String vegetable2;
    private String vegetable3;
    private String sauce;
    private boolean toasted;

    // 🚨 8 个参数！传错顺序编译器发现不了
    public SandwichV1(String bread, String meat, String cheese,
                      String v1, String v2, String v3,
                      String sauce, boolean toasted) {
        this.bread = bread;
        this.meat = meat;
        this.cheese = cheese;
        this.vegetable1 = v1;
        this.vegetable2 = v2;
        this.vegetable3 = v3;
        this.sauce = sauce;
        this.toasted = toasted;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder("🥪 [" + bread + "]");
        if (meat      != null) sb.append(" + ").append(meat);
        if (cheese    != null) sb.append(" + ").append(cheese);
        if (vegetable1 != null) sb.append(" + ").append(vegetable1);
        if (vegetable2 != null) sb.append(" + ").append(vegetable2);
        if (vegetable3 != null) sb.append(" + ").append(vegetable3);
        if (sauce     != null) sb.append(" + ").append(sauce);
        if (toasted)           sb.append(" 🔥加热");
        return sb.toString();
    }
}

// ================================================================
// 反模式 2：JavaBean setter 版三明治
// ================================================================
class SandwichV2 {
    private String bread;
    private String meat;
    private String cheese;
    private String sauce;
    private boolean toasted;

    public SandwichV2() {}      // 🚨 无参构造：创建时对象就"存在"了，但啥都没

    public void setBread(String bread)    { this.bread = bread; }
    public void setMeat(String meat)      { this.meat = meat; }
    public void setCheese(String cheese)  { this.cheese = cheese; }
    public void setSauce(String sauce)    { this.sauce = sauce; }
    public void setToasted(boolean t)     { this.toasted = t; }

    public String describe() {
        StringBuilder sb = new StringBuilder("🥪 [" + (bread == null ? "无面包?!" : bread) + "]");
        if (meat   != null) sb.append(" + ").append(meat);
        if (cheese != null) sb.append(" + ").append(cheese);
        if (sauce  != null) sb.append(" + ").append(sauce);
        if (toasted)        sb.append(" 🔥加热");
        return sb.toString();
    }
}
