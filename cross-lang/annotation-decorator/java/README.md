# Java 注解 + 反射 · 示例

## 🎯 这个示例展示了什么

Java 注解**本身不执行任何逻辑**。真正工作的是**运行时反射处理器**。

这个 demo 实现了一个**极简版 Spring AOP**：
- `@LogExecutionTime` 注解：**纯标签**
- `AnnotationProcessor`：**处理器**，用反射读取标签，决定是否包一层耗时统计

## 🚀 运行

```bash
java AnnotationDemo.java
```

**需要**：JDK 11+（单文件运行）

## 📋 预期输出

```
=== 调用 placeOrder（有 @LogExecutionTime）===
  订单已创建
⏱  [下单] OrderService#placeOrder 耗时 120 ms

=== 调用 normalMethod（没打注解）===
  普通方法，不打日志

=== 调用 refund（有注解，带 value）===
  退款处理完成
⏱  [退款] OrderService#refund 耗时 50 ms
```

## 🔍 关键代码解读

### 1. 注解定义

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)    // 👈 必须 RUNTIME
@interface LogExecutionTime {
    String value() default "";
}
```

**关键**：`@Retention(RUNTIME)` 让注解保留到运行时，才能被反射读到。

### 2. 注解处理（反射读取）

```java
if (method.isAnnotationPresent(LogExecutionTime.class)) {
    LogExecutionTime anno = method.getAnnotation(LogExecutionTime.class);
    // ... 包一层耗时统计
}
```

**核心**：反射 API (`isAnnotationPresent` / `getAnnotation`) 是整个机制的工作基础。

## 🔗 扩展阅读

- [../../../java-notes/15 Java 注解体系完全指南](../../../java-notes/15-Java注解体系完全指南.md)
- Spring 的 `@Transactional` 就是类似机制 + 动态代理实现的

## 🎯 对比其他语言

看完这个，再看：
- [../python-native/](../python-native/) —— Python 装饰器如何用**函数包装**做到类似效果
- [../go/](../go/) —— Go 用**字符串标签**做元数据
