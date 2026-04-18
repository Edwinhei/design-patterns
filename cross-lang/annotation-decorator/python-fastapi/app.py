"""
============================================================
FastAPI · 类型提示 + 默认值 做元数据 · 可运行示例
============================================================

展示 FastAPI 的四大机制：
  1. 装饰器注册路由（@app.get）
  2. 类型提示做参数类型（user_id: int）
  3. 默认值做元数据槽位（= Path(...) / = Query(...) / = Depends(...)）
  4. Pydantic 做数据校验

安装依赖：
  pip install fastapi uvicorn pydantic

运行：
  uvicorn app:app --reload

访问：
  http://localhost:8000/users/1?q=hello
  http://localhost:8000/docs   (自动生成的 OpenAPI 文档)
"""

from fastapi import FastAPI, Path, Query, Depends
from pydantic import BaseModel, Field
from typing import Optional

app = FastAPI(title="注解与装饰器示例")


# ================================================================
# 依赖注入：一个"假"的 UserService
# ================================================================
class UserService:
    def find(self, user_id: int) -> dict:
        # 假数据
        users = {
            1: {"id": 1, "name": "张三"},
            2: {"id": 2, "name": "李四"},
        }
        return users.get(user_id, {"error": "用户不存在"})


def get_user_service():
    """工厂函数，返回 UserService 实例。
    FastAPI 的 Depends 会自动调用它。"""
    return UserService()


# ================================================================
# Pydantic 数据模型（用于请求体校验）
# ================================================================
class UserCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=50)
    age: int = Field(..., ge=0, le=150)


# ================================================================
# 路由定义
# ================================================================
@app.get("/")
async def root():
    """根路径"""
    return {"message": "FastAPI 注解示例"}


@app.get("/users/{user_id}")
async def get_user(
    # 路径参数：通过默认值 Path(...) 声明
    user_id: int = Path(..., ge=1, description="用户 ID，必须大于 0"),

    # 查询参数：通过默认值 Query(...) 声明
    q: Optional[str] = Query(None, min_length=3, max_length=50, description="查询关键词"),

    # 依赖注入：通过默认值 Depends(...) 声明
    service: UserService = Depends(get_user_service)
):
    """
    FastAPI 最"神奇"的部分：
      - user_id 的类型 + Path 默认值 → 告诉框架"从 URL 路径取，整数，>=1"
      - q 的类型 + Query 默认值 → 告诉框架"从 URL query 取，可选字符串"
      - service 的类型 + Depends → 告诉框架"用 get_user_service 工厂注入"

    框架通过 inspect.signature() 读取上面这些信息，自动完成参数绑定。
    """
    user = service.find(user_id)
    return {
        "user": user,
        "query": q
    }


@app.post("/users")
async def create_user(user: UserCreate):
    """
    请求体自动解析：
      - FastAPI 看到 user 是 UserCreate 类型（Pydantic 模型）
      - 自动：JSON body → Pydantic 校验 → UserCreate 实例
    """
    return {
        "created": user.model_dump(),
        "message": f"用户 {user.name} 创建成功"
    }


# ================================================================
# 演示：查看函数签名的元数据（FastAPI 就是这么读的）
# ================================================================
if __name__ == "__main__":
    import inspect

    print("=== FastAPI 的秘密：查看函数签名 ===\n")
    sig = inspect.signature(get_user)

    for name, param in sig.parameters.items():
        print(f"参数 {name}:")
        print(f"  类型提示: {param.annotation}")
        print(f"  默认值:   {param.default}")
        print(f"  默认值的类型: {type(param.default).__name__}")
        print()

    print("✅ FastAPI 就是通过这个 inspect.signature() 拿到所有信息，")
    print("   然后看默认值是 Path / Query / Depends / Pydantic 模型等，")
    print("   决定参数从哪来、如何解析。")
