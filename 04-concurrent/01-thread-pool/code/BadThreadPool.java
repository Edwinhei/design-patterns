/**
 * ============================================================
 * 🚫 不用线程池 · 每任务 new Thread
 * ============================================================
 *
 * 运行方式：
 *   java BadThreadPool.java
 */

public class BadThreadPool {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 土办法：每任务 new Thread ===\n");

        long start = System.currentTimeMillis();

        // 模拟 100 个请求
        Thread[] threads = new Thread[100];
        for (int i = 0; i < 100; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                // 模拟处理请求
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            });
            threads[i].start();       // 🚨 每次都 new Thread
        }

        // 等所有线程完成
        for (Thread t : threads) {
            t.join();
        }

        long cost = System.currentTimeMillis() - start;
        System.out.println("100 个任务完成，耗时 " + cost + "ms");

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 创建/销毁 100 个线程，全是 OS 级操作");
        System.out.println("2. 如果是 10000 个请求？内存爆炸（每线程 ~1MB 栈）");
        System.out.println("3. 无法复用，每次都从零开始");
        System.out.println("4. 线程数量失控，无法限流");

        System.out.println("\n👉 看 ThreadPoolDemo.java 如何用线程池复用");
    }
}
