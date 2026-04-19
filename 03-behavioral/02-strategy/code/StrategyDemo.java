/**
 * ============================================================
 * ✅ 策略模式 Strategy · 出行方式
 * ============================================================
 *
 * 三角色：
 *   ① 策略接口 TransportStrategy
 *   ② 具体策略：CarStrategy / SubwayStrategy / BikeStrategy
 *   ③ 上下文 TravelContext：持有策略，可运行时切换
 *
 * 运行方式：
 *   java StrategyDemo.java
 */

import java.util.Map;

public class StrategyDemo {

    public static void main(String[] args) {

        TravelContext ctx = new TravelContext();

        // === 场景 1：基本用法 ===
        System.out.println("===== 基本用法：运行时切换策略 =====");
        ctx.setStrategy(new CarStrategy());
        ctx.travel("公司");

        ctx.setStrategy(new SubwayStrategy());
        ctx.travel("商场");

        ctx.setStrategy(new BikeStrategy());
        ctx.travel("公园");

        // === 场景 2：Lambda 简化（Java 8+）===
        System.out.println("\n===== Lambda 简化（Java 8+）=====");
        ctx.setStrategy(dest -> System.out.println("🛵 骑摩托去 " + dest));
        ctx.travel("山顶");

        ctx.setStrategy(dest -> System.out.println("✈️  飞机去 " + dest));
        ctx.travel("上海");

        // === 场景 3：Map 查表消除 if-else（重要！）===
        System.out.println("\n===== Map 查表法：彻底消灭 if-else =====");
        Map<String, TransportStrategy> strategies = Map.of(
                "car",    new CarStrategy(),
                "subway", new SubwayStrategy(),
                "bike",   new BikeStrategy()
        );

        String userInput = "subway";                    // 假设用户选地铁
        TransportStrategy chosen = strategies.get(userInput);
        if (chosen != null) {
            chosen.go("火车站");
        }

        // === 小结 ===
        System.out.println("\n✨ 策略模式的威力：");
        System.out.println("  ① 每种算法独立成类，代码清晰");
        System.out.println("  ② 加新策略只需新增一个类，不改已有代码");
        System.out.println("  ③ 运行时动态切换（ctx.setStrategy）");
        System.out.println("  ④ Lambda 让简单策略更简洁");
        System.out.println("  ⑤ Map + 策略 = 彻底消灭 if-else");
    }
}

// ================================================================
// 策略接口
// ================================================================
interface TransportStrategy {
    void go(String destination);
}

// ================================================================
// 具体策略
// ================================================================
class CarStrategy implements TransportStrategy {
    public void go(String dest) {
        System.out.println("🚗 开车去 " + dest + "（快但堵）");
    }
}

class SubwayStrategy implements TransportStrategy {
    public void go(String dest) {
        System.out.println("🚇 坐地铁去 " + dest + "（稳定准时）");
    }
}

class BikeStrategy implements TransportStrategy {
    public void go(String dest) {
        System.out.println("🚴 骑车去 " + dest + "（锻炼身体）");
    }
}

// ================================================================
// Context：持有策略引用
// ================================================================
class TravelContext {
    private TransportStrategy strategy;

    public void setStrategy(TransportStrategy strategy) {
        this.strategy = strategy;
    }

    public void travel(String destination) {
        if (strategy == null) {
            throw new IllegalStateException("请先设置出行策略");
        }
        strategy.go(destination);
    }
}
