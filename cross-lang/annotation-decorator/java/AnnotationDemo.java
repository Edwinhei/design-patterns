/**
 * ============================================================
 * Java 注解 + 运行时反射 · 可运行示例
 * ============================================================
 *
 * 场景：实现一个 @LogExecutionTime 注解，打在方法上后，
 *       通过反射处理器自动给方法包一层耗时统计。
 *
 * 这是 Spring AOP 最朴素的"手写版"实现。
 *
 * 运行方式（JDK 11+）：
 *   java AnnotationDemo.java
 */

import java.lang.annotation.*;
import java.lang.reflect.Method;

public class AnnotationDemo {

    public static void main(String[] args) throws Exception {

        OrderService service = new OrderService();

        System.out.println("=== 调用 placeOrder（有 @LogExecutionTime）===");
        AnnotationProcessor.invokeWithLogging(service, "placeOrder");

        System.out.println("\n=== 调用 normalMethod（没打注解）===");
        AnnotationProcessor.invokeWithLogging(service, "normalMethod");

        System.out.println("\n=== 调用 refund（有注解，带 value）===");
        AnnotationProcessor.invokeWithLogging(service, "refund");

        System.out.println("\n✅ 原理小结：");
        System.out.println("   ① @LogExecutionTime 只是元数据标签，不执行任何逻辑");
        System.out.println("   ② AnnotationProcessor 通过反射读到注解 → 自己包装了耗时统计");
        System.out.println("   ③ 没打注解的方法正常调用，没额外开销");
    }
}

// ================================================================
// 定义注解
// ================================================================
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)    // 必须 RUNTIME 才能反射读到
@interface LogExecutionTime {
    String value() default "";
}

// ================================================================
// 业务类
// ================================================================
class OrderService {

    @LogExecutionTime("下单")
    public void placeOrder() {
        try { Thread.sleep(120); } catch (InterruptedException e) {}
        System.out.println("  订单已创建");
    }

    // 没打注解，不会有日志
    public void normalMethod() {
        System.out.println("  普通方法，不打日志");
    }

    @LogExecutionTime("退款")
    public void refund() {
        try { Thread.sleep(50); } catch (InterruptedException e) {}
        System.out.println("  退款处理完成");
    }
}

// ================================================================
// 注解处理器（运行时反射）
// 相当于 Spring AOP 的极简版
// ================================================================
class AnnotationProcessor {

    public static Object invokeWithLogging(Object target, String methodName, Object... args)
            throws Exception {

        Method method = target.getClass().getMethod(methodName);

        // 检查方法上是否有 @LogExecutionTime 注解
        if (method.isAnnotationPresent(LogExecutionTime.class)) {
            LogExecutionTime anno = method.getAnnotation(LogExecutionTime.class);
            String tag = anno.value().isEmpty() ? method.getName() : anno.value();

            long start = System.nanoTime();
            Object result = method.invoke(target, args);       // 反射调用
            long costMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("⏱  [%s] %s#%s 耗时 %d ms%n",
                    tag, target.getClass().getSimpleName(), methodName, costMs);
            return result;
        }

        // 没打注解，正常调用
        return method.invoke(target, args);
    }
}
