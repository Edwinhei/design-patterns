/**
 * ============================================================
 * ✅ 观察者模式 Observer · 公众号订阅
 * ============================================================
 *
 * 三角色：
 *   ① Subject (PublicAccount)：持有观察者列表
 *   ② Observer 接口 (Subscriber)：定义"收到通知后做什么"
 *   ③ 具体 Observer (User)：实现接口
 *
 * 运行方式：
 *   java ObserverDemo.java
 */

import java.util.*;

public class ObserverDemo {

    public static void main(String[] args) {

        // === 场景 1：基础订阅 ===
        System.out.println("===== 场景 1：张三 + 李四 订阅 =====");
        PublicAccount tech = new PublicAccount("技术公众号");

        Subscriber zhang = new User("张三");
        Subscriber li = new User("李四");

        tech.subscribe(zhang);
        tech.subscribe(li);

        tech.publish("Java 21 新特性");

        // === 场景 2：取消订阅 ===
        System.out.println("\n===== 场景 2：张三取消订阅 =====");
        tech.unsubscribe(zhang);
        tech.publish("Spring Boot 3 升级指南");

        // === 场景 3：用 Lambda 定义订阅者（函数式接口）===
        System.out.println("\n===== 场景 3：Lambda 订阅者 =====");
        tech.subscribe(news -> System.out.println("  🤖 匿名爬虫抓取: " + news));
        tech.subscribe(news -> {
            if (news.contains("Java")) {
                System.out.println("  ☕ Java 粉丝特别关注: " + news);
            }
        });

        tech.publish("Java 虚拟线程深度解析");

        // === 场景 4：多公众号 ===
        System.out.println("\n===== 场景 4：订阅多个公众号 =====");
        PublicAccount food = new PublicAccount("美食公众号");
        food.subscribe(li);
        food.subscribe(new User("王五"));

        food.publish("今日推荐：红烧肉做法");

        // === 小结 ===
        System.out.println("\n✨ 观察者模式的威力：");
        System.out.println("  ① 发布者不知道具体订阅者是谁（只知道 Subscriber 接口）");
        System.out.println("  ② 加新订阅者只需 subscribe()，发布者代码不动");
        System.out.println("  ③ 动态订阅/取消");
        System.out.println("  ④ 一次发布，所有订阅者自动收到");
        System.out.println("  ⑤ Lambda 让简单订阅者代码最小化");
    }
}

// ================================================================
// 观察者接口（函数式接口，单方法）
// ================================================================
@FunctionalInterface
interface Subscriber {
    void onNews(String news);
}

// ================================================================
// Subject：公众号（被观察者）
// ================================================================
class PublicAccount {
    private final String name;
    private final List<Subscriber> subscribers = new ArrayList<>();

    public PublicAccount(String name) {
        this.name = name;
    }

    // 订阅
    public void subscribe(Subscriber s) {
        subscribers.add(s);
    }

    // 取消订阅
    public void unsubscribe(Subscriber s) {
        subscribers.remove(s);
    }

    // 发布 → 通知所有订阅者
    public void publish(String news) {
        System.out.println("[" + name + "] 📢 " + news);
        // 遍历通知
        for (Subscriber s : subscribers) {
            s.onNews(news);
        }
    }
}

// ================================================================
// 具体观察者：普通用户
// ================================================================
class User implements Subscriber {
    private final String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public void onNews(String news) {
        System.out.println("  📱 " + name + " 收到: " + news);
    }
}
