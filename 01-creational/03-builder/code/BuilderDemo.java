/**
 * ============================================================
 * ✅ 建造者模式（现代版 · Effective Java 推荐）
 * ============================================================
 *
 * 场景：Subway 定制三明治
 *   - 必选：面包
 *   - 可选：肉、奶酪、蔬菜（多选）、酱料、是否加热
 *
 * 核心要点：
 *   1. 目标类 SubwaySandwich 所有字段 final（不可变）
 *   2. 构造器 private（外部不能直接 new）
 *   3. 静态内部类 Builder 负责收集配置
 *   4. Builder 的每个方法返回 this，支持链式调用
 *   5. 最后 build() 一次性创建完整对象
 *
 * 运行方式（需要 JDK 11+）：
 *   java BuilderDemo.java
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BuilderDemo {

    public static void main(String[] args) {

        // ===== 场景 1：经典鸡肉三明治（大部分配料都要）=====
        System.out.println("=== 客人甲：豪华鸡肉三明治 ===");
        SubwaySandwich s1 = SubwaySandwich.builder("全麦面包")
                .meat("烤鸡")
                .cheese("切达奶酪")
                .addVegetable("生菜")
                .addVegetable("番茄")
                .addVegetable("黄瓜")
                .sauce("蛋黄酱")
                .toasted(true)
                .build();
        System.out.println(s1.describe());

        // ===== 场景 2：素食（没有肉，没有奶酪）=====
        System.out.println("\n=== 客人乙：素食清淡 ===");
        SubwaySandwich s2 = SubwaySandwich.builder("黑麦面包")
                .addVegetable("菠菜")
                .addVegetable("青椒")
                .sauce("油醋")
                .build();
        System.out.println(s2.describe());

        // ===== 场景 3：最简 —— 只有面包 =====
        System.out.println("\n=== 客人丙：只要面包（节食）===");
        SubwaySandwich s3 = SubwaySandwich.builder("全麦面包").build();
        System.out.println(s3.describe());

        // ===== 场景 4：对比普通构造器调用（给你感受差异）=====
        System.out.println("\n=== 对比：如果用普通构造器 ===");
        System.out.println("  new Sandwich(\"全麦\", \"烤鸡\", \"切达\", \"生菜\", null, null, \"蛋黄酱\", true)");
        System.out.println("  ↑ 一堆 null，顺序传错编译器检测不了");
        System.out.println("");
        System.out.println("  vs");
        System.out.println("");
        System.out.println("  SubwaySandwich.builder(\"全麦\").meat(\"烤鸡\").cheese(\"切达\")...");
        System.out.println("  ↑ 清晰、可选、不会搞错");

        // ===== 场景 5：必选参数漏传 → 编译错误或运行时错误 =====
        System.out.println("\n=== 场景 5：必选参数保护 ===");
        try {
            // 🚨 面包传 null，builder 会立刻抛异常
            SubwaySandwich.builder(null).build();
        } catch (NullPointerException e) {
            System.out.println("  ✅ 正确阻止：" + e.getMessage());
        }
    }
}

// ================================================================
// 目标类：SubwaySandwich（不可变对象）
// ================================================================
class SubwaySandwich {

    // 🔒 所有字段 final：对象一旦构造完成，状态不可变
    private final String bread;
    private final String meat;
    private final String cheese;
    private final List<String> vegetables;
    private final String sauce;
    private final boolean toasted;

    // 🔒 构造器私有：外部不能直接 new，只能通过 Builder
    private SubwaySandwich(Builder b) {
        this.bread      = b.bread;
        this.meat       = b.meat;
        this.cheese     = b.cheese;
        this.vegetables = List.copyOf(b.vegetables);   // 防御性复制：防止外部通过 builder.vegetables 修改
        this.sauce      = b.sauce;
        this.toasted    = b.toasted;
    }

    // 🚪 全局入口：通过 builder() 开始构建
    public static Builder builder(String bread) {
        return new Builder(bread);
    }

    public String describe() {
        StringBuilder sb = new StringBuilder("🥪 [" + bread + "]");
        if (meat   != null) sb.append(" + 🍗").append(meat);
        if (cheese != null) sb.append(" + 🧀").append(cheese);
        if (!vegetables.isEmpty()) sb.append(" + 🥬").append(String.join("/", vegetables));
        if (sauce  != null) sb.append(" + 🥫").append(sauce);
        if (toasted) sb.append(" 🔥加热");
        return sb.toString();
    }

    // ================================================================
    // 🏗 静态内部类 Builder —— 本课的主角
    // ================================================================
    public static class Builder {

        // Builder 字段：和目标类对应，但都不是 final（构建期间可修改）
        private final String bread;                            // 必选：构造时传入
        private String meat;
        private String cheese;
        private List<String> vegetables = new ArrayList<>();
        private String sauce;
        private boolean toasted = false;

        // Builder 构造器：必选字段放这
        private Builder(String bread) {
            // 必选字段校验
            this.bread = Objects.requireNonNull(bread, "面包是必选项，不能为 null");
        }

        // 🔗 链式 API：每个方法返回 this
        public Builder meat(String meat) {
            this.meat = meat;
            return this;
        }

        public Builder cheese(String cheese) {
            this.cheese = cheese;
            return this;
        }

        public Builder addVegetable(String vegetable) {
            this.vegetables.add(vegetable);                   // 多次调用会累加
            return this;
        }

        public Builder sauce(String sauce) {
            this.sauce = sauce;
            return this;
        }

        public Builder toasted(boolean toasted) {
            this.toasted = toasted;
            return this;
        }

        // 🎁 一次性创建目标对象
        public SubwaySandwich build() {
            // 可以在这里做跨字段的整体校验
            // 例如：toasted=true 但 bread 是不支持加热的品种 → 报错
            return new SubwaySandwich(this);
        }
    }
}
