# TypeScript 装饰器 + reflect-metadata · 示例

## 🎯 这个示例展示了什么

**手写简化版 NestJS**，用最少的代码展示 NestJS 的核心原理：

1. **TypeScript 装饰器**立即执行
2. **reflect-metadata** 提供元数据存取能力
3. 装饰器只"**存元数据**"不修改类
4. "框架"启动时**扫描所有 class 的元数据**，注册路由

整个 demo 不到 100 行，但**完整呈现了 NestJS 的工作原理**。

## 🚀 运行

### 方式 1：ts-node（最简单）

```bash
# 进入本目录
cd cross-lang/annotation-decorator/typescript-nestjs/

# 装依赖（装在当前目录）
npm init -y
npm install typescript reflect-metadata ts-node
npm install --save-dev @types/node

# 创建 tsconfig.json（见下方）
# 运行
npx ts-node mini_nest.ts
```

### tsconfig.json 内容

创建一个 `tsconfig.json` 文件：

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "experimentalDecorators": true,
    "emitDecoratorMetadata": true,
    "strict": false,
    "esModuleInterop": true
  }
}
```

**关键**：`experimentalDecorators: true` 和 `emitDecoratorMetadata: true` 必须开，装饰器才能用。

## 📋 预期输出

```
📝 [装饰器] @Get('/:id') 注册到 findOne
📝 [装饰器] @Get('') 注册到 findAll
📝 [装饰器] @Post('') 注册到 create
📝 [装饰器] @Controller('/users') 注册到 UserController
📝 [装饰器] @Get('') 注册到 findAll
📝 [装饰器] @Controller('/orders') 注册到 OrderController

🚀 框架启动：扫描 Controller 元数据

📋 注册的路由：
  GET    /users/:id         → UserController#findOne
  GET    /users             → UserController#findAll
  POST   /users             → UserController#create
  GET    /orders            → OrderController#findAll

✅ NestJS 原理小结：
   ① @Controller / @Get 是 TS 装饰器，class 加载时立即执行
   ② 装饰器用 Reflect.defineMetadata 给 class/方法存元数据
   ③ 装饰器不修改类本身，class 代码保持干净
   ④ 框架启动时扫描所有 class，读元数据，注册路由
   ⑤ 用法像 Java 注解，底层是 TS 装饰器 + reflect-metadata
```

## 🔍 关键代码解读

### 1. 装饰器工厂

```typescript
function Controller(path: string): ClassDecorator {
    return (target) => {
        Reflect.defineMetadata('controller:path', path, target);
    };
}
```

**三层结构**：
- `Controller(path)` —— 装饰器工厂（接收用户参数）
- 返回一个真正的装饰器函数（接收 class）
- 函数内用 `Reflect.defineMetadata` 存元数据

### 2. 立即执行的证据

看输出的**顺序**：

```
📝 [装饰器] @Get('/:id') 注册到 findOne        ← 先执行方法装饰器
📝 [装饰器] @Controller('/users') 注册到 ...   ← 再执行类装饰器

🚀 框架启动：扫描 Controller 元数据            ← 最后才"启动框架"
```

**装饰器在 class 加载时就执行了**，和 Python 装饰器一样。

### 3. 用 `Reflect.defineMetadata` / `Reflect.getMetadata` 存取

```typescript
// 存
Reflect.defineMetadata('key', value, target);

// 读
const v = Reflect.getMetadata('key', target);
```

这是 `reflect-metadata` polyfill 提供的 API，原生 JS 没有。

## 🎭 对比 Java 和 Python

### Java 注解
```java
@Controller("/users")           // 只是标签，编译期记录
class UserController { ... }

// 启动时：Spring 用反射读 class 的注解
```

### Python 装饰器（纯粹版）
```python
@route("/users")                # 立即执行，替换 class
class UserController: ...

# @route 可能包装了 class 或做了改造
```

### TypeScript（NestJS 风格）
```typescript
@Controller('/users')           // 立即执行，但只存元数据
class UserController { ... }

// 启动时：框架读 reflect-metadata
```

**NestJS 是把 Python 装饰器"用得像 Java 注解"**。

## 🎯 对比其他语言

- [../java/](../java/) —— Java 注解，编译期记录 + 运行时反射
- [../python-native/](../python-native/) —— Python 装饰器，立即修改函数
- [../python-fastapi/](../python-fastapi/) —— FastAPI，不用装饰器存元数据

## 🔗 扩展阅读

- [../../../java-notes/16 跨语言辨析](../../../java-notes/16-注解与装饰器跨语言辨析.md)
- [TypeScript Decorators](https://www.typescriptlang.org/docs/handbook/decorators.html)
- [NestJS 官方文档](https://docs.nestjs.com/)
