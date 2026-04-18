# 第 8 课 · 代理 Proxy ★★

> 类型：结构型 | 难度：★★ | GoF 经典 | **打通 Spring AOP / 注解底层的钥匙**

## 🎯 本课目标

- [x] 理解"**代理 = 给真对象包一层，拦截调用**"
- [x] 掌握 **静态代理** + **JDK 动态代理**两种实现
- [x] 看懂 Spring `@Transactional` / 注解属性访问的底层机制
- [x] 能区分 Proxy / Adapter / Facade / Decorator 四兄弟

---

## 🎬 场景：明星的经纪人

周杰伦这种顶级明星**不直接接电话**。想找他干活，你得：

- 📞 先联系经纪人
- 经纪人问你预算（筛选客户）
- 经纪人核对档期（状态检查）
- 经纪人记录合同（日志）
- 合格 → 转达给明星本人 → 明星出席 / 唱歌
- 经纪人结算、开发票（后处理）

**经纪人 = Proxy（代理）**：
- 对外**伪装成明星**（客户以为在联系明星）
- 内部**持有真明星**（不能代替唱歌，唱歌必须本人）
- **拦截调用**，在前后做预处理 / 后处理

**关键**：**经纪人和明星有同一个"能力清单"**（都能接唱歌活动、出席活动），所以对客户来说可以"无感替换"。

---

## 🤔 土办法：横切关注点混在业务里

假如明星自己接电话，他得这样：

```java
class Singer {
    public void sing(String song) {
        // 📞 筛选客户（横切关注点）
        // ... 判断来电合法性

        // ⏱ 记录时间（横切关注点）
        long start = System.nanoTime();

        // 🎤 真正的业务
        System.out.println("🎤 演唱: " + song);

        // ⏱ 耗时统计（横切关注点）
        long cost = System.nanoTime() - start;

        // 💰 结算（横切关注点）
        // ... 开发票
    }

    public void attend(String event, double fee) {
        // 又来一遍筛选 / 时间 / 结算 ...
    }
}
```

**痛点**：
- 🙁 业务代码和非业务代码**全混在一起**（违反单一职责）
- 🙁 每个方法都要**重复写一遍**筛选、记时间、结算
- 🙁 想改筛选规则？要改**所有方法**
- 🙁 测试业务时要**连同横切关注点一起测**

跑一下 [code/BadProxy.java](code/BadProxy.java) 感受。

---

## 💡 静态代理（最朴素的 Proxy）

**核心思想**：**新建一个代理类，和真对象实现同一接口。代理类持有真对象，在方法前后插入"横切代码"**。

```java
// 接口
interface Celebrity {
    void sing(String song);
    void attend(String event, double fee);
}

// 真明星：只管业务
class RealCelebrity implements Celebrity {
    public void sing(String song) {
        System.out.println("🎤 演唱: " + song);
    }

    public void attend(String event, double fee) {
        System.out.println("🎭 出席: " + event);
    }
}

// 经纪人（代理）：处理横切关注点
class CelebrityManager implements Celebrity {
    private final RealCelebrity celebrity;

    public CelebrityManager(RealCelebrity celebrity) {
        this.celebrity = celebrity;
    }

    public void sing(String song) {
        System.out.println("📞 经纪人：登记唱歌请求");    // 前
        long start = System.nanoTime();

        celebrity.sing(song);                              // 👈 委托给真对象

        long cost = (System.nanoTime() - start) / 1_000_000;
        System.out.println("💰 经纪人：结算，耗时 " + cost + "ms");  // 后
    }

    public void attend(String event, double fee) {
        if (fee < 100_000) {
            System.out.println("❌ 经纪人：报价不够，拒绝");    // 访问控制
            return;
        }
        celebrity.attend(event, fee);
    }
}

// 客户端
Celebrity star = new CelebrityManager(new RealCelebrity());
star.sing("稻香");      // 看起来在调明星，实际先经过经纪人
```

**客户端完全不知道有代理存在**，它以为自己在调明星。

**痛点**：**每加一个明星（或者每加一个接口），就要手写一个代理类**。如果有 100 个业务类要加日志，就要写 100 个代理类。

---

## 🚀 JDK 动态代理 —— 一个代理通吃所有

**核心**：JDK 提供 `java.lang.reflect.Proxy`，**运行时动态生成代理类**。

```java
import java.lang.reflect.*;

class LoggingHandler implements InvocationHandler {
    private final Object target;

    public LoggingHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("📞 [动态代理] " + method.getName() + " 调用前");
        long start = System.nanoTime();

        Object result = method.invoke(target, args);       // 反射调真方法

        long cost = (System.nanoTime() - start) / 1_000_000;
        System.out.println("💰 [动态代理] " + method.getName() + " 耗时 " + cost + "ms");
        return result;
    }
}

// 使用
Celebrity real = new RealCelebrity();
Celebrity proxy = (Celebrity) Proxy.newProxyInstance(
    Celebrity.class.getClassLoader(),              // 类加载器
    new Class<?>[] { Celebrity.class },            // 代理实现的接口
    new LoggingHandler(real)                       // 拦截器
);

proxy.sing("稻香");      // 会走 LoggingHandler.invoke()
```

**威力**：**同一个 `LoggingHandler` 可以适用于任何接口**。100 个业务类都能用这一个 handler 加日志。

### 动态代理的限制
- **JDK 动态代理只能代理接口**（`target` 必须有至少一个接口）
- 代理普通类 → 用 **CGLIB** / **ByteBuddy**（字节码增强）

