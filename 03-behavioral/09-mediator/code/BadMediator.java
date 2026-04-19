/**
 * ============================================================
 * 🚫 不用中介者 · 对象互相直接引用
 * ============================================================
 *
 * 运行方式：
 *   java BadMediator.java
 */

import java.util.*;

public class BadMediator {

    public static void main(String[] args) {
        System.out.println("=== 土办法：每个人都持有所有人的引用 ===\n");

        NaiveUser alice = new NaiveUser("Alice");
        NaiveUser bob   = new NaiveUser("Bob");
        NaiveUser tom   = new NaiveUser("Tom");

        // 🚨 每个人都要手动加其他人为朋友
        alice.addFriend(bob);
        alice.addFriend(tom);

        bob.addFriend(alice);
        bob.addFriend(tom);

        tom.addFriend(alice);
        tom.addFriend(bob);

        alice.send("大家好");

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 3 个用户就要 6 次 addFriend（N×(N-1) 条连线）");
        System.out.println("2. 加新用户（比如 Dan）→ 要给所有已有用户加他");
        System.out.println("3. 用户之间紧耦合");
        System.out.println("4. 消息转发逻辑分散在每个用户里");

        System.out.println("\n👉 看 MediatorDemo.java 如何用中介者解耦");
    }
}

class NaiveUser {
    private final String name;
    private final List<NaiveUser> friends = new ArrayList<>();

    public NaiveUser(String name) {
        this.name = name;
    }

    public void addFriend(NaiveUser u) {
        friends.add(u);
    }

    public void send(String msg) {
        System.out.println(name + " 发送: " + msg);
        for (NaiveUser f : friends) {
            f.receive(name, msg);     // 🚨 直接调用其他用户
        }
    }

    public void receive(String from, String msg) {
        System.out.println("  " + name + " 收到 [" + from + "]: " + msg);
    }
}
