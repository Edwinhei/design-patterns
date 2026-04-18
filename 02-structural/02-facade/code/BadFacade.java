/**
 * ============================================================
 * 🚫 不用 Facade · 客户端要管所有子系统
 * ============================================================
 *
 * 场景：回家要启动一堆智能家居设备
 *
 * 土办法：
 *   客户端代码 → 直接操作所有子系统
 *   - 要记住所有设备的类名
 *   - 要记住调用顺序
 *   - 每个使用场景都要复制粘贴一大段
 *
 * 运行方式：
 *   java BadFacade.java
 */

public class BadFacade {

    public static void main(String[] args) {
        System.out.println("=== 土办法：回家要一个个操作所有设备 ===\n");

        // 客户端要自己 new 所有子系统
        SmartLight light = new SmartLight();
        AirCon ac = new AirCon();
        DoorLock lock = new DoorLock();
        Curtain curtain = new Curtain();
        MusicPlayer music = new MusicPlayer();

        // 回家流程：一大堆操作
        System.out.println("--- 客户端执行'回家模式' ---");
        lock.unlock();
        light.turnOn();
        light.setDim(80);
        ac.powerOn();
        ac.setTemp(24);
        curtain.open();
        music.start();
        music.setVolume(30);

        System.out.println("\n--- 客户端执行'离家模式'（又要来一遍）---");
        music.stop();
        ac.powerOff();
        light.turnOff();
        curtain.close();
        lock.lock();

        // ---- 问题暴露 ----
        System.out.println("\n⚠️  问题暴露：");
        System.out.println("1. 客户端要了解 5 个子系统（+各自的方法）");
        System.out.println("2. 操作顺序、参数全靠客户端自己记");
        System.out.println("3. 每个需要'回家模式'的地方都要重写这段代码");
        System.out.println("4. 子系统升级 → 所有调用方都要改");

        System.out.println("\n👉 解决：看 FacadeDemo.java 如何用一个 SmartHomeFacade 封装");
    }
}

// ================================================================
// 子系统 1：智能灯
// ================================================================
class SmartLight {
    public void turnOn()  { System.out.println("  💡 灯 已开"); }
    public void turnOff() { System.out.println("  💡 灯 已关"); }
    public void setDim(int level) { System.out.println("  💡 亮度调至 " + level + "%"); }
}

// ================================================================
// 子系统 2：空调
// ================================================================
class AirCon {
    public void powerOn()  { System.out.println("  ❄️  空调 开机"); }
    public void powerOff() { System.out.println("  ❄️  空调 关机"); }
    public void setTemp(int temp) { System.out.println("  ❄️  温度设为 " + temp + "°C"); }
}

// ================================================================
// 子系统 3：门锁
// ================================================================
class DoorLock {
    public void lock()   { System.out.println("  🔒 门 已锁"); }
    public void unlock() { System.out.println("  🔓 门 已解锁"); }
}

// ================================================================
// 子系统 4：窗帘
// ================================================================
class Curtain {
    public void open()  { System.out.println("  🪟 窗帘 拉开"); }
    public void close() { System.out.println("  🪟 窗帘 合上"); }
}

// ================================================================
// 子系统 5：音响
// ================================================================
class MusicPlayer {
    public void start() { System.out.println("  🎵 音乐 开始"); }
    public void stop()  { System.out.println("  🎵 音乐 停止"); }
    public void setVolume(int vol) { System.out.println("  🎵 音量 " + vol + "%"); }
}
