# 动态代理（一）· InvocationHandler 设计推演

> 📍 承接 [19 号反射进阶](./19-反射进阶-Method与Field.md)
> 🎯 本篇目标：用"**设计推演**"的方式，让你理解 **为什么 InvocationHandler 长这样**
> 📖 前置：读完 18 + 19，理解 Class / Method / Field

---

## 🎯 场景：假设你是 JDK 设计者

你的任务：**给 Java 添加"动态代理"功能**，让用户能做类似 Spring AOP 那种"在真方法前后包一层"的事。

我们一步步推演**怎么设计**，为什么最后就是 `InvocationHandler` 长这样。

---

## 🤔 第一关：代理类要实现什么接口？**你不知道**

用户千千万，每个人想代理的接口都不同：

- 用户 A 想代理 `Celebrity`
- 用户 B 想代理 `Database`
- 用户 C 想代理 `UserService`
- 用户 D 想代理一个明天才发明的接口

**JDK 不可能预先知道所有接口**。

### 推出结论 1

代理类必须**运行时动态生成**。
用户告诉你"我要代理 Celebrity" → JDK 临时生成一个 `class $Proxy0 implements Celebrity`。

---

## 🤔 第二关：代理类方法里写什么？**你也不知道**

你动态生成了 `$Proxy0 implements Celebrity`。里面的 `sing(String)` 方法**该写什么逻辑**？

不同用户需求不同：

| 用户 | 想在 sing 里做 |
|------|--------------|
| 日志用户 | 打印日志 + 调真方法 |
| 事务用户 | 开事务 + 调真方法 + 提交 |
| 权限用户 | 检查权限 + 调真方法 |
| 缓存用户 | 查缓存 + 未命中才调真 |

**JDK 不可能预测**。

### 推出结论 2

代理方法的逻辑**必须由用户自己写**。JDK 只提供"**钩子**"让用户把逻辑塞进来。

---

## 🤔 第三关：用户怎么"把逻辑给你"？

需要一个"约定"让用户传逻辑。两个选择：

### 方案 A：让用户继承一个抽象类？

```java
abstract class ProxyBase {
    abstract Object doSomething(...);
}

// 用户继承
class MyProxy extends ProxyBase { ... }
```

**问题**：Java **单继承**。如果用户想代理的目标类已经有父类，就用不了这个方案了。

### 方案 B：让用户实现一个接口？✅

```java
interface InvocationHandler {
    ...
}

class MyHandler implements InvocationHandler { ... }
```

**优势**：
- Java 可以实现多个接口，不冲突
- 可以用**匿名类 / Lambda** 简写，灵活

### 推出结论 3

**用接口**。

---

## 🤔 第四关：接口里放什么方法？

**关键问题**：代理对象可能实现很多个方法（`sing` / `attend` / `eat`...），用户要怎么处理所有这些方法？

### 选择 A：为每个方法写一个 handler？

```java
// 假设的设计（不可行）
interface MyHandler {
    Object onSing(String song);
    Object onAttend(String event, double fee);
    Object onEat(String food);
    // ... 永远写不完
}
```

**问题**：
- 用户得**提前写好所有方法**
- 接口变了用户要改
- Handler **绑死一个接口**，不能通用

### 选择 B：把所有方法调用"折叠"成一个 invoke？✅

```java
interface InvocationHandler {
    Object invoke(...);   // 任何方法调用都进这一个口子
}
```

用户只写**一个方法**，处理**所有方法调用**。

### 类比：HTTP 服务器的"路由分发"

```
HTTP 服务器收到任何请求
        ↓
进入一个统一入口 handle(request, response)
        ↓
handle 里根据 URL 决定做什么

动态代理收到任何方法调用
        ↓
进入一个统一入口 invoke(method, args)
        ↓
invoke 里根据 method 决定做什么
```

**同一种"通用钩子"模式**。

### 推出结论 4

接口里只放**一个 `invoke` 方法**。

---

## 🤔 第五关：invoke 方法需要什么参数？

用户在 invoke 里要写代理逻辑。**它需要知道什么信息**？

### 信息 1：**被调的是哪个方法？**（最重要）

代理对象有多个方法。当代理被调用时，**必须告诉用户"刚才是 sing 被调了还是 attend 被调了"**。

