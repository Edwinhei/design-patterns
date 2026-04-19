/**
 * ============================================================
 * ✅ 生产者-消费者 · BlockingQueue + 多消费者
 * ============================================================
 *
 * 场景：
 *   - 多个用户下单（生产者）
 *   - 共享订单队列
 *   - 多个处理器并发消费
 */

import java.util.concurrent.*;

public class ProducerConsumerDemo {

    // 共享队列（容量 10）
    private static final BlockingQueue<String> queue = new LinkedBlockingQueue<>(10);

    // 结束标志
    private static final String POISON_PILL = "__STOP__";

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        // 启动 3 个消费者
        Thread c1 = new Thread(new Consumer("C1"));
        Thread c2 = new Thread(new Consumer("C2"));
        Thread c3 = new Thread(new Consumer("C3"));
        c1.start(); c2.start(); c3.start();

        // 启动 2 个生产者（每个生成 5 个订单）
        Thread p1 = new Thread(new Producer("P1", 5));
        Thread p2 = new Thread(new Producer("P2", 5));
        p1.start(); p2.start();

        // 等生产者完成
        p1.join(); p2.join();

        // 给每个消费者一个"毒丸"，让它们自己退出
        queue.put(POISON_PILL);
        queue.put(POISON_PILL);
        queue.put(POISON_PILL);

        c1.join(); c2.join(); c3.join();

        long cost = System.currentTimeMillis() - start;
        System.out.println("\n✅ 全部完成！总耗时: " + cost + "ms");
        System.out.println("\n✨ Producer-Consumer 的威力：");
        System.out.println("  ① 生产/消费完全解耦（互相不知道对方）");
        System.out.println("  ② 队列自动缓冲");
        System.out.println("  ③ 3 个消费者并发处理");
        System.out.println("  ④ 队列满/空时自动阻塞");
    }

    // ================================================================
    // 生产者
    // ================================================================
    static class Producer implements Runnable {
        private final String name;
        private final int count;

        Producer(String name, int count) {
            this.name = name;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= count; i++) {
                    String order = name + "-订单#" + i;
                    queue.put(order);               // 满了会阻塞
                    System.out.println("📥 [" + name + "] 生产: " + order);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ================================================================
    // 消费者
    // ================================================================
    static class Consumer implements Runnable {
        private final String name;

        Consumer(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String order = queue.take();    // 空了会阻塞
                    if (POISON_PILL.equals(order)) {
                        System.out.println("💊 [" + name + "] 收到毒丸，退出");
                        return;
                    }
                    System.out.println("  📤 [" + name + "] 消费: " + order);
                    Thread.sleep(200);              // 模拟处理耗时（比生产慢）
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
