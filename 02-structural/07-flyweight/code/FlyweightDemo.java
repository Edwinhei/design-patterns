/**
 * ============================================================
 * ✅ 享元模式 Flyweight · 围棋场景
 * ============================================================
 *
 * 核心：
 *   - 内部状态（color, texture）共享 → 存在享元对象里
 *   - 外部状态（x, y）不共享 → 作为方法参数传入
 *   - 享元工厂维护对象池，同 key 返回同对象
 *
 * 效果：
 *   200 颗棋子 → 内存里只有 2 个享元对象
 *
 * 运行方式：
 *   java FlyweightDemo.java
 */

import java.util.*;

public class FlyweightDemo {

    public static void main(String[] args) {
        System.out.println("=== 享元模式：围棋棋盘 ===\n");

        List<PlacedStone> placedStones = new ArrayList<>();
        Random random = new Random();

        // 放 200 颗棋子
        for (int i = 0; i < 200; i++) {
            String color = random.nextBoolean() ? "黑" : "白";
            int x = random.nextInt(19);
            int y = random.nextInt(19);

            // 🎯 从工厂拿享元对象（相同颜色返回同对象）
            GoStone flyweight = GoStoneFactory.getStone(color);

            // 组合成"已放置的棋子"：享元 + 外部状态
            placedStones.add(new PlacedStone(flyweight, x, y));
        }

        // 画几颗看看
        System.out.println("随机画 5 颗棋子:");
        for (int i = 0; i < 5; i++) {
            placedStones.get(i).draw();
        }

        // === 最关键的对比 ===
        System.out.println("\n=== 内存使用对比 ===");
        System.out.println("棋盘上棋子数: " + placedStones.size());
        System.out.println("享元池对象数: " + GoStoneFactory.poolSize() + " （只有黑+白 2 个）");
        System.out.println("纹理加载次数: " + GoStoneFactory.loadCount() + " 次（不是 200 次！）");

        System.out.println("\n✨ 享元的威力：");
        System.out.println("  ① 200 颗棋子 → 只有 2 个享元对象");
        System.out.println("  ② 纹理只加载 2 次（而不是 200 次）");
        System.out.println("  ③ 内部状态（颜色/纹理）共享");
        System.out.println("  ④ 外部状态（位置 x/y）由客户端管理");

        // 证明享元真的共享
        System.out.println("\n🧪 验证享元复用：");
        GoStone a = GoStoneFactory.getStone("黑");
        GoStone b = GoStoneFactory.getStone("黑");
        System.out.println("  两次获取黑子 == ? " + (a == b));   // true！同一对象
    }
}

// ================================================================
// 享元接口
// ================================================================
interface GoStone {
    void draw(int x, int y);       // 外部状态作参数
}

// ================================================================
// 具体享元
// ----------------------------------------------------------------
// 只存【内部状态】（可共享的、不变的）：color, texture
// 外部状态 x, y 从方法参数来
// ================================================================
class ConcreteGoStone implements GoStone {
    private final String color;
    private final String texture;   // 重量级资源

    public ConcreteGoStone(String color) {
        this.color = color;
        // 模拟加载纹理（重量级）
        this.texture = "[" + color + "色棋子的纹理数据，假装很大]";
        System.out.println("  🎨 为 " + color + " 子加载了纹理（重量级操作）");
    }

    @Override
    public void draw(int x, int y) {
        System.out.println("  在 (" + x + "," + y + ") 画 " + color + " 子");
    }
}

// ================================================================
// 享元工厂：维护对象池
// ================================================================
class GoStoneFactory {
    private static final Map<String, GoStone> pool = new HashMap<>();
    private static int textureLoadCount = 0;

    public static GoStone getStone(String color) {
        // 同色棋子返回同一个对象
        return pool.computeIfAbsent(color, c -> {
            textureLoadCount++;
            return new ConcreteGoStone(c);
        });
    }

    public static int poolSize() {
        return pool.size();
    }

    public static int loadCount() {
        return textureLoadCount;
    }
}

// ================================================================
// 外部状态 + 享元引用（"已放置的棋子"）
// ================================================================
class PlacedStone {
    private final GoStone flyweight;     // 引用享元
    private final int x, y;               // 外部状态

    public PlacedStone(GoStone flyweight, int x, int y) {
        this.flyweight = flyweight;
        this.x = x;
        this.y = y;
    }

    public void draw() {
        flyweight.draw(x, y);
    }
}
