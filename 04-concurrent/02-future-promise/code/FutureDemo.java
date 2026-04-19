/**
 * ============================================================
 * ✅ Future / CompletableFuture 演示
 * ============================================================
 *
 * 场景：并发调 3 个接口聚合结果
 */

import java.util.concurrent.*;

public class FutureDemo {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);

    public static void main(String[] args) throws Exception {

        // === 方式 1：Future (Java 5+) ===
        System.out.println("===== 方式 1：Future 并发调用 =====");
        futureWay();

        // === 方式 2：CompletableFuture (Java 8+ 现代) ===
        System.out.println("\n===== 方式 2：CompletableFuture 链式 =====");
        completableFutureWay();

        // === 方式 3：CompletableFuture 组合 ===
        System.out.println("\n===== 方式 3：thenCombine 组合 =====");
        combineWay();

        POOL.shutdown();
    }

    // ================================================================
    // 方式 1：Future
    // ================================================================
    static void futureWay() throws Exception {
        long start = System.currentTimeMillis();

        // 立刻拿到三个"凭证"，三个任务并发执行
        Future<String> userF    = POOL.submit(FutureDemo::fetchUser);
        Future<String> productF = POOL.submit(FutureDemo::fetchProduct);
        Future<String> couponF  = POOL.submit(FutureDemo::fetchCoupon);

        // 需要结果时才阻塞等
        String user    = userF.get();
        String product = productF.get();
        String coupon  = couponF.get();

        long cost = System.currentTimeMillis() - start;
        System.out.println("✅ 结果: [" + user + ", " + product + ", " + coupon + "]");
        System.out.println("⏱ 总耗时: " + cost + "ms（对比串行 3000ms）");
    }

    // ================================================================
    // 方式 2：CompletableFuture 基础用法
    // ================================================================
    static void completableFutureWay() throws Exception {
        long start = System.currentTimeMillis();

        CompletableFuture<String> userF = CompletableFuture
            .supplyAsync(FutureDemo::fetchUser, POOL)
            .thenApply(u -> u.toUpperCase())                    // 转换
            .exceptionally(e -> "User:兜底");                    // 异常兜底

        String result = userF.get();

        long cost = System.currentTimeMillis() - start;
        System.out.println("✅ 结果: " + result);
        System.out.println("⏱ 耗时: " + cost + "ms（含链式处理）");
    }

    // ================================================================
    // 方式 3：多个任务合并（thenCombine）
    // ================================================================
    static void combineWay() throws Exception {
        long start = System.currentTimeMillis();

        CompletableFuture<String> userF    = CompletableFuture.supplyAsync(FutureDemo::fetchUser, POOL);
        CompletableFuture<String> productF = CompletableFuture.supplyAsync(FutureDemo::fetchProduct, POOL);
        CompletableFuture<String> couponF  = CompletableFuture.supplyAsync(FutureDemo::fetchCoupon, POOL);

        // 三路汇总
        CompletableFuture<String> merged = userF
            .thenCombine(productF, (u, p) -> u + " + " + p)
            .thenCombine(couponF, (up, c) -> up + " + " + c);

        String result = merged.get();

        long cost = System.currentTimeMillis() - start;
        System.out.println("✅ 聚合结果: " + result);
        System.out.println("⏱ 耗时: " + cost + "ms（取最慢那个）");

        System.out.println("\n✨ Future 模式的威力：");
        System.out.println("  ① 三个接口并发，总耗时从 3s → ~1s");
        System.out.println("  ② Future.get() 需要才阻塞，其他时间主线程可以做别的");
        System.out.println("  ③ CompletableFuture 支持链式组合 / 异常处理");
        System.out.println("  ④ thenCombine 优雅合并多个异步结果");
    }

    // ================================================================
    // 模拟三个接口
    // ================================================================
    static String fetchUser() {
        System.out.println("  👤 " + threadName() + " 取用户...");
        sleep(1000);
        return "User:张三";
    }

    static String fetchProduct() {
        System.out.println("  📦 " + threadName() + " 取商品...");
        sleep(1000);
        return "Product:iPhone";
    }

    static String fetchCoupon() {
        System.out.println("  🎫 " + threadName() + " 取优惠券...");
        sleep(1000);
        return "Coupon:满100减20";
    }

    static String threadName() {
        return Thread.currentThread().getName();
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