怎么传这个信息？——**用 Method 对象**（参考 19 号笔记）。

```java
invoke(..., Method method, ...)
```

用户拿到 method 能做两件关键事：
- `method.getName()` → 知道方法名
- `method.invoke(realObj, args)` → 反射调真对象

### 信息 2：**调用时传了什么参数？**

调用 `proxy.sing("稻香")` → 用户需要知道 `"稻香"`。

怎么传？——**打包成数组 `Object[]`**（最通用，能容纳任意数量/类型的参数）。

```java
invoke(..., Object[] args)
```

### 信息 3：**代理对象本身**

JDK 设计者想："偶尔用户可能需要知道是哪个代理被调用。" → 加个参数：

```java
invoke(Object proxy, ..., ...)
```

**99% 场景用不上**，但为了完整性留着。

### 推出结论 5

```java
invoke(Object proxy, Method method, Object[] args)
```

**三个参数每个都有设计理由**。

---

## 🤔 第六关：返回值 + 异常

**返回值类型是什么？**

代理方法可能返回各种类型（void / int / String / User / List...）。
→ 统一用 **`Object`**（所有类型的父类）。
- void 方法 → 返回 null
- int 方法 → 返回 Integer（自动装箱）
- 任何引用类型 → 直接返回

**异常怎么处理？**

代理的真方法可能抛任何异常（包括 checked 异常）。
→ 声明抛 **`Throwable`**（异常的最父类）。

### 推出结论 6

```java
Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
```

---

## ✅ 最终的 InvocationHandler 接口

```java
package java.lang.reflect;

public interface InvocationHandler {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
```

**每一部分都有理由**：

| 设计决策 | 理由 |
|---------|------|
| 是接口（不是抽象类）| 不占单继承名额 + 支持 Lambda |
| 只有一个方法 | 折叠所有方法调用到一个入口 |
| 方法叫 invoke | 语义"调用" |
| `Object proxy` | 偶尔需要知道代理本身 |
| `Method method` | **必须**知道是哪个方法被调 |
| `Object[] args` | 通用容纳任意参数 |
| 返回 `Object` | 通用容纳任意返回类型 |
| 抛 `Throwable` | 透传任何异常 |

**这是经过深思熟虑的最小 API 设计**。

---

## 🎁 类比：其他语言的同类设计

### JavaScript `Proxy`

```javascript
const proxy = new Proxy(target, {
    get(target, prop, receiver) {           // 相当于 Java 的 invoke
        return function(...args) {
            console.log("调:", prop);
            return target[prop](...args);
        };
    }
});
```

### Python `__getattr__`

```python
class Proxy:
    def __init__(self, target):
        self.target = target

    def __getattr__(self, name):            # 所有属性访问都走这里
        return getattr(self.target, name)
```

**三种语言，三种语法，但思想完全一样**：

> **"提供一个通用钩子，用户在钩子里写逻辑"**

---

## 🧪 手工模拟一下（加深理解）

**虽然 JDK 不让你手写代理类，但我们手工模拟一下帮助理解**：

```java
interface Celebrity {
    void sing(String song);
    void attend(String event, double fee);
}

// 假设这是 JDK 动态生成的代理类（手写模拟）
class HandmadeProxy implements Celebrity {
    private final InvocationHandler handler;

    public HandmadeProxy(InvocationHandler handler) {
        this.handler = handler;
    }

    @Override
    public void sing(String song) {
        try {
            Method m = Celebrity.class.getMethod("sing", String.class);
            handler.invoke(this, m, new Object[]{song});
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void attend(String event, double fee) {
        try {
            Method m = Celebrity.class.getMethod("attend", String.class, double.class);
            handler.invoke(this, m, new Object[]{event, fee});
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
```

**每个代理方法的实现都是**：
1. 拿 Method
2. 把参数打包
3. 转发到 `handler.invoke`

**你理解了这一点，就理解了 JDK 动态代理的本质**。

---

## 🧪 实战：日志代理（用 JDK 真实 API 跑一次）

光讲接口没意思，**我们现在就把它跑起来**。

这是一个完整的可运行例子 —— **用你刚学会的 InvocationHandler**，加一点 `Proxy.newProxyInstance`（下一篇会详解），实现一个"**调用前后打日志**"的代理。

