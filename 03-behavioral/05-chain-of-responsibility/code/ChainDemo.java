/**
 * ============================================================
 * ✅ 责任链模式 · 请假审批三级
 * ============================================================
 *
 * 设计：
 *   - Approver 抽象基类，持有 next 引用
 *   - handle() 是模板方法：能处理就处理，不能就传递
 *   - GroupLeader / Manager / Director 具体处理者
 *
 * 运行方式：
 *   java ChainDemo.java
 */

public class ChainDemo {

    public static void main(String[] args) {

        // === 组装责任链 ===
        Approver chain = new GroupLeader();
        chain.setNext(new Manager())
             .setNext(new Director());

        // === 提交请假单 ===
        System.out.println("===== 张三 请假 2 天（组长范围）=====");
        chain.handle(new LeaveRequest("张三", 2));

        System.out.println("\n===== 李四 请假 5 天（经理范围）=====");
        chain.handle(new LeaveRequest("李四", 5));

        System.out.println("\n===== 王五 请假 15 天（总监范围）=====");
        chain.handle(new LeaveRequest("王五", 15));

        System.out.println("\n===== 赵六 请假 40 天（超出范围）=====");
        chain.handle(new LeaveRequest("赵六", 40));

        System.out.println("\n✨ 责任链的威力：");
        System.out.println("  ① 每个处理者只管自己的职责");
        System.out.println("  ② 处理不了就自动传递到下一环");
        System.out.println("  ③ 加新级别（主管）只需加一个类 + 组装链");
        System.out.println("  ④ 组装顺序可以动态调整");
    }
}

// ================================================================
// 请求对象
// ================================================================
class LeaveRequest {
    final String name;
    final int days;

    public LeaveRequest(String name, int days) {
        this.name = name;
        this.days = days;
    }

    @Override
    public String toString() {
        return name + " 请假 " + days + " 天";
    }
}

// ================================================================
// 抽象处理者
// ================================================================
abstract class Approver {
    protected Approver next;

    public Approver setNext(Approver next) {
        this.next = next;
        return next;          // 返回 next，支持链式调用
    }

    // 模板方法：能处理就处理，不能就传递
    public final void handle(LeaveRequest req) {
        System.out.println("  [传到] " + this.getClass().getSimpleName());

        if (canHandle(req)) {
            process(req);
        } else if (next != null) {
            next.handle(req);    // 🔗 传给下一个
        } else {
            System.out.println("  ❌ 无人能处理");
        }
    }

    protected abstract boolean canHandle(LeaveRequest req);
    protected abstract void process(LeaveRequest req);
}

// ================================================================
// 具体处理者
// ================================================================
class GroupLeader extends Approver {
    @Override
    protected boolean canHandle(LeaveRequest req) {
        return req.days <= 3;
    }

    @Override
    protected void process(LeaveRequest req) {
        System.out.println("  👨‍💼 组长批准 [" + req + "]");
    }
}

class Manager extends Approver {
    @Override
    protected boolean canHandle(LeaveRequest req) {
        return req.days <= 7;
    }

    @Override
    protected void process(LeaveRequest req) {
        System.out.println("  🎩 经理批准 [" + req + "]");
    }
}

class Director extends Approver {
    @Override
    protected boolean canHandle(LeaveRequest req) {
        return req.days <= 30;
    }

    @Override
    protected void process(LeaveRequest req) {
        System.out.println("  👔 总监批准 [" + req + "]");
    }
}
