/**
 * ============================================================
 * ✅ 双检锁 DCL 正确姿势
 * ============================================================
 */

import java.util.concurrent.*;
import java.util.function.Supplier;

public class DCLDemo {

    public static void main(String[] args) throws InterruptedException {
        // === 场景 1：DCL 基本用法 ===
        System.out.println("===== 场景 1：DCL 懒加载 Config =====");
        demoDCL();

        // === 场景 2：通用 LazyResource 模板 ===
        System.out.println("\n===== 场景 2：通用 LazyResource 模板 =====");
        demoGeneric();

        System.out.println("\n✨ DCL 的威力：");
        System.out.println("  ① 快路径无锁（已初始化时不加锁）");
        System.out.println("  ② 慢路径加锁 + 双检（防多次初始化）");
        System.out.println("  ③ volatile 禁止重排（防半成品暴露）");
        System.out.println("  ④ 100 个线程并发 → 只初始化 1 次");
    }

    // ================================================================
    // 场景 1：DCL 模板
    // ================================================================
    static void demoDCL() throws InterruptedException {
        ConfigLoader loader = new ConfigLoader();

        // 100 个线程并发 get
        ExecutorService pool = Executors.newFixedThreadPool(20);
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            pool.submit(() -> loader.get());
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        long cost = System.currentTimeMillis() - start;
        System.out.println("✅ 100 个线程并发 get 完成，耗时: " + cost + "ms");
        System.out.println("   Config 初始化次数: " + loader.getInitCount() + "（应该是 1）");
    }

    // ================================================================
    // 场景 2：通用 LazyResource
    // ================================================================
    static void demoGeneric() throws InterruptedException {
        int[] loadCount = {0};
        LazyResource<String> lazy = new LazyResource<>(() -> {
            loadCount[0]++;
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            return "LoadedResource";
        });

        ExecutorService pool = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 50; i++) {
            pool.submit(() -> lazy.get());
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("✅ 50 次并发 get，loader 被调用次数: " + loadCount[0] + "（应该是 1）");
    }
}

// ================================================================
// DCL 懒加载 Config
// ================================================================
class ConfigLoader {
    private volatile Config config;                    // ← volatile 关键
    private int initCount = 0;

    public Config get() {
        if (config == null) {                          // 快路径
            synchronized (this) {
                if (config == null) {                  // 双检
                    config = loadFromDisk();
                }
            }
        }
        return config;
    }

    private Config loadFromDisk() {
        synchronized (this) { initCount++; }
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        return new Config();
    }

    public int getInitCount() { return initCount; }
}

class Config {
    // 模拟大对象
    public Config() {
        System.out.println("  📂 Config 加载（应该只执行一次）");
    }
}

// ================================================================
// 通用 LazyResource 模板
// ================================================================
class LazyResource<T> {
    private volatile T resource;
    private final Supplier<T> loader;

    public LazyResource(Supplier<T> loader) {
        this.loader = loader;
    }

    public T get() {
        T r = resource;
        if (r == null) {
            synchronized (this) {
                r = resource;
                if (r == null) {
                    r = loader.get();
                    resource = r;
                }
            }
        }
        return r;
    }
}
