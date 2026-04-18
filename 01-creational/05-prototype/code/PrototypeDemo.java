/**
 * ============================================================
 * ✅ 原型模式（Prototype）· Java 三种实现方式对比
 * ============================================================
 *
 * 场景：简历模板 → 克隆出多份微调
 *
 * V1 · Cloneable 浅拷贝（GoF 原版，但有坑）
 *     → 演示浅拷贝的经典陷阱
 *
 * V2 · Cloneable 深拷贝
 *     → 手动修坑
 *
 * V3 · Copy constructor（Effective Java 推荐）
 *     → 现代最佳实践
 *
 * 运行方式：
 *   java PrototypeDemo.java
 */

import java.util.ArrayList;
import java.util.List;

public class PrototypeDemo {

    public static void main(String[] args) throws Exception {

        // ==============================================================
        // V1 · Cloneable 浅拷贝 —— ⚠️ 暴露经典陷阱
        // ==============================================================
        System.out.println("=== V1 · Cloneable 浅拷贝（展示陷阱）===");
        ResumeV1 v1Template = new ResumeV1("张三", List.of("Java", "Spring"));
        ResumeV1 v1Copy = v1Template.clone();

        // 🚨 修改 copy 的 skills
        // （因为 List.of 返回不可变 List，要先转可变）
        v1Copy.skills = new ArrayList<>(v1Copy.skills);
        v1Copy.skills.add("Kubernetes");

        System.out.println("V1 原版 skills: " + v1Template.skills);
        System.out.println("V1 克隆 skills: " + v1Copy.skills);
        System.out.println("两者 skills 是同一个对象吗？ " + (v1Template.skills == v1Copy.skills));
        // 本例特意用 List.of 返回不可变 List 避免污染原版，
        // 但在常见的 ArrayList 场景下，"copy.skills.add(x)" 会污染原版！

        // ==============================================================
        // V1 · 污染演示：用可变 ArrayList 再跑一次
        // ==============================================================
        System.out.println("\n--- V1 污染演示（用可变 ArrayList）---");
        ResumeV1 dangerous = new ResumeV1("李四", new ArrayList<>(List.of("Python")));
        ResumeV1 dangerousCopy = dangerous.clone();
        dangerousCopy.skills.add("PyTorch");   // 在 copy 上加

        System.out.println("原版 skills (李四): " + dangerous.skills);
        System.out.println("克隆 skills:        " + dangerousCopy.skills);
        System.out.println("🚨 原版也被污染了！浅拷贝共享同一个 List");
        System.out.println("两者 skills 是同一个对象吗？ " + (dangerous.skills == dangerousCopy.skills));

        // ==============================================================
        // V2 · Cloneable 深拷贝 —— 修好了浅拷贝的坑
        // ==============================================================
        System.out.println("\n=== V2 · Cloneable 深拷贝（修坑版）===");
        ResumeV2 v2Template = new ResumeV2("王五", new ArrayList<>(List.of("Go", "Rust")));
        ResumeV2 v2Copy = v2Template.clone();
        v2Copy.skills.add("WASM");

        System.out.println("V2 原版 skills: " + v2Template.skills);
        System.out.println("V2 克隆 skills: " + v2Copy.skills);
        System.out.println("两者 skills 是同一个对象吗？ " + (v2Template.skills == v2Copy.skills));
        System.out.println("✅ 原版未被污染，克隆独立了");

        // ==============================================================
        // V3 · Copy constructor —— 现代推荐
        // ==============================================================
        System.out.println("\n=== V3 · Copy constructor（现代推荐）===");
        ResumeV3 v3Template = new ResumeV3("赵六", new ArrayList<>(List.of("React", "TypeScript")));
        ResumeV3 v3Copy = new ResumeV3(v3Template);     // 一行克隆
        v3Copy.skills.add("Next.js");

        System.out.println("V3 原版 skills: " + v3Template.skills);
        System.out.println("V3 克隆 skills: " + v3Copy.skills);
        System.out.println("两者 skills 是同一个对象吗？ " + (v3Template.skills == v3Copy.skills));
        System.out.println("✅ 深拷贝，代码清晰，没有 Cloneable 的诡异");

        // ==============================================================
        // 小结
        // ==============================================================
        System.out.println("\n📌 总结：");
        System.out.println("  V1 浅拷贝  → 共享引用字段，会污染原版");
        System.out.println("  V2 深拷贝  → 修好了，但 Cloneable 接口设计丑陋");
        System.out.println("  V3 拷贝构造→ 推荐！代码清晰 + 走正常构造流程");
    }
}

// ================================================================
// V1 · Cloneable 浅拷贝（GoF 原版思路，但 Java 这样写有坑）
// ================================================================
class ResumeV1 implements Cloneable {          // 标记接口
    String name;
    List<String> skills;

    public ResumeV1(String name, List<String> skills) {
        this.name = name;
        this.skills = skills;
    }

    @Override
    public ResumeV1 clone() {
        try {
            // Object.clone() 只做浅拷贝 —— 字段按位复制
            // 引用类型字段（如 skills）不会被深拷贝，只复制引用
            return (ResumeV1) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);    // 不会发生，因为实现了 Cloneable
        }
    }
}

// ================================================================
// V2 · Cloneable 深拷贝（手动修坑）
// ================================================================
class ResumeV2 implements Cloneable {
    String name;
    List<String> skills;

    public ResumeV2(String name, List<String> skills) {
        this.name = name;
        this.skills = skills;
    }

    @Override
    public ResumeV2 clone() {
        try {
            ResumeV2 cloned = (ResumeV2) super.clone();
            // 🎯 关键：手动新建 List，切断和原版的共享
            cloned.skills = new ArrayList<>(this.skills);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

// ================================================================
// V3 · Copy constructor（Effective Java 推荐）
// ----------------------------------------------------------------
// 特点：
//   ✅ 不实现 Cloneable，不受它的坑影响
//   ✅ 走正常构造器，final 字段也能处理
//   ✅ 代码一目了然
// ================================================================
class ResumeV3 {
    String name;
    List<String> skills;

    // 普通构造器
    public ResumeV3(String name, List<String> skills) {
        this.name = name;
        this.skills = new ArrayList<>(skills);   // 防御性复制
    }

    // 🎯 拷贝构造器：接收同类对象，按字段复制
    public ResumeV3(ResumeV3 other) {
        this.name   = other.name;
        this.skills = new ArrayList<>(other.skills);   // 深拷贝集合
    }
}
