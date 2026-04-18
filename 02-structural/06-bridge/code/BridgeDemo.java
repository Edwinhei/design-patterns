/**
 * ============================================================
 * ✅ 桥接模式 Bridge · 遥控器 × 电视
 * ============================================================
 *
 * 两个独立变化的维度：
 *   - 抽象维度：遥控器（基础 / 高级 / 智能）
 *   - 实现维度：电视（索尼 / 三星 / LG）
 *
 * 通过"组合"连接，避免类爆炸（M + N 代替 M × N）。
 *
 * 运行方式：
 *   java BridgeDemo.java
 */

public class BridgeDemo {

    public static void main(String[] args) {
        // 创建电视（实现维度）
        TV sony    = new SonyTV();
        TV samsung = new SamsungTV();
        TV lg      = new LGTV();

        // 创建遥控器（抽象维度），通过组合连接电视
        System.out.println("===== 基础遥控 × 不同电视 =====");
        BasicRemote basic1 = new BasicRemote(sony);
        basic1.turnOn();
        basic1.setChannel(5);
        basic1.turnOff();

        BasicRemote basic2 = new BasicRemote(samsung);
        basic2.turnOn();

        System.out.println("\n===== 高级遥控 × 不同电视 =====");
        AdvancedRemote adv = new AdvancedRemote(lg);
        adv.turnOn();
        adv.setChannel(12);
        adv.mute();              // 高级遥控特有功能
        adv.turnOff();

        System.out.println("\n===== 智能遥控 × 索尼 =====");
        SmartRemote smart = new SmartRemote(sony);
        smart.turnOn();
        smart.voiceCommand("切到 体育频道");
        smart.turnOff();

        System.out.println("\n✨ 桥接的威力：");
        System.out.println("  ① 3 种遥控器 + 3 种电视 = 3 + 3 = 6 个类（不是 9 个）");
        System.out.println("  ② 加新电视品牌 → 加 1 个类（只在电视这条线）");
        System.out.println("  ③ 加新遥控类型 → 加 1 个类（只在遥控这条线）");
        System.out.println("  ④ 两个维度【各自独立演化】");
    }
}

// ================================================================
// 实现维度：电视接口
// ================================================================
interface TV {
    void on();
    void off();
    void setChannel(int channel);
}

class SonyTV implements TV {
    public void on()                 { System.out.println("  📺 索尼 开机"); }
    public void off()                { System.out.println("  📺 索尼 关机"); }
    public void setChannel(int n)    { System.out.println("  📺 索尼 切到频道 " + n); }
}

class SamsungTV implements TV {
    public void on()                 { System.out.println("  📺 三星 开机"); }
    public void off()                { System.out.println("  📺 三星 关机"); }
    public void setChannel(int n)    { System.out.println("  📺 三星 切到频道 " + n); }
}

class LGTV implements TV {
    public void on()                 { System.out.println("  📺 LG 开机"); }
    public void off()                { System.out.println("  📺 LG 关机"); }
    public void setChannel(int n)    { System.out.println("  📺 LG 切到频道 " + n); }
}

// ================================================================
// 抽象维度：遥控器基类
// ----------------------------------------------------------------
// 关键：持有 TV 引用（"桥"）→ 任意遥控器可以操作任意电视
// ================================================================
abstract class RemoteControl {

    protected final TV tv;          // 🌉 这就是桥！

    public RemoteControl(TV tv) {
        this.tv = tv;
    }

    public void turnOn() {
        System.out.println("🔘 按开机键");
        tv.on();
    }

    public void turnOff() {
        System.out.println("🔘 按关机键");
        tv.off();
    }
}

// 具体遥控：基础
class BasicRemote extends RemoteControl {
    public BasicRemote(TV tv) { super(tv); }

    public void setChannel(int n) {
        System.out.println("🔘 基础遥控 按频道键 " + n);
        tv.setChannel(n);
    }
}

// 具体遥控：高级（多了静音功能）
class AdvancedRemote extends RemoteControl {
    public AdvancedRemote(TV tv) { super(tv); }

    public void setChannel(int n) {
        System.out.println("🎛 高级遥控 按频道键 " + n);
        tv.setChannel(n);
    }

    public void mute() {
        System.out.println("🎛 高级遥控 静音");
        tv.setChannel(0);
    }
}

// 具体遥控：智能（多了语音控制）
class SmartRemote extends RemoteControl {
    public SmartRemote(TV tv) { super(tv); }

    public void voiceCommand(String command) {
        System.out.println("📱 智能遥控 收到语音: \"" + command + "\"");
        // 简化：识别"切到 X 频道"
        if (command.contains("体育")) {
            tv.setChannel(8);
        } else if (command.contains("新闻")) {
            tv.setChannel(1);
        }
    }
}
