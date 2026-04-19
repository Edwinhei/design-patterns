/**
 * ============================================================
 * 🚫 不用读写锁 · synchronized 读写都独占
 * ============================================================
 */

import java.util.*;
import java.util.concurrent.*;

public class BadReadWriteLock {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 土办法：synchronized 全独占 ===\n");

        NaiveCache cache = new NaiveCache();
        cache.put("a", "1");

        // 10 个读线程并发读（本应无冲突）
        ExecutorService pool = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            pool.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    cache.get("a");     // 🚨 synchronized → 读读也互斥
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        long cost = System.currentTimeMillis() - start;
        System.out.println("1000 次 get 耗时: " + cost + "ms");

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 读线程之间也排队（读读不应该互斥）");
        System.out.println("2. 99% 的读请求在白等");
        System.out.println("3. 并发吞吐量差");

        System.out.println("\n👉 看 ReadWriteLockDemo.java 如何让读并发");
    }
}

class NaiveCache {
    private final Map<String, String> data = new HashMap<>();

    public synchronized String get(String key) {
        try { Thread.sleep(1); } catch (InterruptedException e) {}   // 模拟耗时
        return data.get(key);
    }

    public synchronized void put(String key, String value) {
        data.put(key, value);
    }
}
