# 第 17 课 · 责任链 Chain of Responsibility ★★

> 类型：行为型 | 难度：★★ | GoF 经典 | **中间件架构的基石**

## 🎯 本课目标

- [x] 理解"**请求沿链传递直到有人处理**"
- [x] 能识别 Servlet Filter / Spring Security / OkHttp 里的责任链
- [x] 会用责任链消除一堆 if-else

---

## 🎬 场景：请假审批流程

公司请假有审批权限：

| 审批级别 | 能批多少天 |
|---------|----------|
| 组长 | ≤ 3 天 |
| 经理 | ≤ 7 天 |
| 总监 | ≤ 30 天 |

**审批流程**：你递交请假单 → **先到组长** → 如果组长批不了 → **自动转给经理** → 还批不了 → **再转总监**。

---

## 🤔 土办法：一堆 if-else

```java
void approve(LeaveRequest req) {
    if (req.days <= 3) {
        System.out.println("组长批准");
    } else if (req.days <= 7) {
        System.out.println("经理批准");
    } else if (req.days <= 30) {
        System.out.println("总监批准");
    } else {
        System.out.println("超出审批范围");
    }
}
```

跑 [code/BadChain.java](code/BadChain.java)。

**痛点**：
- 🙁 加新审批级（主管）→ 要改 `approve` 方法
- 🙁 业务流程**硬编码**在代码里，不灵活
- 🙁 级别顺序固定，**不能动态调整**
- 🙁 判断逻辑和业务逻辑耦合

---

## 💡 责任链模式登场

**核心思想**：**每个处理者都持有"下一个处理者"的引用，处理不了就向下传递**。

```java
// 抽象处理者
abstract class Approver {
    protected Approver next;    // 🔗 下一环

    public Approver setNext(Approver next) {
        this.next = next;
        return next;             // 返回 next，便于链式调用
    }

    // 模板方法：先判断能不能处理，不能就传递
    public final void handle(LeaveRequest req) {
        if (canHandle(req)) {
            process(req);
        } else if (next != null) {
            next.handle(req);    // 🔗 传递给下一个
        } else {
            System.out.println("❌ 无人能处理");
        }
    }

    protected abstract boolean canHandle(LeaveRequest req);
    protected abstract void process(LeaveRequest req);
}

// 具体处理者
class GroupLeader extends Approver {
    protected boolean canHandle(LeaveRequest req) { return req.days <= 3; }
    protected void process(LeaveRequest req)     { System.out.println("👨‍💼 组长批准 " + req); }
}

class Manager extends Approver {
    protected boolean canHandle(LeaveRequest req) { return req.days <= 7; }
    protected void process(LeaveRequest req)     { System.out.println("🎩 经理批准 " + req); }
}

class Director extends Approver {
    protected boolean canHandle(LeaveRequest req) { return req.days <= 30; }
    protected void process(LeaveRequest req)     { System.out.println("👔 总监批准 " + req); }
}
```

### 使用

```java
// 组装链
Approver chain = new GroupLeader();
chain.setNext(new Manager())
     .setNext(new Director());

// 提交请求
chain.handle(new LeaveRequest("张三", 2));    // → 组长批
chain.handle(new LeaveRequest("李四", 5));    // → 组长转 → 经理批
chain.handle(new LeaveRequest("王五", 15));   // → 组长转 → 经理转 → 总监批
```

**威力**：
- ✅ 加新处理者（主管） → **只加一个类**，组装链时加一环，不改已有代码
- ✅ 调整顺序 → 改组装代码即可
- ✅ 动态组装不同链应对不同业务
- ✅ 每个处理者只关心自己的职责

跑 [code/ChainDemo.java](code/ChainDemo.java) 看完整流程。

---

## 📐 UML 结构

```
┌────────────────────┐
│  Approver (抽象)    │
├────────────────────┤
│ -next: Approver    │◀──┐
│ +setNext(n)        │   │ 持有下一环
│ +handle(req)       │───┘
│ #canHandle(req)    │
│ #process(req)      │
└─────────▲──────────┘
          │
    ┌─────┴─────┬─────────┐
    │           │         │
GroupLeader  Manager   Director
```

**链的核心**：**每个处理者持有下一个处理者的引用**，形成单向链表。

---

## 🌍 责任链 = 中间件架构的基础

### Servlet Filter Chain

```java
// web.xml 或注解配置
// Filter 1: 字符编码
// Filter 2: 鉴权
// Filter 3: 日志
// → 最后到 Servlet

public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        if (isLoggedIn(req)) {
            chain.doFilter(req, res);     // 👈 传给下一个 Filter
        } else {
            res.sendRedirect("/login");
        }
    }
}
```

**Servlet Filter 就是标准的责任链**。

### Spring Security

```
请求 → SecurityFilter1 (CORS)
     → SecurityFilter2 (CSRF)
     → SecurityFilter3 (认证)
     → SecurityFilter4 (授权)
     → ... (一共 15+ 个过滤器)
     → Controller
```

**Spring Security 的核心就是一条巨大的责任链**。

### OkHttp Interceptor

```java
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new AuthInterceptor())
    .addInterceptor(new RetryInterceptor())
    .build();
```

**每个 Interceptor 决定是否向下传递**。

### 其他

| 工具 | 责任链体现 |
|------|----------|
| Servlet Filter | 经典案例 |
| Spring Security FilterChain | 15+ 过滤器组成的链 |
| Spring MVC HandlerInterceptor | 前置 / 后置拦截 |
| OkHttp Interceptor | HTTP 请求拦截链 |
| Netty ChannelPipeline | 网络 I/O 处理链 |
| Node.js Express middleware | 中间件就是责任链 |
| Redux middleware | 状态管理拦截链 |

**"中间件"本质上就是责任链**。

---

## 🔀 责任链的两种变体

### 变体 1：**找到能处理的就停**（本课例子）
请假审批 → 组长能批就不传递。

### 变体 2：**链上每个都处理一下**（中间件模式）
HTTP 请求 → 日志记一下 + 鉴权验一下 + 限流查一下 → 都过才到业务。

**两种都属于责任链，只是"传递逻辑"不同**。

---

## ⚠️ 什么时候别用

### 🚫 处理者只有 1-2 个
直接 if-else 更简单。

### 🚫 链很深（几十层）
性能问题（方法调用栈深 + 内存 + 可读性差）。

### 🚫 处理逻辑极其简单
Map 查表可能比链更合适。

---

## 📝 思考题 & 小练习

### 思考题

1. 如果链上某个处理者**抛异常**，整条链会怎样？（答：异常中断传递，除非上层处理者捕获）
2. 责任链和观察者有什么区别？（答：观察者"所有人都收到"，责任链"传递到被处理为止"）
3. Spring Security 有 15+ Filter，性能会有问题吗？（答：单次请求开销可以接受，这种架构的优势远超开销）

### 小练习

**扩展 ChainDemo.java**

加一个新审批级别：**主管**（能批 ≤ 5 天，介于组长和经理之间）。
观察：
- 需要改 `GroupLeader` / `Manager` 吗？（答：不用）
- 只需要加个 `Supervisor` 类 + 链组装时插一环

这就是责任链的**开闭原则**体现。

---

## 🏁 学完后

- **"懂了，下一课"** → 第 18 课 · 命令 Command（餐厅点单小票）
- **"还有问题"** → 问
- **"先 commit"** → 我帮你

**责任链是中间件架构的基石**。以后你看 Servlet Filter、Spring Security、OkHttp、Redux middleware 都会**一眼认出**它 🔗
