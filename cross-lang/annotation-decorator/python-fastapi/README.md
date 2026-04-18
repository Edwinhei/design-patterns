# FastAPI · 示例

## 🎯 这个示例展示了什么

FastAPI **不引入新的注解语法**，巧妙利用 Python 已有的特性：
- **装饰器**（`@app.get`）注册路由
- **类型提示**（`user_id: int`）标注参数类型
- **默认值作为元数据**（`= Path(...)` / `= Query(...)` / `= Depends(...)`）
- **Pydantic 模型**作为请求体自动校验

这是**最 Pythonic** 的实现方式。

## 🚀 运行

### 1. 安装依赖

```bash
pip install fastapi uvicorn pydantic
```

### 2. 启动服务

```bash
uvicorn app:app --reload
```

### 3. 访问

- http://localhost:8000/ —— 根路径
- http://localhost:8000/users/1?q=hello —— 带路径参数和查询参数
- http://localhost:8000/docs —— **FastAPI 自动生成的 OpenAPI 文档**（超级实用）

### 4. 或直接看"元数据秘密"

不启动服务器，直接：

```bash
python3 app.py
```

会输出 FastAPI 通过 `inspect.signature` 读到的函数签名元数据。

## 📋 预期输出（直接跑 python3 app.py）

```
=== FastAPI 的秘密：查看函数签名 ===

参数 user_id:
  类型提示: <class 'int'>
  默认值:   Path(PydanticUndefined, ge=1, description='用户 ID，必须大于 0')
  默认值的类型: Path

参数 q:
  类型提示: typing.Optional[str]
  默认值:   Query(None, min_length=3, max_length=50, description='查询关键词')
  默认值的类型: Query

参数 service:
  类型提示: <class '__main__.UserService'>
  默认值:   Depends(get_user_service)
  默认值的类型: Depends
```

**关键点**：**默认值的类型** 是 Path / Query / Depends，这就是 FastAPI 判断"这个参数从哪来"的依据。

## 🔍 关键代码解读

### 1. 装饰器（简单注册路由）

```python
@app.get("/users/{user_id}")
async def get_user(...): ...
```

装饰器**立即执行**，把函数注册到路由表。没有复杂元数据存储（不像 NestJS）。

### 2. 类型提示 + 默认值（元数据）

```python
user_id: int = Path(..., ge=1)
#         ↑     ↑
#      类型    默认值 = Path 实例 = 元数据
```

**这个 Path 对象不是装饰器**，是普通对象。放在默认值槽位里。

FastAPI 通过 `inspect.signature()` 读签名，看到默认值是 `Path` 类型 → 知道从 URL 路径取。

### 3. Pydantic 自动校验

```python
class UserCreate(BaseModel):
    name: str = Field(..., min_length=2)
    age: int = Field(..., ge=0, le=150)

@app.post("/users")
async def create_user(user: UserCreate): ...
#                       ↑
#                 FastAPI 看到类型是 Pydantic 模型
#                 → 自动把 JSON body 解析 + 校验 → 实例
```

## 🎭 核心洞察

**看 `app.py` 末尾那段代码**：

```python
sig = inspect.signature(get_user)
for name, param in sig.parameters.items():
    print(param.annotation)   # 类型提示
    print(param.default)       # 默认值（Path/Query/Depends 实例）
```

**这就是 FastAPI 的"魔法"**：**用反射读函数签名，判断每个参数的默认值类型**。

## 🎯 对比其他语言

- [../java/](../java/) —— Java 用注解：`@PathVariable Long id` / `@RequestParam String q`
- [../typescript-nestjs/](../typescript-nestjs/) —— NestJS 用装饰器：`@Param('id')` / `@Query('q')`
- **FastAPI 用"默认值对象"** —— `id: int = Path(...)` / `q: str = Query(...)`

**都达到同样效果，FastAPI 最不引入新语法**。

## 🔗 扩展阅读

- [../../../java-notes/16 跨语言辨析](../../../java-notes/16-注解与装饰器跨语言辨析.md)
- [FastAPI 官方文档](https://fastapi.tiangolo.com/)
