# 动态代理（二）· Proxy 工厂 & 完整流程 & 实战

> 📍 承接 [20 号 InvocationHandler 设计推演](./20-动态代理一-InvocationHandler设计推演.md)
> 🎯 本篇目标：讲 **Proxy 工厂类** + **完整调用流程** + **可运行的重试代理实战**
> 📖 前置：理解 InvocationHandler 接口（20 号笔记）

---

## 🧠 前情提要

- 18：反射 + Class
- 19：Method / Field / Constructor
- 20：InvocationHandler 接口（**用户写的拦截器**）

现在只差一步：**怎么让 JDK 生成代理类？** —— 用 `Proxy` 工厂。

---

## 🏭 Proxy 类

`java.lang.reflect.Proxy` —— JDK 提供的**代理工厂工具类**。

**最重要的方法**：

```java
public static Object newProxyInstance(
    ClassLoader loader,
    Class<?>[] interfaces,
    InvocationHandler h
)
```

## 三个参数逐个讲

### 参数 1：`ClassLoader loader`

JDK 要**动态生成一个新类的字节码**（就是那个 `$Proxy0`），这个新类需要被**加载**到 JVM 才能用。加载需要 ClassLoader。

**固定写法**：`YourInterface.class.getClassLoader()`

**不用理解太深**，记住这个写法就行。

### 参数 2：`Class<?>[] interfaces`

生成的代理类要**实现哪些接口**。

```java
new Class<?>[] { Celebrity.class }
// 生成的代理类 implements Celebrity
```

决定了**代理对象的"身份"** —— 能把代理强转成什么类型。

### 参数 3：`InvocationHandler h`

**你写的拦截器**（20 号笔记讲的那个）。

代理对象收到任何方法调用都转发给它的 `invoke`。

---

## 🔬 Proxy.newProxyInstance 内部做了什么

```
调用 Proxy.newProxyInstance(loader, [Celebrity.class], handler)
    ↓
Proxy 内部执行：
  ① 根据 [Celebrity.class]，动态生成一个新类的字节码：
     class $Proxy0 implements Celebrity {
         private InvocationHandler h;

         public $Proxy0(InvocationHandler h) { this.h = h; }

         public void sing(String song) {
             Method m = 查找 Celebrity.sing 的 Method;
             h.invoke(this, m, new Object[]{song});
         }

         public void attend(String event, double fee) {
             Method m = 查找 Celebrity.attend 的 Method;
             h.invoke(this, m, new Object[]{event, fee});
         }
     }
    ↓
  ② 用 ClassLoader 加载这个 $Proxy0 类到 JVM
    ↓
  ③ new $Proxy0(handler) 得到实例
    ↓
  ④ 返回给你
```

**你拿到的代理对象**：
- 类型是 `$Proxy0`（运行时生成的类）
- implements 你指定的接口
- 任何方法调用都转发到 `handler.invoke`

---

## 🎬 完整调用流程（从头到尾）

```java
import java.lang.reflect.*;

interface Celebrity {
    void sing(String song);
}

class RealCelebrity implements Celebrity {
    @Override
    public void sing(String song) {
        System.out.println("🎤 演唱: " + song);
    }
}

public class Demo {
    public static void main(String[] args) {
        // 1️⃣ 准备真对象
        Celebrity real = new RealCelebrity();

        // 2️⃣ 写 InvocationHandler
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("📞 前置: " + method.getName());
                Object result = method.invoke(real, args);   // 调真对象
                System.out.println("💰 后置");
                return result;
            }
        };

        // 3️⃣ 用 Proxy 工厂生成代理
        Celebrity proxy = (Celebrity) Proxy.newProxyInstance(
            Celebrity.class.getClassLoader(),
            new Class<?>[] { Celebrity.class },
            handler
        );

        // 4️⃣ 使用代理
        proxy.sing("稻香");
    }
}
```

### 调用链路（逐步走）

```
main 里: proxy.sing("稻香")
    ↓
proxy 的真实类型是 $Proxy0（JDK 动态生成的）
    ↓
$Proxy0.sing 方法内部执行：
    Method m = 查找 Celebrity.sing 的 Method;
    handler.invoke(this, m, new Object[]{"稻香"});
    ↓
进入你写的 handler.invoke：
    proxy  = $Proxy0 对象
    method = Celebrity.sing 的 Method 对象
    args   = ["稻香"]
    ↓
invoke 内部执行：
    println("📞 前置: sing")
    ↓
    method.invoke(real, args)
        = method.invoke(real, ["稻香"])
        = 反射调 real.sing("稻香")
        = 周杰伦真的唱了（输出 "🎤 演唱: 稻香"）
    ↓
    println("💰 后置")
    ↓
    return null (sing 是 void)
```

**输出**：
```
📞 前置: sing
🎤 演唱: 稻香
💰 后置
```

---

## 🧪 实战：写一个"失败自动重试"代理

**需求**：给任意接口方法加上"调用失败自动重试 3 次"的能力。

