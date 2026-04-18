/**
 * ============================================================
 * 🚫 不用享元 · 每个对象独立占内存
 * ============================================================
 *
 * 场景：围棋棋盘上 200 颗棋子
 * 土办法：每颗棋子一个对象，每个都存重复的 color / texture
 *
 * 运行方式：
 *   java BadFlyweight.java
 */

import java.util.*;

public class BadFlyweight {

    public static void main(String[] args) {
        System.out.println("=== 土办法：每颗棋子一个对象 ===\n");

        List<SimpleGoStone> stones = new ArrayList<>();
        Random random = new Random();

        // 放 200 颗棋子
        for (int i = 0; i < 200; i++) {
            String color = random.nextBoolean() ? "黑" : "白";
            int x = random.nextInt(19);
            int y = random.nextInt(19);
            stones.add(new SimpleGoStone(color, "纹理数据(重量级)", x, y));
        }

        System.out.println("棋盘上棋子数: " + stones.size());
        System.out.println("内存中对象数: " + stones.size() + " （每颗一个）");
        System.out.println("每个对象都存:");
        System.out.println("  - color 字符串（\"黑\" or \"白\"）");
        System.out.println("  - texture 纹理数据（重量级）");
        System.out.println("  - x, y 位置");

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 200 颗棋子 = 200 个对象");
        System.out.println("2. 每个对象都重复存 color + texture");
        System.out.println("3. 如果是游戏里的【10000 棵树】？内存爆炸");
        System.out.println("4. 大部分字段都是一样的，纯属浪费");

        System.out.println("\n👉 看 FlyweightDemo.java 如何用享元模式优化");
    }
}

// ================================================================
// 棋子类（每颗独立）
// ================================================================
class SimpleGoStone {
    String color;
    String texture;       // 假设是重量级的纹理数据
    int x, y;

    public SimpleGoStone(String color, String texture, int x, int y) {
        this.color = color;
        this.texture = texture;
        this.x = x;
        this.y = y;
    }
}
