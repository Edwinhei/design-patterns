/**
 * ============================================================
 * 🚫 不用 Adapter 会发生什么 · 接口不兼容的痛点
 * ============================================================
 *
 * 场景：出差到欧洲，笔记本要充电
 *   - 笔记本提供"中国三脚插头"
 *   - 酒店插座只认"欧标两圆脚"
 *   - 两边都不能改（第三方 / 基础设施）
 *   - 没有 Adapter → 根本插不上！
 *
 * 运行方式：
 *   java BadAdapter.java
 */

public class BadAdapter {

    public static void main(String[] args) {
        System.out.println("=== 场景：在欧洲酒店给中国笔记本充电 ===\n");

        ChineseLaptop laptop = new ChineseLaptop();
        System.out.println("✅ 笔记本就绪：" + laptop.plugThreePinsCN());

        System.out.println("✅ 欧洲酒店插座就绪：只认两圆脚");

        // 尝试直接连接
        System.out.println("\n🔌 尝试直接插入...");
        // EuropeanSocket socket = laptop;    // ❌ 编译错误！类型不兼容
        // socket.provideTwoRoundPins();       // ❌ 笔记本根本没这方法

        System.out.println("❌ 失败：");
        System.out.println("   - 笔记本只提供 plugThreePinsCN()");
        System.out.println("   - 欧洲插座要求 provideTwoRoundPins()");
        System.out.println("   - 两者不是同一个接口，编译都过不去");

        System.out.println("\n⚠️  你不能：");
        System.out.println("   ① 修改笔记本（厂商不给你改）");
        System.out.println("   ② 修改欧洲插座（规范如此）");
        System.out.println("   ③ 换一台欧标笔记本（贵）");

        System.out.println("\n👉 解决方案：去看 AdapterDemo.java 如何用'转换器'模式适配");
    }
}

// ================================================================
// 欧洲插座接口（Target 要求的接口）
// ================================================================
interface EuropeanSocket {
    String provideTwoRoundPins();
}

// ================================================================
// 中国笔记本（已有的 Adaptee，不能改）
// ================================================================
class ChineseLaptop {
    // ⚠️ 注意：它没有实现 EuropeanSocket 接口
    public String plugThreePinsCN() {
        return "🔌 三脚中国插头（Type I）的笔记本";
    }
}