```java
import java.lang.reflect.*;

// 示例接口
interface DataService {
    String fetchData();
}

// 模拟实现（前两次会失败）
class FlakyDataService implements DataService {
    private int callCount = 0;

    @Override
    public String fetchData() {
        callCount++;
        if (callCount < 3) {
            throw new RuntimeException("网络错误！第 " + callCount + " 次");
        }
        return "数据: OK";
    }
}

// 重试 InvocationHandler
class RetryHandler implements InvocationHandler {
    private final Object target;
    private final int maxRetries;

    public RetryHandler(Object target, int maxRetries) {
        this.target = target;
        this.maxRetries = maxRetries;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (int i = 1; i <= maxRetries; i++) {
            try {
                System.out.println("[尝试 " + i + "/" + maxRetries + "] " + method.getName());
                return method.invoke(target, args);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                System.out.println("  ❌ 失败: " + cause.getMessage());
                if (i == maxRetries) throw cause;
            }
        }
        throw new RuntimeException("不应到这里");
    }
}

public class RetryDemo {
    public static void main(String[] args) {
        DataService real = new FlakyDataService();

        DataService proxy = (DataService) Proxy.newProxyInstance(
            DataService.class.getClassLoader(),
            new Class<?>[] { DataService.class },
            new RetryHandler(real, 3)
        );

        String data = proxy.fetchData();
        System.out.println("✅ 最终结果: " + data);
    }
}
```

### 预期输出
```
[尝试 1/3] fetchData
  ❌ 失败: 网络错误！第 1 次
[尝试 2/3] fetchData
  ❌ 失败: 网络错误！第 2 次
[尝试 3/3] fetchData
✅ 最终结果: 数据: OK
```

**一个 `RetryHandler` 可以给任何接口加重试能力** —— 这就是动态代理的威力。

---

## 🎯 Spring AOP 原来就是这样

```java
@Service
public class UserService {
    @Transactional
    public void save(User u) { ... }
}
```

**Spring 启动时做的事**（和上面你手写的重试代理一模一样）：

```
① Spring 扫描 @Service → 发现 UserService
② 发现它的方法有 @Transactional
③ Spring 写了一个 TransactionHandler implements InvocationHandler {
       public Object invoke(..., Method method, Object[] args) {
           beginTransaction();
           try {
               Object r = method.invoke(realUserService, args);
               commit();
               return r;
           } catch (Exception e) {
               rollback();
               throw e;
           }
       }
   }
④ 用 Proxy.newProxyInstance 生成代理对象
⑤ Spring 容器里【存的是代理对象】
⑥ 你 @Autowired 时注入的其实是代理
```

**你调 `userService.save(u)` → 走代理 → 先开事务 → 再调真方法 → 提交**。

**不需要一点配置**，Spring 帮你做了这一切。

---

## ⚠️ JDK 动态代理的限制

### 只能代理接口

```java
Proxy.newProxyInstance(..., new Class<?>[] { XXX.class }, ...);
//                               ↑
//                   必须是接口，不能是普通类
```

**代理普通类要用 CGLIB / ByteBuddy**（字节码增强，通过继承实现）。

**Spring AOP 的选择规则**：
- 目标类实现了接口 → 用 JDK 动态代理
- 目标类没实现接口 → 用 CGLIB 继承它

---

## 🌍 真实应用再梳理

| 工具 | 动态代理做什么 |
|------|-------------|
| Spring `@Transactional` | 代理包装 bean，拦截方法加事务 |
| Spring `@Async` | 代理转异步执行 |
| Spring `@Cacheable` | 代理查缓存，未命中才调真方法 |
| Spring Data `@Repository` | 接口无实现类，代理生成 CRUD |
| MyBatis `@Mapper` | 接口无实现类，代理把调用翻译成 SQL |
| Mockito | mock 对象就是动态代理 |
| Retrofit `@GET` | 代理把接口调用翻译成 HTTP 请求 |

**全是同一套机制的应用**。

---

## 📌 本篇一句话总结

> **`Proxy.newProxyInstance(loader, interfaces, handler)` 是动态代理的工厂**。
>
> - 参数 1：ClassLoader（用接口的即可）
> - 参数 2：代理类要实现的接口（决定代理的"身份"）
> - 参数 3：InvocationHandler（你的拦截器）
>
> **内部**动态生成 `$Proxy0 implements 接口` 字节码，加载，new 一个实例返回。
>
> **你调代理对象任何方法** → 转发到 handler.invoke → 你在 invoke 里写逻辑（通常会 `method.invoke(target, args)` 调真对象）。
>
> **Spring AOP / MyBatis Mapper / Mockito 全是这套机制**。

---

## 🎓 反射 & 动态代理系列完结

- [18 反射入门](./18-反射入门-什么是反射与Class类.md) · Class 类
- [19 反射进阶](./19-反射进阶-Method与Field.md) · Method / Field / Constructor
- [20 动态代理（一）](./20-动态代理一-InvocationHandler设计推演.md) · InvocationHandler 设计推演
- **21 动态代理（二）· 本篇** · Proxy 工厂 + 实战

四篇合起来，你应该**彻底理解**了 Java 动态代理的所有细节。

---

## 🔗 接下来

- 设计模式第 2 阶段继续：组合 Composite / 装饰器 Decorator 等
- **如果你想深入 CGLIB / ByteBuddy** → 以后可以单开一篇
