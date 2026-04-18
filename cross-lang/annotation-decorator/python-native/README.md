# Python 原生装饰器 · 示例

## 🎯 这个示例展示了什么

Python 装饰器**不是元数据标签**，而是**高阶函数**：
- 接收一个函数
- 返回一个新函数（通常是原函数的包装版）
- `@decorator` 语法 = `func = decorator(func)` 的语法糖

和 Java 注解的对比：
- Java：**定义一个注解 + 另外写处理器**
- Python：**装饰器自己就是处理器**

## 🚀 运行

```bash
python3 decorator_demo.py
```

**需要**：Python 3.x（几乎所有版本都支持）

## 📋 预期输出

```
=== @timer 单装饰器 ===
  订单已创建
⏱  place_order 耗时 120.1 ms

=== @log + @timer 组合装饰器 ===
[退款业务] 调用 wrapper
  退款处理完成
⏱  refund 耗时 50.3 ms

=== 普通方法（无装饰器）===
  普通方法，无日志

=== 装饰器揭秘 ===
  foo 执行
⏱  foo 耗时 50.1 ms

✅ 装饰器原理小结：
   ① Python 装饰器 = 高阶函数
   ② @timer 只是 foo = timer(foo) 的语法糖
   ③ 装饰器【立即执行】，直接替换原函数
   ④ 不像 Java 注解要额外的'处理器'
```

## 🔍 关键代码解读

### 1. 装饰器本质

```python
def timer(func):
    def wrapper(*args, **kwargs):
        # 调用前做点什么
        result = func(*args, **kwargs)
        # 调用后做点什么
        return result
    return wrapper
```

**三层结构**：
- 外层 `timer` 接收原函数
- 内层 `wrapper` 是包装版
- 返回 `wrapper`，让它替换掉原函数

### 2. `@functools.wraps` 的作用

```python
@wraps(func)
def wrapper(*args, **kwargs): ...
```

不加 `@wraps`，`wrapper.__name__` 会变成 `'wrapper'`，丢失原函数信息。

### 3. 带参数的装饰器（装饰器工厂）

```python
@log("退款业务")
def refund(): ...

# 等价于：
refund = log("退款业务")(refund)
#        ↑               ↑
#    返回装饰器        装饰器包装函数
```

需要**两层嵌套**，所以叫"装饰器工厂"。

## 🎭 和 Java 注解的本质差异

| | Java | Python |
|--|------|--------|
| 何时执行 | 编译期只记录标签 | **定义时立即执行**（函数被替换）|
| 自己有逻辑吗 | ❌ | ✅（装饰器就是处理逻辑）|
| 需要处理器 | ✅ 必须另外写 | ❌ |
| 运行期开销 | 只是元数据，调用本身无开销 | **每次调用都经过 wrapper** |

## 🔗 扩展阅读

- [../../../java-notes/16 跨语言辨析](../../../java-notes/16-注解与装饰器跨语言辨析.md)

## 🎯 对比

看完这个，对比：
- [../java/](../java/) —— Java 注解 + 反射（需要处理器）
- [../python-fastapi/](../python-fastapi/) —— FastAPI 如何把装饰器 + 类型提示结合
