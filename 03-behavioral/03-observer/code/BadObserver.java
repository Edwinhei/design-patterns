/**
 * ============================================================
 * 🚫 不用观察者 · 发布者硬编码所有订阅者
 * ============================================================
 *
 * 场景：公众号发新闻
 *
 * 土办法：发布者持有具体用户的引用，硬编码通知
 *
 * 运行方式：
 *   java BadObserver.java
 */

public class BadObserver {

    public static void main(String[] args) {
        System.out.println("=== 土办法：发布者硬编码订阅者 ===\n");

        BadPublisher pub = new BadPublisher();
        pub.publishNews("Java 21 发布");

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 发布者必须知道所有订阅者（硬编码 zhang/li/wang）");
        System.out.println("2. 加新订阅者（赵六）→ 要改 publishNews 方法");
        System.out.println("3. 订阅者无法【动态】增减");
        System.out.println("4. 发布者和订阅者紧耦合");

        System.out.println("\n👉 看 ObserverDemo.java 如何用观察者模式解耦");
    }
}

class BadPublisher {
    // 🚨 硬编码所有订阅者
    NaiveUser zhang = new NaiveUser("张三");
    NaiveUser li = new NaiveUser("李四");
    NaiveUser wang = new NaiveUser("王五");

    public void publishNews(String news) {
        System.out.println("[技术公众号] " + news);
        // 🚨 一个个调用，加新的要改这里
        zhang.receive(news);
        li.receive(news);
        wang.receive(news);
    }
}

class NaiveUser {
    private final String name;

    public NaiveUser(String name) {
        this.name = name;
    }

    public void receive(String news) {
        System.out.println("  📱 " + name + " 收到: " + news);
    }
}
