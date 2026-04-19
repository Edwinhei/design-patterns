/**
 * ============================================================
 * ✅ 备忘录模式 · 游戏存档 / 读档
 * ============================================================
 *
 * 三个角色：
 *   Originator (GameCharacter)：产出/恢复自己的状态
 *   Memento   (内部类)         ：保存状态快照（不可变）
 *   Caretaker (SaveSlots)      ：管理多个存档
 *
 * 运行方式：
 *   java MementoDemo.java
 */

import java.util.*;

public class MementoDemo {

    public static void main(String[] args) {
        GameCharacter hero = new GameCharacter();
        SaveSlots slots = new SaveSlots();

        // === 场景 1：初始状态 + 存档 ===
        hero.updateStatus(1, 100, "新手村");
        System.out.println("初始: " + hero);

        slots.save(hero.save());      // 💾 存档 1
        System.out.println("  💾 保存存档 #1");

        // === 场景 2：升级打怪 + 存档 ===
        hero.updateStatus(10, 80, "魔法森林");
        System.out.println("\n升级后: " + hero);
        slots.save(hero.save());      // 💾 存档 2
        System.out.println("  💾 保存存档 #2");

        // === 场景 3：继续冒险 + 存档 ===
        hero.updateStatus(20, 50, "恶龙洞穴");
        System.out.println("\n继续: " + hero);
        slots.save(hero.save());      // 💾 存档 3
        System.out.println("  💾 保存存档 #3");

        // === 场景 4：挑战 Boss 失败，HP=0 ===
        hero.updateStatus(20, 0, "恶龙洞穴");
        System.out.println("\n💀 被 Boss 秒杀: " + hero);

        // === 场景 5：读档（回到存档 3）===
        System.out.println("\n--- 读档 #3 ---");
        hero.restore(slots.load());
        System.out.println("恢复后: " + hero);

        // === 场景 6：再读档（回到存档 2）===
        System.out.println("\n--- 读档 #2 ---");
        hero.restore(slots.load());
        System.out.println("恢复后: " + hero);

        // === 场景 7：再读档（回到存档 1）===
        System.out.println("\n--- 读档 #1 ---");
        hero.restore(slots.load());
        System.out.println("恢复后: " + hero);

        System.out.println("\n✨ 备忘录模式的威力：");
        System.out.println("  ① 封装完好：Character 字段 private，但能被保存/恢复");
        System.out.println("  ② Caretaker (SaveSlots) 只管存取 Memento，不看内部");
        System.out.println("  ③ 可存多份存档，任意回溯");
        System.out.println("  ④ Memento 不可变（final 字段），安全共享");
    }
}

// ================================================================
// Originator：游戏角色
// ================================================================
class GameCharacter {
    private int level;
    private int hp;
    private String location;

    public void updateStatus(int level, int hp, String location) {
        this.level = level;
        this.hp = hp;
        this.location = location;
    }

    // 保存状态
    public Memento save() {
        return new Memento(level, hp, location);
    }

    // 从 Memento 恢复
    public void restore(Memento m) {
        if (m == null) return;
        this.level = m.level;
        this.hp = m.hp;
        this.location = m.location;
    }

    @Override
    public String toString() {
        return "Lv." + level + " HP=" + hp + " @ " + location;
    }

    // ================================================================
    // Memento：作为 GameCharacter 的静态内部类
    // ----------------------------------------------------------------
    // 关键：字段 final 不可变，构造器本应 private，
    //      但 Java 同外部类内可互访，所以 default 也行
    // ================================================================
    public static class Memento {
        private final int level;
        private final int hp;
        private final String location;

        private Memento(int level, int hp, String location) {
            this.level = level;
            this.hp = hp;
            this.location = location;
        }
    }
}

// ================================================================
// Caretaker：存档管理
// ================================================================
class SaveSlots {
    private final Deque<GameCharacter.Memento> saves = new ArrayDeque<>();

    public void save(GameCharacter.Memento m) {
        saves.push(m);
    }

    public GameCharacter.Memento load() {
        return saves.isEmpty() ? null : saves.pop();
    }

    public int count() {
        return saves.size();
    }
}
