/**
 * ============================================================
 * 配套 java-notes/20 · InvocationHandler 设计推演
 * 最小可运行的日志代理示例
 * ============================================================
 *
 * 只用一个 InvocationHandler + 一行 Proxy.newProxyInstance，
 * 就给任意接口方法包装了"前后打日志"的能力。
 *
 * 运行方式（JDK 11+）：
 *   java InvocationHandlerDemo.java
 */

import java.lang.reflect.*;
import java.util.Arrays;

public class InvocationHandlerDemo {

    public static void main(String[] args) {

        // 1️⃣ 准备真对象
        Greeter real = new RealGreeter();

        // 2️⃣ 写你的 InvocationHandler（核心）
        InvocationHandler loggingHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 方法调用前
                System.out.println("📝 [日志] 准备调用: " + method.getName()
                        + (args != null ? " 参数=" + Arrays.toString(args) : ""));

                // 调真对象的那个方法
                Object result = method.invoke(real, args);

                // 方法调用后
                System.out.println("📝 [日志] 调用完毕，返回: " + result);

                return result;
            }
        };

        // 3️⃣ 用 Proxy 工厂创建代理
        Greeter proxy = (Greeter) Proxy.newProxyInstance(
                Greeter.class.getClassLoader(),      // 21 号笔记详解
                new Class<?>[] { Greeter.class },    // 21 号笔记详解
                loggingHandler                         // 你的 InvocationHandler
        );

        // 4️⃣ 用代理调方法
        System.out.println("=== 第一次调用 ===");
        proxy.sayHello("张三");

        System.out.println("\n=== 第二次调用 ===");
        proxy.sayHello("李四");

        System.out.println("\n=== 调用另一个方法 ===");
        String desc = proxy.describe();
        System.out.println("拿到描述: " + desc);

        System.out.println("\n✅ 观察要点：");
        System.out.println("  ① 你只写了一个 InvocationHandler");
        System.out.println("  ② sayHello / describe 都被同一个 handler 拦截了");
        System.out.println("  ③ 客户端调用方式和调真对象完全一样");
        System.out.println("  ④ 想去掉日志？只改 handler，不改客户端");
    }
}

// ================================================================
// 接口
// ================================================================
interface Greeter {
    void sayHello(String name);
    String describe();
}

// ================================================================
// 真实实现
// ================================================================
class RealGreeter implements Greeter {
    @Override
    public void sayHello(String name) {
        System.out.println("  👋 Hello, " + name);
    }

    @Override
    public String describe() {
        return "我是打招呼者";
    }
}
