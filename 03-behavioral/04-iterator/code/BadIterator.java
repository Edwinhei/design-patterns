/**
 * ============================================================
 * 🚫 不用迭代器 · 客户端要懂内部结构
 * ============================================================
 *
 * 运行方式：
 *   java BadIterator.java
 */

public class BadIterator {

    public static void main(String[] args) {
        System.out.println("=== 土办法：客户端直接访问数组 ===\n");

        NaivePlaylist playlist = new NaivePlaylist();
        playlist.songs = new Song[] {
                new Song("稻香", 245),
                new Song("七里香", 280),
                new Song("告白气球", 215)
        };

        // 🚨 客户端必须知道"内部是数组"
        for (int i = 0; i < playlist.songs.length; i++) {
            Song song = playlist.songs[i];
            System.out.println("  🎵 " + song.name + " (" + song.duration + "s)");
        }

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 客户端依赖【内部是数组】");
        System.out.println("2. 换成 List/LinkedList/ResultSet → 所有客户端代码都要改");
        System.out.println("3. 暴露内部结构 → 破坏封装");
        System.out.println("4. 不同集合的遍历方式不统一");

        System.out.println("\n👉 看 IteratorDemo.java 如何用 Iterator 统一遍历");
    }
}

class NaivePlaylist {
    public Song[] songs;         // 🚨 public 暴露
}

class Song {
    String name;
    int duration;

    Song(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }
}
