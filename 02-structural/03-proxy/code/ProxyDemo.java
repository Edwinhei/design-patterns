/**
 * ============================================================
 * ✅ 代理模式 Proxy · 静态代理 + JDK 动态代理
 * ============================================================
 *
 * 场景：经纪人代理明星
 *   - RealCelebrity 只管业务（唱歌、出席）
 *   - 经纪人代理处理横切：筛选客户、记录时间、结算
 *
 * 两种实现对比：
 *   V1 · 静态代理  —— 手写 Manager 类（一对一）
 *   V2 · JDK 动态代理 —— 一个 Handler 通吃所有接口
 *
 * 运行方式：
 *   java ProxyDemo.java
 */

import java.lang.reflect.*;

public class ProxyDemo {

    public static void main(String[] args) {

        RealCelebrity real = new RealCelebrity("周杰伦");

        // ===== V1 静态代理 =====
        System.out.println("========== V1 静态代理（手写经纪人）==========\n");
        Celebrity staticProxy = new CelebrityManager(real);
        staticProxy.sing("稻香");
        System.out.println();
        staticProxy.attend("商业演出", 500_000);
        System.out.println();
        staticProxy.attend("小婚礼", 30_000);

        // ===== V2 JDK 动态代理 =====
        System.out.println("\n\n========== V2 JDK 动态代理（运行时生成）==========\n");
        Celebrity dynamicProxy = (Celebrity) Proxy.newProxyInstance(
                Celebrity.class.getClassLoader(),
                new Class<?>[] { Celebrity.class },
                new LoggingHandler(real)
        );

        // 看看生成的代理类名字
        System.out.println("💡 代理对象的 class: " + dynamicProxy.getClass().getName());
        System.out.println();

        dynamicProxy.sing("七里香");
        System.out.println();
        dynamicProxy.attend("产品发布会", 1_000_000);

        // ===== 小结 =====
        System.out.println("\n========== ✅ 代理模式总结 ==========");
        System.out.println("静态代理：每个接口 / 业务类 → 手写一个代理类");
        System.out.println("动态代理：一个 InvocationHandler → 通用于任意接口");
        System.out.println("");
        System.out.println("Spring @Transactional / @Async / @Cacheable");
        System.out.println("  → 全部通过动态代理实现（JDK Proxy 或 CGLIB）");
    }
}

// ================================================================
// 接口（目标）
// ================================================================
interface Celebrity {
    void sing(String song);
    void attend(String event, double fee);
}

// ================================================================
// 真明星（只管业务）
// ================================================================
class RealCelebrity implements Celebrity {
    private final String name;

    public RealCelebrity(String name) {
        this.name = name;
    }

    @Override
    public void sing(String song) {
        System.out.println("  🎤 " + name + " 正在演唱: " + song);
        try { Thread.sleep(50); } catch (InterruptedException e) {}
    }

    @Override
    public void attend(String event, double fee) {
        System.out.println("  🎭 " + name + " 正在出席: " + event);
        try { Thread.sleep(50); } catch (InterruptedException e) {}
    }
}

// ================================================================
// V1 · 静态代理：手写 CelebrityManager 类
// ----------------------------------------------------------------
// 特点：
//   ✅ 简单直接，容易理解
//   ❌ 每个接口要写一个代理类 —— 代理类爆炸
// ================================================================
class CelebrityManager implements Celebrity {

    private final RealCelebrity celebrity;
    private static final double MIN_FEE = 100_000;

    public CelebrityManager(RealCelebrity celebrity) {
        this.celebrity = celebrity;
    }

    @Override
    public void sing(String song) {
        System.out.println("📞 [静态代理] 经纪人登记: 唱歌 " + song);
        long start = System.nanoTime();

        celebrity.sing(song);              // 👈 委托给真明星

        long cost = (System.nanoTime() - start) / 1_000_000;
        System.out.println("💰 [静态代理] 结算，耗时 " + cost + "ms");
    }

    @Override
    public void attend(String event, double fee) {
        System.out.println("📞 [静态代理] 经纪人审核: " + event + " ¥" + fee);

        if (fee < MIN_FEE) {
            System.out.println("❌ [静态代理] 报价不够，拒绝");
            return;
        }

        celebrity.attend(event, fee);      // 👈 委托给真明星
        System.out.println("💰 [静态代理] 结算，收取 " + (fee * 0.1) + " 佣金");
    }
}

// ================================================================
// V2 · JDK 动态代理：一个通用 InvocationHandler
// ----------------------------------------------------------------
// 特点：
//   ✅ 一个 Handler 处理所有接口、所有方法
//   ✅ Spring AOP 的核心机制
//   ❌ 只能代理接口（CGLIB 能代理普通类，但 Spring 里都有）
// ================================================================
class LoggingHandler implements InvocationHandler {

    private final Object target;
    private static final double MIN_FEE = 100_000;

    public LoggingHandler(Object target) {
        this.target = target;
    }

    /**
     * 所有方法调用都会进到这里。
     * 这个方法就是"代理做的事"。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("📞 [动态代理] 拦截方法: " + method.getName());

        // 横切关注点：权限检查（按方法名判断）
        if (method.getName().equals("attend") && args.length >= 2) {
            double fee = (double) args[1];
            if (fee < MIN_FEE) {
                System.out.println("❌ [动态代理] 报价不够，拒绝");
                return null;
            }
        }

        long start = System.nanoTime();

        // 调用真对象的方法（反射）
        Object result = method.invoke(target, args);

        long cost = (System.nanoTime() - start) / 1_000_000;
        System.out.println("💰 [动态代理] 结算，" + method.getName() + " 耗时 " + cost + "ms");

        return result;
    }
}
