/**
 * ============================================================
 * ✅ 迭代器模式 Iterator · 音乐播放器歌单
 * ============================================================
 *
 * 设计：
 *   - Playlist implements Iterable<Song>
 *   - 提供 iterator() 返回 Iterator<Song>
 *   - 客户端用 for-each 或手动 while 遍历
 *
 * 运行方式：
 *   java IteratorDemo.java
 */

import java.util.*;

public class IteratorDemo {

    public static void main(String[] args) {
        Playlist playlist = new Playlist();
        playlist.add(new Song("稻香", 245));
        playlist.add(new Song("七里香", 280));
        playlist.add(new Song("告白气球", 215));

        // === 方式 1：for-each（for-each 是 Iterator 的语法糖）===
        System.out.println("===== 方式 1：for-each（自动调 iterator）=====");
        for (Song s : playlist) {
            System.out.println("  🎵 " + s.getName() + " (" + s.getDuration() + "s)");
        }

        // === 方式 2：手动用 Iterator ===
        System.out.println("\n===== 方式 2：手动 Iterator（for-each 的真相）=====");
        Iterator<Song> it = playlist.iterator();
        while (it.hasNext()) {
            Song s = it.next();
            System.out.println("  🎵 " + s.getName());
        }

        // === 方式 3：Stream（Java 8+ 函数式）===
        System.out.println("\n===== 方式 3：Stream（函数式，底层还是迭代器）=====");
        playlist.stream()
                .filter(s -> s.getDuration() > 230)
                .forEach(s -> System.out.println("  🎵 长歌: " + s.getName()));

        // === 关键 ===
        System.out.println("\n✨ 观察：");
        System.out.println("  ① 客户端代码完全不知道内部是数组还是链表");
        System.out.println("  ② for-each 底层就是 iterator() + while(hasNext)");
        System.out.println("  ③ JDK 所有集合都 implements Iterable");
        System.out.println("  ④ Stream 底层依然是迭代器，只是加了函数式包装");
    }
}

// ================================================================
// 歌曲
// ================================================================
class Song {
    private final String name;
    private final int duration;

    public Song(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() { return name; }
    public int getDuration() { return duration; }
}

// ================================================================
// 歌单 implements Iterable<Song>
// ================================================================
class Playlist implements Iterable<Song> {
    private final List<Song> songs = new ArrayList<>();

    public void add(Song song) {
        songs.add(song);
    }

    @Override
    public Iterator<Song> iterator() {
        // 返回一个匿名 Iterator
        return new Iterator<Song>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < songs.size();
            }

            @Override
            public Song next() {
                if (!hasNext()) throw new NoSuchElementException();
                return songs.get(index++);
            }
        };
    }

    // 方便使用 Stream API
    public java.util.stream.Stream<Song> stream() {
        return songs.stream();
    }
}