---

## 🔥 终于能解释：Spring `@Transactional` 怎么工作的

还记得**你之前几次问过的 Spring 魔法**吗？

```java
@Service
public class OrderService {
    @Transactional                 // 就一个注解，怎么自动事务的？
    public void placeOrder(Order o) { ... }
}
```

**真相**：

```
Spring 启动时：
  ① 扫描 @Service 类 → 发现 OrderService
  ② 发现它有 @Transactional 方法
  ③ 用【动态代理】生成一个代理对象
      - JDK Proxy（如果有接口）
      - CGLIB（如果没接口，用字节码继承）
  ④ 代理拦截所有方法调用：
      try {
          beginTransaction();            // 开事务
          Object r = method.invoke(real, args);  // 调真方法
          commit();                      // 提交
          return r;
      } catch (Exception e) {
          rollback();                    // 回滚
          throw e;
      }
  ⑤ Spring 容器里【存的是代理对象】，不是 OrderService 本身

你注入：
  @Autowired OrderService service;    // 实际注入的是代理对象

你调：
  service.placeOrder(order);
  → 实际进了代理的 invoke
  → 代理先开事务 → 再调真的 placeOrder → 最后提交
```

**@Transactional 的魔法 = 动态代理 + 方法拦截**。

---

## 🎭 代理的典型应用分类

代理模式有**多种用途**，都共享"包一层"的结构：

| 用途 | 例子 |
|------|------|
| **远程代理**（Remote Proxy）| RMI / 微服务 RPC 客户端（本地调用 → 实际发网络请求）|
| **虚拟代理**（Virtual Proxy）| 延迟加载（大图/大对象用到才真正加载）|
| **保护代理**（Protection Proxy）| 权限控制 / 访问限制 |
| **智能引用**（Smart Reference）| 引用计数 / 锁 / 缓存 |
| **日志代理** | 记录调用日志 |
| **AOP 代理** | Spring `@Transactional` / `@Async` / `@Cacheable` |

**一种模式，多种用途**。

---

## 🔀 结构型四兄弟辨析（重点！）

Proxy / Adapter / Facade / Decorator **都是"包一层"**，但**意图完全不同**：

| 模式 | 意图 | 接口关系 | 典型句子 |
|------|------|--------|--------|
| **Proxy**（本课）| **控制访问** | 接口**相同** | "我代替你，在调你之前/后做点事" |
| **Adapter** | **适配不兼容接口** | 接口**不同** | "让你用起来像目标接口" |
| **Facade** | **简化复杂子系统** | **聚合多个** | "一键启动，别操心细节" |
| **Decorator** | **动态加功能** | 接口**相同**，可**堆叠** | "再套一层，再加一个能力" |

### 一句话辨别
- Proxy → **"代替"**（你没来，我替你）
- Adapter → **"翻译"**（让两边对得上）
- Facade → **"前台"**（简化入口）
- Decorator → **"包装"**（一层一层加料）

---

## 🌍 真实应用

| 在哪里 | 代理做什么 |
|--------|-----------|
| **Spring AOP** | `@Transactional` / `@Async` / `@Cacheable` 全靠代理 |
| **Spring Data JPA** | `@Repository` 接口根本没实现类 → Spring 生成代理实现 CRUD |
| **MyBatis** | `@Mapper` 接口也是代理生成 SQL 执行 |
| **Dubbo / gRPC** | RPC 客户端就是一个远程代理 |
| **JDK 注解机制** | 注解属性的 `anno.value()` 调用 → JDK 动态代理实现（见 15 号笔记补充）|
| **Mockito** | mock 对象 = 动态代理 |
| **Retrofit**（Android）| `@GET` 接口 → 动态代理生成 HTTP 请求 |

---

## ⚠️ 什么时候别用

### 🚫 业务对象简单，没有横切关注点
没必要包一层，直接用就行。

### 🚫 代理层成为性能瓶颈
代理有一点点开销（反射调用、额外方法栈）。99% 场景可忽略，**高频热点路径**要考虑。

### 🚫 滥用代理隐藏复杂度
代理里塞太多业务逻辑 → 本末倒置。代理应该只做"横切"，不做业务。

---

## 📝 思考题 & 小练习

### 思考题

1. 静态代理 vs 动态代理，各自的优缺点？
2. 为什么 JDK 动态代理**要求目标类实现接口**？
3. Spring 如何选择 JDK Proxy 还是 CGLIB？
4. 为什么 `@Transactional` 在**同一个类的方法内部互调时不生效**？（提示：代理只拦截外部调用）

### 小练习

**练习 1：写一个"重试代理"**
给任意接口方法加上"失败自动重试 3 次"的能力。提示：用 JDK 动态代理。

**练习 2：研究 JDK 源码**
打开 IDE，看 `java.lang.reflect.Proxy.newProxyInstance` 的实现。你会发现它**动态生成字节码**创建代理类。

---

## 🏁 学完后

- **"都懂了，下一课"** → 第 9 课 · 组合 Composite（文件夹装文件夹）
- **"动态代理再讲一次"** → 我拆步骤
- **"做了练习"** → 贴代码 review

---

**这一课的深远意义**：你现在理解 Spring 的"魔法"就不再神秘了。
**注解 + 反射 + 动态代理 = Spring 的整套基础设施**。

往后再看任何 Spring / MyBatis / RPC 源码，你会有**"哦就是代理而已"**的淡定感 🙌
