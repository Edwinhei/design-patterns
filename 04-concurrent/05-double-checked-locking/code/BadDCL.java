/**
 * ============================================================
 * 🚫 不用 DCL · 三种错误/低效方案
 * ============================================================
 */

public class BadDCL {

    public static void main(String[] args) {
        System.out.println("=== 方案 A：没锁 → 多线程可能创建多次 ===\n");
        System.out.println("（多线程并发下 loadFromDisk 可能被调多次）");

        System.out.println("\n=== 方案 B：synchronized 全方法 → 每次加锁 ===\n");
        System.out.println("（之后每次 get 都加锁，99.99% 无意义）");

        System.out.println("\n=== 方案 C：DCL 但缺 volatile → 半成品陷阱 ===\n");
        System.out.println("（指令重排可能让另一线程看到非 null 但未初始化对象）");

        System.out.println("\n⚠️  总结：");
        System.out.println("1. 没锁 → 多次 load 浪费内存");
        System.out.println("2. 全锁 → 性能差");
        System.out.println("3. 无 volatile → 数据竞争 / NPE");

        System.out.println("\n👉 看 DCLDemo.java 如何正确实现双检锁");
    }
}

// 方案 A：线程不安全
class UnsafeConfigLoader {
    private Config config;

    public Config get() {
        if (config == null) {
            config = new Config();     // 🚨 多线程都可能进
        }
        return config;
    }
}

// 方案 B：每次都锁
class SyncMethodConfigLoader {
    private Config config;

    public synchronized Config get() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }
}

// 方案 C：DCL 但缺 volatile（不安全）
class UnsafeDCLConfigLoader {
    private Config config;                // 🚨 没有 volatile

    public Config get() {
        if (config == null) {
            synchronized (this) {
                if (config == null) {
                    config = new Config();   // 可能指令重排 → 半成品暴露
                }
            }
        }
        return config;
    }
}

class Config {
    public Config() {
        System.out.println("Config 加载完成");
    }
}