### 场景
有一个 `Greeter`（打招呼者），我们给它加个代理，让它每次打招呼前后都打日志。

### 完整代码

```java
import java.lang.reflect.*;

// 接口
interface Greeter {
    void sayHello(String name);
    String describe();
}

// 真实实现
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

        // 3️⃣ 用 Proxy 工厂创建代理（下一篇详解这三个参数）
        Greeter proxy = (Greeter) Proxy.newProxyInstance(
                Greeter.class.getClassLoader(),      // 下篇讲
                new Class<?>[] { Greeter.class },    // 下篇讲
                loggingHandler                        // ← 你的 InvocationHandler
        );

        // 4️⃣ 用代理调方法
        System.out.println("=== 第一次调用 ===");
        proxy.sayHello("张三");

        System.out.println("\n=== 第二次调用 ===");
        proxy.sayHello("李四");

        System.out.println("\n=== 调用另一个方法 ===");
        String desc = proxy.describe();
        System.out.println("拿到描述: " + desc);
    }
}
```

### 预期输出

```
=== 第一次调用 ===
📝 [日志] 准备调用: sayHello 参数=[张三]
  👋 Hello, 张三
📝 [日志] 调用完毕，返回: null

=== 第二次调用 ===
📝 [日志] 准备调用: sayHello 参数=[李四]
  👋 Hello, 李四
📝 [日志] 调用完毕，返回: null

=== 调用另一个方法 ===
📝 [日志] 准备调用: describe
  (什么也没打，因为 describe 没 println)
📝 [日志] 调用完毕，返回: 我是打招呼者
拿到描述: 我是打招呼者
```

### 关键点解读

**① 你只写了一个 InvocationHandler**
- 它**不知道**自己在代理 Greeter
- 它**只知道**"有方法被调了，我就打日志 + 调真对象"

**② 一个 Handler 适配所有方法**
- `sayHello` 被调 → 进 invoke
- `describe` 被调 → 也进 invoke
- **同一套逻辑应用到所有方法**

**③ 客户端完全无感**
```java
Greeter proxy = ...;   // 拿到代理
proxy.sayHello("张三"); // 写法和调真对象一模一样
```
客户端代码**和调 RealGreeter 没有任何区别** —— 但每次调用都自动打了日志。

**④ 想加/改/去掉日志？**
**只改 InvocationHandler 一处**，所有方法都生效。

### 这就是 Spring AOP 的雏形

```java
@Service
public class UserService {
    @Transactional
    public void save(User u) { ... }    // 想让它自动开事务？
}
```

Spring 做的事情**和你上面这段代码一模一样**：
- 写一个 `TransactionHandler implements InvocationHandler`
- 在 invoke 里：前 → 开事务 → 调真方法 → 提交
- 用 Proxy.newProxyInstance 包装 UserService
- 把代理对象放进 IoC 容器

**你现在已经理解 Spring AOP 的核心原理了**。

### 🎯 现在你能跑起来了

**把上面的完整代码复制到一个文件 `InvocationHandlerDemo.java`，直接运行**：

```bash
java InvocationHandlerDemo.java
```

应该看到预期输出。**眼见为实**后，你对 InvocationHandler 的理解会落地。

---

## 📌 本篇一句话总结

> **InvocationHandler 是"代理的钩子"**。JDK 面对"不知道要代理什么接口 + 不知道用户要做什么"这两个未知，设计出了"**接口 + 一个 invoke 方法 + 三个参数**"这个最小方案。
>
> **每个参数都有设计理由**：`proxy`（代理自身，为完整性）、`method`（**关键**：是哪个方法被调）、`args`（调用参数）。
>
> **核心思想**：**把所有方法调用折叠到一个 invoke 方法**。用户只写 invoke，处理所有调用。

---

## 🔗 下一步

现在你理解了 InvocationHandler 的设计。

**下一篇 → [21 · 动态代理（二）· Proxy 工厂 & 完整流程 & 实战](./21-动态代理二-Proxy工厂与实战.md)**

讲：
- `Proxy.newProxyInstance` 三个参数
- 完整调用链路图
- 手写一个"**失败自动重试**"代理（可运行）
- 和 Spring AOP 的对应关系
