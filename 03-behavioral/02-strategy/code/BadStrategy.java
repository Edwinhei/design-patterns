/**
 * ============================================================
 * 🚫 不用策略 · if-else 大串烧
 * ============================================================
 *
 * 场景：根据出行方式选择不同的逻辑
 *
 * 土办法：一个方法里 if-else 判断类型
 *
 * 运行方式：
 *   java BadStrategy.java
 */

public class BadStrategy {

    public static void main(String[] args) {
        travel("car", "公司");
        travel("subway", "商场");
        travel("bike", "公园");
        travel("plane", "上海");   // 🚨 出问题了

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 加新出行方式（比如打车）→ 要改这个方法");
        System.out.println("2. 每种方式代码复杂时，方法爆炸增长");
        System.out.println("3. 字符串匹配，typo 不报错");
        System.out.println("4. 违反开闭原则 —— 加新功能要改老代码");

        System.out.println("\n👉 看 StrategyDemo.java 如何用策略模式优雅解决");
    }

    /**
     * 🚨 if-else 大串烧，随业务扩展越来越长
     */
    static void travel(String mode, String destination) {
        if (mode.equals("car")) {
            System.out.println("🚗 开车去 " + destination + "（快但堵）");
        } else if (mode.equals("subway")) {
            System.out.println("🚇 坐地铁去 " + destination + "（稳定准时）");
        } else if (mode.equals("bike")) {
            System.out.println("🚴 骑车去 " + destination + "（锻炼身体）");
        } else if (mode.equals("walk")) {
            System.out.println("🚶 走路去 " + destination + "（最慢）");
        } else {
            System.out.println("❌ 未知出行方式: " + mode);
        }
    }
}
