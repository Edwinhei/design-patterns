/**
 * ============================================================
 * ✅ 读写锁 ReadWriteLock
 * ============================================================
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class ReadWriteLockDemo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("===== 场景 1：ReadWriteLock 缓存 =====\n");
        demoReadWriteLock();

        System.out.println("\n===== 场景 2：性能对比 synchronized vs ReadWriteLock =====\n");
        benchmark();

        System.out.println("\n✨ 读写锁的威力：");
        System.out.println("  ① 读读并发 → 读吞吐量大幅提升");
        System.out.println("  ② 读写互斥 → 数据安全");
        System.out.println("  ③ 写写互斥 → 写入原子");
        System.out.println("  ④ 读多写少场景首选（次选 ConcurrentHashMap）");
    }

    // ================================================================
    // 场景 1：演示读写锁用法
    // ================================================================
    static void demoReadWriteLock() throws InterruptedException {
        RWCache cache = new RWCache();
        cache.put("a", "1");

        // 2 个写线程
        Thread w1 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                cache.put("a", "v" + i);
            }
        }, "Writer-1");

        // 3 个读线程
        Thread r1 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                cache.get("a");
            }
        }, "Reader-1");
        Thread r2 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                cache.get("a");
            }
        }, "Reader-2");
        Thread r3 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                cache.get("a");
            }
        }, "Reader-3");

        w1.start(); r1.start(); r2.start(); r3.start();
        w1.join(); r1.join(); r2.join(); r3.join();
    }

    // ================================================================
    // 场景 2：性能对比（使用无日志版本进行公平对比）
    // ================================================================
    static void benchmark() throws InterruptedException {
        int threads = 10;
        int opsPerThread = 500;

        // synchronized 版
        NaiveCacheForBench naive = new NaiveCacheForBench();
        long t1 = benchmarkCache(threads, opsPerThread, i -> naive.get("a"));
        System.out.println("  synchronized 版：" + t1 + "ms");

        // ReadWriteLock 版（无日志）
        RWCacheForBench rwCache = new RWCacheForBench();
        long t2 = benchmarkCache(threads, opsPerThread, i -> rwCache.get("a"));
        System.out.println("  ReadWriteLock 版：" + t2 + "ms");

        double speedup = (double) t1 / t2;
        System.out.printf("  → 读写锁快 %.1fx%n", speedup);
    }

    static long benchmarkCache(int threads, int opsPerThread, java.util.function.IntConsumer action)
            throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        long start = System.currentTimeMillis();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < opsPerThread; j++) action.accept(j);
            });
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return System.currentTimeMillis() - start;
    }
}

// ================================================================
// ReadWriteLock 版缓存
// ================================================================
class RWCache {
    private final Map<String, String> data = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock  = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public String get(String key) {
        readLock.lock();
        try {
            String name = Thread.currentThread().getName();
            System.out.println("  📖 [" + name + "] 获取读锁 get(" + key + ")");
            try { Thread.sleep(50); } catch (InterruptedException e) {}
            return data.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public void put(String key, String value) {
        writeLock.lock();
        try {
            String name = Thread.currentThread().getName();
            System.out.println("  ✏️ [" + name + "] 获取写锁 put(" + key + "=" + value + ")");
            try { Thread.sleep(50); } catch (InterruptedException e) {}
            data.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }
}

// ================================================================
// synchronized 版（对比用，不带打印以公平对比）
// ================================================================
class NaiveCacheForBench {
    private final Map<String, String> data = new HashMap<>();

    {
        data.put("a", "1");
    }

    public synchronized String get(String key) {
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        return data.get(key);
    }
}

// ================================================================
// ReadWriteLock 版（无日志的基准测试版）
// ================================================================
class RWCacheForBench {
    private final Map<String, String> data = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock  = rwLock.readLock();

    {
        data.put("a", "1");
    }

    public String get(String key) {
        readLock.lock();
        try {
            try { Thread.sleep(1); } catch (InterruptedException e) {}
            return data.get(key);
        } finally {
            readLock.unlock();
        }
    }
}
