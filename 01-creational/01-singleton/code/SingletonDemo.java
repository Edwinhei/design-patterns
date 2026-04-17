/**
 * ============================================================
 * ✅ 单例模式：5 种经典实现对照
 * ============================================================
 *
 * 同一个"办公室打印机"场景，我们用 5 种方式来实现单例。
 * 跑一下，观察每种实现的特点。
 *
 * 运行方式（需要 JDK 11+）：
 *   java SingletonDemo.java
 *
 * ⚠️ 顺序约定：Java 单文件运行模式（JEP 330）要求"第一个类"是入口，
 *              所以 public class SingletonDemo 放在文件最前，
 *              五种实现类放在下面。阅读时可以先跳到各实现看，再回来看 main。
 */

// ================================================================
// 主程序：把五种实现都跑一遍，对比行为
// ================================================================
public class SingletonDemo {

    public static void main(String[] args) {

        // ---------------- V1 饿汉式 ----------------
        System.out.println("=== V1 · 饿汉式演示 ===");
        EagerPrinter e1 = EagerPrinter.getInstance();
        EagerPrinter e2 = EagerPrinter.getInstance();
        System.out.println("e1 == e2 ? " + (e1 == e2));  // true：真的是同一个
        e1.print("小明", "报告.pdf");
        e2.print("小红", "月报.docx");                     // 注意计数是共享累加的

        // ---------------- V2 懒汉式（不安全）----------------
        System.out.println("\n=== V2 · 懒汉式不安全演示（单线程下看不出问题）===");
        LazyUnsafePrinter l1 = LazyUnsafePrinter.getInstance();
        LazyUnsafePrinter l2 = LazyUnsafePrinter.getInstance();
        System.out.println("单线程下 l1 == l2 ? " + (l1 == l2));  // true，但多线程下就不一定了

        // ---------------- V3 DCL ----------------
        System.out.println("\n=== V3 · 双检锁 DCL 演示 ===");
        DCLPrinter d1 = DCLPrinter.getInstance();
        DCLPrinter d2 = DCLPrinter.getInstance();
        System.out.println("d1 == d2 ? " + (d1 == d2));

        // ---------------- V4 静态内部类 ----------------
        System.out.println("\n=== V4 · 静态内部类 Holder 演示 ===");
        HolderPrinter h1 = HolderPrinter.getInstance();
        HolderPrinter h2 = HolderPrinter.getInstance();
        System.out.println("h1 == h2 ? " + (h1 == h2));

        // ---------------- V5 枚举 ----------------
        System.out.println("\n=== V5 · 枚举演示（实际开发最推荐）===");
        EnumPrinter ep1 = EnumPrinter.INSTANCE;
        EnumPrinter ep2 = EnumPrinter.INSTANCE;   // 两次分别获取，验证唯一性
        ep1.print("小李", "年终奖申请.xlsx");
        ep2.print("老板", "加班通知.pdf");         // 计数跨引用累加，证明同一对象
        System.out.println("ep1 == ep2 ? " + (ep1 == ep2));   // true

        // ---------------- 小结 ----------------
        System.out.println("\n✅ 所有实现都保证了 getInstance() 返回同一对象。");
        System.out.println("   差别在于：线程安全性、懒加载、防攻击能力、代码量。");
    }
}

// ================================================================
// V1 · 饿汉式（Eager Initialization）
// ----------------------------------------------------------------
// 思路：类加载时就创建实例，简单粗暴。
// 优点：线程安全（JVM 类加载机制保证）、实现最简
// 缺点：即使不用也会占内存
// 推荐度：★★★★★（大多数场景用这个就够了）
// ================================================================
class EagerPrinter {
    // 类加载的瞬间就创建，且 final 保证不被重新赋值
    private static final EagerPrinter INSTANCE = new EagerPrinter();

    // 🔒 构造器私有，外部无法 new
    private EagerPrinter() {
        System.out.println("🖨  [V1 饿汉式] 类加载时创建，只此一次");
    }

    // 🚪 全局访问点
    public static EagerPrinter getInstance() {
        return INSTANCE;
    }

    private int count = 0;

    public void print(String user, String doc) {
        count++;
        System.out.println("  ↳ " + user + " 打印了：" + doc
                + "（全司已打印 " + count + " 份）");
    }
}

