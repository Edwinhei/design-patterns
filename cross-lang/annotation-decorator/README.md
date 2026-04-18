# 跨语言元数据机制 · 代码实验

> 📍 配合 [java-notes/16 注解与装饰器跨语言辨析](../../java-notes/16-注解与装饰器跨语言辨析.md)

## 🎯 目的

用**同一个功能需求**（一个声明式路由 / 日志 / 参数绑定），在 5 种语言/框架里各写一份**最小可运行示例**，直观对比"**`@ 语法`"背后的机制**。

---

## 📁 目录

| 目录 | 语言/框架 | 机制 | 能直接跑？ |
|------|---------|------|---------|
| [java/](./java/) | Java | 注解 + 反射 | ✅ JDK 11+ 直接跑 |
| [python-native/](./python-native/) | Python | 原生装饰器 | ✅ Python 3+ 直接跑 |
| [python-fastapi/](./python-fastapi/) | FastAPI | 类型提示 + 默认值 | 需 `pip install fastapi uvicorn` |
| [typescript-nestjs/](./typescript-nestjs/) | TypeScript | 装饰器 + reflect-metadata | 需 `npm install` |
| [go/](./go/) | Go | struct tag + reflect | 需安装 Go |

---

## 🎭 同一个诉求的五种表达

"**给一个函数打标签，让框架自动做事（日志 / 路由 / 参数绑定）**"

### Java（注解 + 反射）
```java
@LogExecutionTime
public void work() { ... }
```

### Python 原生（装饰器）
```python
@timer
def work(): ...
```

### FastAPI（类型提示 + 默认值）
```python
@app.get("/users/{id}")
def get_user(id: int = Path(...)): ...
```

### NestJS（装饰器 + 元数据）
```typescript
@Controller('/users')
class Controller {
    @Get(':id')
    getUser(@Param('id') id: string) {}
}
```

### Go（struct tag）
```go
type User struct {
    Name string `json:"name" validate:"required"`
}
```

**语法各异，效果接近**。

---

## 🚀 快速开始

每个子目录有自己的 `README.md`，里面说明：
- 这个示例展示了什么
- 如何运行
- 关键代码解读

**建议学习顺序**：
1. **[java/](./java/)** ← 从最熟悉的开始
2. **[python-native/](./python-native/)** ← 理解装饰器的"立即执行"本质
3. **[go/](./go/)** ← 看最朴素的实现
4. **[python-fastapi/](./python-fastapi/)** ← 理解"默认值当元数据"
5. **[typescript-nestjs/](./typescript-nestjs/)** ← 看混血设计

---

## 📚 配套阅读

- [java-notes/15 Java 注解体系完全指南](../../java-notes/15-Java注解体系完全指南.md)
- [java-notes/16 跨语言辨析](../../java-notes/16-注解与装饰器跨语言辨析.md)
