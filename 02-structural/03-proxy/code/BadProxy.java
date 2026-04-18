/**
 * ============================================================
 * 🚫 不用代理 · 业务代码和横切关注点混在一起
 * ============================================================
 *
 * 场景：明星自己处理所有事务（没有经纪人）
 *   - 要判断客户预算
 *   - 要记录时间
 *   - 要结算
 *   - ……还要唱歌演出
 *
 * 痛点：这些横切关注点散落在每个方法里，业务代码被淹没
 *
 * 运行方式：
 *   java BadProxy.java
 */

public class BadProxy {

    public static void main(String[] args) {
        System.out.println("=== 土办法：明星自己接电话（横切关注点混在业务里）===\n");

        Singer singer = new Singer("周杰伦");
        singer.sing("稻香");
        System.out.println();
        singer.attend("商业演出", 500000);
        System.out.println();
        singer.attend("小婚礼", 30000);    // 预算不够

        System.out.println("\n⚠️  问题暴露：");
        System.out.println("1. sing / attend 方法里【筛选 + 记录 + 结算 + 业务】全混一起");
        System.out.println("2. 如果要改筛选规则 / 加新日志 → 每个方法都要改");
        System.out.println("3. 新增一个业务方法（如 dance）→ 横切代码又要写一遍");
        System.out.println("4. 业务代码和非业务代码耦合，单元测试难写");

        System.out.println("\n👉 解决：看 ProxyDemo.java 如何用'经纪人（代理）'分离关注点");
    }
}

// ================================================================
// 明星（一个人扛所有）
// ================================================================
class Singer {
    private final String name;
    private static final double MIN_FEE = 100_000;

    public Singer(String name) {
        this.name = name;
    }

    public void sing(String song) {
        // 横切关注点：日志
        System.out.println("📞 " + name + "：接到唱歌请求 → " + song);
        long start = System.nanoTime();

        // 真正的业务
        System.out.println("  🎤 演唱中: " + song);
        try { Thread.sleep(50); } catch (InterruptedException e) {}

        // 横切关注点：耗时统计
        long cost = (System.nanoTime() - start) / 1_000_000;
        System.out.println("💰 " + name + "：演唱完成，结算中（耗时 " + cost + "ms）");
    }

    public void attend(String event, double fee) {
        // 横切关注点：日志
        System.out.println("📞 " + name + "：接到活动邀请 → " + event + "（报价 ¥" + fee + "）");

        // 横切关注点：权限检查
        if (fee < MIN_FEE) {
            System.out.println("❌ " + name + "：报价不够 ¥" + MIN_FEE + "，拒绝");
            return;
        }

        long start = System.nanoTime();

        // 真正的业务
        System.out.println("  🎭 出席活动: " + event);
        try { Thread.sleep(50); } catch (InterruptedException e) {}

        // 横切关注点：耗时统计
        long cost = (System.nanoTime() - start) / 1_000_000;
        System.out.println("💰 " + name + "：活动完成，结算中（耗时 " + cost + "ms）");
    }
}
