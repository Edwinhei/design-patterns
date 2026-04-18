/**
 * ============================================================
 * ✅ 适配器模式 Adapter · 对象适配器（推荐方式）
 * ============================================================
 *
 * 场景：欧洲插座 + 中国笔记本，用"插头转换器"搞定
 *
 * 三个角色：
 *   Target    → EuropeanSocket（客户端期望的接口）
 *   Adaptee   → ChineseLaptop（已有的不兼容类）
 *   Adapter   → PowerAdapter（把 Laptop 包装成 Socket）
 *
 * 运行方式：
 *   java AdapterDemo.java
 */

public class AdapterDemo {

    public static void main(String[] args) {

        // === 场景 1：使用对象适配器 ===
        System.out.println("=== 使用对象适配器（推荐）===");

        ChineseLaptop laptop = new ChineseLaptop();

        // 关键：把 Laptop 包进 Adapter，对外是 EuropeanSocket
        EuropeanSocket socket = new PowerAdapter(laptop);

        // 客户端代码只和 EuropeanSocket 打交道，不知道 ChineseLaptop 存在
        String output = socket.provideTwoRoundPins();
        System.out.println("结果: " + output);

        // === 场景 2：客户端函数只接受 EuropeanSocket ===
        System.out.println("\n=== 客户端函数调用演示 ===");
        useSocket(socket);     // 直接传 Adapter，它"就是"一个 EuropeanSocket

        // === 场景 3：演示另一种被适配对象 ===
        System.out.println("\n=== Adapter 可以复用于不同笔记本 ===");
        ChineseLaptop laptop2 = new ChineseLaptop("外星人游戏本");
        EuropeanSocket socket2 = new PowerAdapter(laptop2);
        useSocket(socket2);

        // === 原理小结 ===
        System.out.println("\n✅ Adapter 模式做的三件事：");
        System.out.println("   ① implements 目标接口（客户端以为它是'插座'）");
        System.out.println("   ② 持有被适配对象（内部藏着 ChineseLaptop）");
        System.out.println("   ③ 翻译调用（把 provideTwoRoundPins → plugThreePinsCN）");

        System.out.println("\n🎯 客户端代码完全不知道 ChineseLaptop 的存在！");
    }

    /**
     * 客户端函数：只认 EuropeanSocket 接口
     * 完全不关心对方是"真插座"还是"适配器"
     */
    static void useSocket(EuropeanSocket socket) {
        System.out.println("  [客户端] 我拿到一个 EuropeanSocket: " + socket.provideTwoRoundPins());
    }
}

// ================================================================
// Target 接口：欧洲插座
// ================================================================
interface EuropeanSocket {
    String provideTwoRoundPins();
}

// ================================================================
// Adaptee：中国笔记本（已有的类，不能修改）
// ================================================================
class ChineseLaptop {
    private final String model;

    public ChineseLaptop() {
        this.model = "Thinkpad";
    }

    public ChineseLaptop(String model) {
        this.model = model;
    }

    public String plugThreePinsCN() {
        return "🔌 " + model + " 的三脚中国插头（Type I）";
    }
}

// ================================================================
// Adapter：插头转换器（对象适配器）
// ----------------------------------------------------------------
// 三个要点：
//   ① implements EuropeanSocket  → 对外伪装成欧洲插座
//   ② private final ChineseLaptop laptop  → 持有被适配对象
//   ③ provideTwoRoundPins 内部调用 laptop.plugThreePinsCN()
// ================================================================
class PowerAdapter implements EuropeanSocket {

    private final ChineseLaptop laptop;    // 组合关系：has-a

    public PowerAdapter(ChineseLaptop laptop) {
        this.laptop = laptop;
    }

    @Override
    public String provideTwoRoundPins() {
        // 委托给被适配对象 + 做一些转换工作
        String originalOutput = laptop.plugThreePinsCN();
        return "🔄 [适配器转换中] " + originalOutput + " → 🔌 输出成两圆脚（Type C）";
    }
}
