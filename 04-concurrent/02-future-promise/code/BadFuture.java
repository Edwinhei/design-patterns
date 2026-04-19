/**
 * ============================================================
 * 🚫 不用 Future · 串行同步调用
 * ============================================================
 */

public class BadFuture {

    public static void main(String[] args) {
        System.out.println("=== 土办法：串行调三个接口 ===\n");

        long start = System.currentTimeMillis();

        String user    = fetchUser();
        String product = fetchProduct();
        String coupon  = fetchCoupon();

        long cost = System.currentTimeMillis() - start;
        System.out.println("\n结果: [" + user + ", " + product + ", " + coupon + "]");
        System.out.println("⏱ 总耗时: " + cost + "ms");

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 三个接口串行等 → 总耗时 = 各自耗时之和（约 3s）");
        System.out.println("2. 其实三者不依赖，完全可以并发");
        System.out.println("3. 主线程大部分时间在等 I/O");

        System.out.println("\n👉 看 FutureDemo.java 如何用 Future 并发");
    }

    static String fetchUser() {
        System.out.println("👤 开始取用户信息...");
        sleep(1000);
        System.out.println("👤 用户信息返回");
        return "User:张三";
    }

    static String fetchProduct() {
        System.out.println("📦 开始取商品信息...");
        sleep(1000);
        System.out.println("📦 商品信息返回");
        return "Product:iPhone";
    }

    static String fetchCoupon() {
        System.out.println("🎫 开始取优惠券...");
        sleep(1000);
        System.out.println("🎫 优惠券返回");
        return "Coupon:满100减20";
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
