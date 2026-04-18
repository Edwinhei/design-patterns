/**
 * ============================================================
 * ✅ 外观模式 Facade · 智能家居"一键回家"
 * ============================================================
 *
 * 场景：用 SmartHomeFacade 封装 5 个智能设备的复杂操作
 *
 * 核心设计：
 *   - Facade 持有所有子系统对象
 *   - Facade 暴露"简单的业务场景方法"（arriveHome / leaveHome / sleepMode）
 *   - 客户端只和 Facade 打交道，不知道子系统的存在
 *
 * 运行方式：
 *   java FacadeDemo.java
 */

public class FacadeDemo {

    public static void main(String[] args) {
        // 🎯 客户端只需要知道一个 Facade 类
        SmartHomeFacade home = new SmartHomeFacade();

        System.out.println("===== 场景 1：下班回家 =====");
        home.arriveHome();       // 一行搞定所有

        System.out.println("\n===== 场景 2：准备睡觉 =====");
        home.sleepMode();         // 一行搞定所有

        System.out.println("\n===== 场景 3：出门 =====");
        home.leaveHome();         // 一行搞定所有

        System.out.println("\n✅ Facade 模式的威力：");
        System.out.println("   ① 客户端代码只有 3 行（对比 BadFacade 的一堆）");
        System.out.println("   ② 客户端不关心子系统细节（灯/空调/门锁/窗帘/音响）");
        System.out.println("   ③ 流程改动只需改 Facade 一处");
        System.out.println("   ④ 加新设备（比如热水器）→ 客户端代码不变");
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

// ================================================================
// 🎯 Facade：智能家居"前台"
// ----------------------------------------------------------------
// 核心要点：
//   ① 持有所有子系统对象（封装复杂度）
//   ② 暴露贴近"业务场景"的方法（arriveHome 不是 turnOnAllLights）
//   ③ 方法内部按正确顺序调子系统
//   ④ 客户端只需要和 Facade 交互
// ================================================================
class SmartHomeFacade {

    // 持有所有子系统
    private final SmartLight light;
    private final AirCon ac;
    private final DoorLock lock;
    private final Curtain curtain;
    private final MusicPlayer music;

    public SmartHomeFacade() {
        this.light = new SmartLight();
        this.ac = new AirCon();
        this.lock = new DoorLock();
        this.curtain = new Curtain();
        this.music = new MusicPlayer();
    }

    /**
     * "回家模式"：下班到家一键恢复舒适环境
     */
    public void arriveHome() {
        System.out.println("🏠 [回家模式] 欢迎回家");
        lock.unlock();
        light.turnOn();
        light.setDim(80);
        ac.powerOn();
        ac.setTemp(24);
        curtain.open();
        music.start();
        music.setVolume(30);
    }

    /**
     * "离家模式"：出门一键关掉所有电器
     */
    public void leaveHome() {
        System.out.println("🚪 [离家模式] 出门大吉");
        music.stop();
        ac.powerOff();
        light.turnOff();
        curtain.close();
        lock.lock();
    }

    /**
     * "睡眠模式"：夜间一键调整为舒适睡眠环境
     */
    public void sleepMode() {
        System.out.println("🌙 [睡眠模式] 晚安");
        music.stop();
        light.setDim(10);          // 留一盏微弱夜灯
        ac.setTemp(26);             // 睡眠温度
        curtain.close();
    }
}