// ================================================================
// V2 · 懒汉式（线程不安全版）—— ⚠️ 反面教材
// ----------------------------------------------------------------
// 思路：第一次用到才创建
// 致命缺陷：多线程同时调用 getInstance()，可能创建多个实例！
// 推荐度：☆☆☆☆☆（只用来讲课，生产环境别用）
// ================================================================
class LazyUnsafePrinter {
    private static LazyUnsafePrinter instance;  // ⚠️ 没有 volatile，没有锁

    private LazyUnsafePrinter() {
        System.out.println("🖨  [V2 懒汉不安全] 被创建了一次"
                + "（多线程下可能被创建多次！）");
    }

    public static LazyUnsafePrinter getInstance() {
        // 🚨 线程 A 和线程 B 可能同时进入这个 if
        if (instance == null) {
            // 🚨 然后各自 new 一个，结果是两个实例 → 单例失败
            instance = new LazyUnsafePrinter();
        }
        return instance;
    }
}

// ================================================================
// V3 · 双检锁 DCL（Double-Checked Locking）
// ----------------------------------------------------------------
// 思路：懒加载 + 线程安全 + 高性能（只在第一次初始化时加锁）
// 关键：volatile **绝对不能省**！（下面注释解释为什么）
// 推荐度：★★★★（懒加载 + 创建昂贵时用）
// ================================================================
class DCLPrinter {
    // 👇 volatile 禁止指令重排，防止别的线程拿到"半初始化"对象
    private static volatile DCLPrinter instance;

    private DCLPrinter() {
        System.out.println("🖨  [V3 DCL 双检锁] 线程安全的懒加载");
    }

    public static DCLPrinter getInstance() {
        // 第一次检查：没有锁，快路径
        if (instance == null) {
            // 只有当需要创建时才进入同步块
            synchronized (DCLPrinter.class) {
                // 第二次检查：加锁后再确认，避免被别的线程抢先
                if (instance == null) {
                    // ⚠️ new 操作其实分三步（分配内存 / 调构造器 / 赋值），
                    //    JVM 可能重排成（分配内存 / 赋值 / 调构造器）。
                    //    如果没 volatile，另一个线程可能拿到一个"还没调
                    //    构造器"的对象，出 NPE 或脏数据。
                    instance = new DCLPrinter();
                }
            }
        }
        return instance;
    }
}

// ================================================================
// V4 · 静态内部类（Initialization-on-demand Holder）
// ----------------------------------------------------------------
// 思路：利用 JVM "类加载过程本身就是线程安全"的特性
// 细节：内部类 Holder 只在第一次被引用时加载 → 懒加载
//      类加载又是线程安全的 → 不需要 synchronized
// 推荐度：★★★★★（很多人觉得这个最优雅）
// ================================================================
class HolderPrinter {
    private HolderPrinter() {
        System.out.println("🖨  [V4 静态内部类] 优雅的懒加载 + 线程安全");
    }

    // 外部类加载时，这个内部类并不会被加载。
    // 只有调用 getInstance() → 引用 Holder.INSTANCE 时才会触发加载。
    private static class Holder {
        private static final HolderPrinter INSTANCE = new HolderPrinter();
    }

    public static HolderPrinter getInstance() {
        return Holder.INSTANCE;
    }
}

// ================================================================
// V5 · 枚举（Joshua Bloch 在《Effective Java》中推荐）
// ----------------------------------------------------------------
// 思路：枚举常量由 JVM 保证唯一
// 天生优点：
//   ✅ 线程安全
//   ✅ 防反射攻击（反射 new 枚举会抛异常）
//   ✅ 防序列化破坏（反序列化默认创建新对象，枚举例外）
//   ✅ 代码最少
// 推荐度：★★★★★（想"一劳永逸安全"就选它）
// ================================================================
enum EnumPrinter {
    INSTANCE;  // 整个 JVM 生命周期内只有这一个

    private int count = 0;

    EnumPrinter() {
        System.out.println("🖨  [V5 枚举] 最安全的单例实现");
    }

    public void print(String user, String doc) {
        count++;
        System.out.println("  ↳ " + user + " 打印了：" + doc
                + "（全司已打印 " + count + " 份）");
    }
}
