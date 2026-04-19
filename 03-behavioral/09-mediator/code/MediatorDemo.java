/**
 * ============================================================
 * ✅ 中介者模式 · 聊天室
 * ============================================================
 *
 * 设计：
 *   - ChatRoom 接口（Mediator）
 *   - ConcreteChatRoom 实现（持有所有 User）
 *   - User（Colleague）只持有 ChatRoom 引用
 *
 * 效果：
 *   N 个用户 → N 条连线（不是 N²）
 *
 * 运行方式：
 *   java MediatorDemo.java
 */

import java.util.*;

public class MediatorDemo {

    public static void main(String[] args) {
        ChatRoom room = new ConcreteChatRoom("程序员技术交流群");

        User alice = new User("Alice");
        User bob   = new User("Bob");
        User tom   = new User("Tom");
        User dan   = new User("Dan");

        // 注册到房间
        room.register(alice);
        room.register(bob);
        room.register(tom);
        room.register(dan);

        System.out.println("===== Alice 发消息 =====");
        alice.send("大家好，新人报道");

        System.out.println("\n===== Bob 回复 =====");
        bob.send("欢迎 Alice!");

        System.out.println("\n===== Tom 发技术问题 =====");
        tom.send("有人知道 Lambda 语法吗？");

        System.out.println("\n✨ 中介者模式的威力：");
        System.out.println("  ① 用户之间【不互相引用】，只认识 ChatRoom");
        System.out.println("  ② N 个用户 = N 条连线（不是 N²）");
        System.out.println("  ③ 加新用户只需 room.register()，其他用户代码不动");
        System.out.println("  ④ 消息分发逻辑集中在 ChatRoom 里");
    }
}

// ================================================================
// 中介者接口
// ================================================================
interface ChatRoom {
    void register(User user);
    void send(String from, String msg);
}

// ================================================================
// 具体中介者：聊天室（持有所有用户）
// ================================================================
class ConcreteChatRoom implements ChatRoom {
    private final String name;
    private final List<User> users = new ArrayList<>();

    public ConcreteChatRoom(String name) {
        this.name = name;
    }

    @Override
    public void register(User user) {
        users.add(user);
        user.setRoom(this);        // 通知 user 它在哪个 room
    }

    @Override
    public void send(String from, String msg) {
        System.out.println("[" + name + "] " + from + ": " + msg);
        for (User u : users) {
            if (!u.getName().equals(from)) {
                u.receive(from, msg);
            }
        }
    }
}

// ================================================================
// 同事：User（只认识 ChatRoom）
// ================================================================
class User {
    private final String name;
    private ChatRoom room;        // 只持有中介者引用

    public User(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setRoom(ChatRoom room) {
        this.room = room;
    }

    public void send(String msg) {
        if (room == null) {
            System.out.println(name + " 未加入任何房间");
            return;
        }
        room.send(name, msg);
    }

    public void receive(String from, String msg) {
        System.out.println("  👤 " + name + " 收到 [" + from + "]: " + msg);
    }
}
