/**
 * ============================================================
 * 🚫 不用桥接 · 类爆炸
 * ============================================================
 *
 * 场景：3 种遥控器 × 3 种电视 = 9 个类
 *
 * 运行方式：
 *   java BadBridge.java
 */

public class BadBridge {

    public static void main(String[] args) {
        System.out.println("=== 土办法：每种组合一个类 ===\n");

        new BasicRemoteForSony().turnOn();
        new AdvancedRemoteForSamsung().turnOn();
        new SmartRemoteForLG().turnOn();

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 当前 3 × 3 = 9 个类");
        System.out.println("2. 加一个【夏普】品牌 → 再加 3 个类（夏普 × 三种遥控器）");
        System.out.println("3. 加一个【游戏遥控】 → 再加 3 个类（游戏 × 三种电视）");
        System.out.println("4. 两种变化【乘法累积】");
        System.out.println("5. 类名越来越长（BasicRemoteForSony、SmartRemoteForSamsung...）");

        System.out.println("\n👉 看 BridgeDemo.java 如何用桥接优雅解决");
    }
}

// ================================================================
// 每种组合一个类 —— 一共 9 个（这里只列几个代表）
// ================================================================
class BasicRemoteForSony {
    public void turnOn() { System.out.println("🔘 [基础遥控 × 索尼] 开机"); }
}

class BasicRemoteForSamsung {
    public void turnOn() { System.out.println("🔘 [基础遥控 × 三星] 开机"); }
}

class BasicRemoteForLG {
    public void turnOn() { System.out.println("🔘 [基础遥控 × LG] 开机"); }
}

class AdvancedRemoteForSony {
    public void turnOn() { System.out.println("🎛 [高级遥控 × 索尼] 开机"); }
}

class AdvancedRemoteForSamsung {
    public void turnOn() { System.out.println("🎛 [高级遥控 × 三星] 开机"); }
}

class AdvancedRemoteForLG {
    public void turnOn() { System.out.println("🎛 [高级遥控 × LG] 开机"); }
}

class SmartRemoteForSony {
    public void turnOn() { System.out.println("📱 [智能遥控 × 索尼] 开机"); }
}

class SmartRemoteForSamsung {
    public void turnOn() { System.out.println("📱 [智能遥控 × 三星] 开机"); }
}

class SmartRemoteForLG {
    public void turnOn() { System.out.println("📱 [智能遥控 × LG] 开机"); }
}
