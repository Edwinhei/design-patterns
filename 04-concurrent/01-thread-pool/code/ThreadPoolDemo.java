/**
 * ============================================================
 * ✅ 线程池模式 · Java ExecutorService 完整演示
 * ============================================================
 *
 * 运行方式：
 *   java ThreadPoolDemo.java
 */

import java.util.concurrent.*;

public class ThreadPoolDemo {

    public static void main(String[] args) throws Exception {

        // === 场景 1：最常见的固定线程池 ===
        System.out.println("===== 场景 1：固定线程池 (size=3) =====");
        demoFixed();

        // === 场景 2：生产推荐 · 手动构造 ===
        System.out.println("\n===== 场景 2：手动 ThreadPoolExecutor（生产推荐）=====");
        demoCustom();

        // === 场景 3：定时任务 ===
        System.out.println("\n===== 场景 3：定时任务池 =====");
        demoScheduled();

        System.out.println("\n✨ 线程池的威力：");
        System.out.println("  ① 线程复用，大幅减少创建/销毁开销");
        System.out.println("  ② 有界队列 + 拒绝策略 → 防 OOM");
        System.out.println("  ③ 统一管理：监控/关闭/调参");
        System.out.println("  ④ 生产环境首选 ThreadPoolExecutor 手动构造");
    }

    // ================================================================
    // 场景 1：Executors 快速创建
    // ================================================================
    static void demoFixed() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);

        long start = System.currentTimeMillis();

        // 提交 10 个任务，但池只有 3 个线程
        for (int i = 1; i <= 10; i++) {
            final int id = i;
            pool.submit(() -> {
                System.out.println("  任务 " + id + " 由 "
                        + Thread.currentThread().getName() + " 执行");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        long cost = System.currentTimeMillis() - start;
        System.out.println("  10 个任务耗时 " + cost + "ms");
        System.out.println("  观察：3 个线程重复用（复用）");
    }

    // ================================================================
    // 场景 2：手动构造（生产环境推荐）
    // ================================================================
    static void demoCustom() throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            2,                              // 核心 2
            4,                              // 最大 4
            60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(3),    // 有界队列（容量 3）
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy()   // 拒绝策略：调用方自己跑
        );

        // 提交 10 个任务
        for (int i = 1; i <= 10; i++) {
            final int id = i;
            try {
                pool.submit(() -> {
                    System.out.println("  任务 " + id + " → "
                            + Thread.currentThread().getName());
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                });
            } catch (Exception e) {
                System.out.println("  ❌ 任务 " + id + " 被拒绝");
            }
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("  观察：");
        System.out.println("  - 前 2 任务 → 核心线程");
        System.out.println("  - 接下来 3 → 队列等候");
        System.out.println("  - 再来的 → 开非核心线程到最大 4");
        System.out.println("  - 仍然提交不进 → 调用方自己跑（CallerRunsPolicy）");
    }

    // ================================================================
    // 场景 3：定时任务池
    // ================================================================
    static void demoScheduled() throws Exception {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // 延迟 1 秒后执行
        scheduler.schedule(
            () -> System.out.println("  ⏰ 延迟任务执行（1 秒后）"),
            1, TimeUnit.SECONDS
        );

        // 每 500ms 周期执行，共 3 次
        int[] count = {0};
        ScheduledFuture<?> periodic = scheduler.scheduleAtFixedRate(
            () -> {
                System.out.println("  🔁 周期任务 #" + (++count[0]));
            },
            0, 500, TimeUnit.MILLISECONDS
        );

        Thread.sleep(1700);
        periodic.cancel(false);

        scheduler.shutdown();
    }
}
