/**
 * ============================================================
 * 🚫 不用备忘录 · 暴露字段手动保存
 * ============================================================
 *
 * 运行方式：
 *   java BadMemento.java
 */

public class BadMemento {

    public static void main(String[] args) {
        System.out.println("=== 土办法：暴露字段让外部手动保存 ===\n");

        NaiveCharacter hero = new NaiveCharacter();
        hero.level = 10;
        hero.hp = 100;
        hero.location = "村庄";

        System.out.println("初始: " + hero);

        // 🚨 外部手动保存
        int savedLevel = hero.level;
        int savedHp = hero.hp;
        String savedLocation = hero.location;

        // 玩家继续：升级打怪
        hero.level = 15;
        hero.hp = 0;
        hero.location = "坟场";
        System.out.println("挂了: " + hero);

        // 🚨 外部手动恢复
        hero.level = savedLevel;
        hero.hp = savedHp;
        hero.location = savedLocation;
        System.out.println("读档: " + hero);

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 字段必须 public → 破坏封装");
        System.out.println("2. 加新字段（经验值）→ 所有保存/恢复代码都要改");
        System.out.println("3. 存档逻辑散落在调用方");
        System.out.println("4. 无法存【多份】存档（只有几个变量）");

        System.out.println("\n👉 看 MementoDemo.java 如何优雅保存/恢复");
    }
}

class NaiveCharacter {
    public int level;
    public int hp;
    public String location;

    @Override
    public String toString() {
        return "Lv." + level + " HP=" + hp + " @ " + location;
    }
}
