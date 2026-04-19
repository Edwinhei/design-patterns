/**
 * ============================================================
 * 🚫 不用队列 · 生产者直接同步调用消费者
 * ============================================================
 */

public class BadProducerConsumer {

    public static void main(String[] args) {
        System.out.println("=== 土办法：同步处理 ===\n");

        OrderHandler handler = new OrderHandler();
        long start = System.currentTimeMillis();

        // 模拟 5 个订单下单
        for (int i = 1; i <= 5; i++) {
            System.out.println("用户 #" + i + " 下单");
            handler.handle("订单" + i);       // 🚨 同步等处理完
        }

        long cost = System.currentTimeMillis() - start;
        System.out.println("\n总耗时: " + cost + "ms");

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 用户下单要等处理完才返回");
        System.out.println("2. 串行处理，下单接口响应慢");
        System.out.println("3. 无法多消费者并行");
        System.out.println("4. 生产者和消费者紧耦合");

        System.out.println("\n👉 看 ProducerConsumerDemo.java 如何异步解耦");
    }
}

class OrderHandler {
    void handle(String order) {
        System.out.println("  开始处理 " + order);
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        System.out.println("  完成处理 " + order);
    }
}
