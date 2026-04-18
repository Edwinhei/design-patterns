/**
 * ============================================================
 * 手写简化版 NestJS · 展示 TS 装饰器 + reflect-metadata 原理
 * ============================================================
 *
 * 不引入整个 NestJS 框架，用最少的代码展示它的核心原理：
 *   ① TS 装饰器立即执行，存元数据
 *   ② reflect-metadata 提供元数据存取能力
 *   ③ "框架"扫描 class 的元数据，注册路由
 *
 * 运行（需要 TypeScript 和 reflect-metadata）：
 *   npm install typescript reflect-metadata ts-node
 *   npx ts-node mini_nest.ts
 *
 * 或者用 Node.js 直接跑（需要 tsconfig.json 配置）
 */

import 'reflect-metadata';

// ================================================================
// 第一步：定义装饰器（只负责"存元数据"，不修改类）
// ================================================================

/** 类装饰器：给类打 path 标签 */
function Controller(path: string): ClassDecorator {
    return (target) => {
        Reflect.defineMetadata('controller:path', path, target);
        console.log(`📝 [装饰器] @Controller('${path}') 注册到 ${target.name}`);
    };
}

/** 方法装饰器：给方法打 GET 路径标签 */
function Get(subPath: string = ''): MethodDecorator {
    return (target, propertyKey) => {
        Reflect.defineMetadata('method:verb', 'GET', target, propertyKey);
        Reflect.defineMetadata('method:path', subPath, target, propertyKey);
        console.log(`📝 [装饰器] @Get('${subPath}') 注册到 ${String(propertyKey)}`);
    };
}

/** 方法装饰器：给方法打 POST 路径标签 */
function Post(subPath: string = ''): MethodDecorator {
    return (target, propertyKey) => {
        Reflect.defineMetadata('method:verb', 'POST', target, propertyKey);
        Reflect.defineMetadata('method:path', subPath, target, propertyKey);
        console.log(`📝 [装饰器] @Post('${subPath}') 注册到 ${String(propertyKey)}`);
    };
}

// ================================================================
// 第二步：使用装饰器（就像 NestJS 那样写）
// ================================================================

@Controller('/users')
class UserController {
    @Get('/:id')
    findOne(id: string) {
        return { id, name: '张三' };
    }

    @Get()
    findAll() {
        return [{ id: '1', name: '张三' }, { id: '2', name: '李四' }];
    }

    @Post()
    create(data: any) {
        return { created: true, data };
    }
}

@Controller('/orders')
class OrderController {
    @Get()
    findAll() {
        return [{ id: 1, total: 100 }];
    }
}

// ================================================================
// 第三步：简化版"框架"，扫描元数据，注册路由
// ================================================================

function bootstrap(controllers: any[]) {
    console.log('\n🚀 框架启动：扫描 Controller 元数据\n');

    const routes: Array<{verb: string, path: string, handler: string}> = [];

    for (const Controller of controllers) {
        // 读类级元数据
        const basePath = Reflect.getMetadata('controller:path', Controller);

        // 读方法级元数据
        const prototype = Controller.prototype;
        const methodNames = Object.getOwnPropertyNames(prototype)
            .filter(name => name !== 'constructor');

        for (const methodName of methodNames) {
            const verb = Reflect.getMetadata('method:verb', prototype, methodName);
            const subPath = Reflect.getMetadata('method:path', prototype, methodName);

            if (verb) {
                routes.push({
                    verb,
                    path: basePath + subPath,
                    handler: `${Controller.name}#${methodName}`
                });
            }
        }
    }

    console.log('📋 注册的路由：');
    routes.forEach(r => {
        console.log(`  ${r.verb.padEnd(6)} ${r.path.padEnd(20)} → ${r.handler}`);
    });
}

// ================================================================
// 启动"框架"
// ================================================================

bootstrap([UserController, OrderController]);

console.log('\n✅ NestJS 原理小结：');
console.log('   ① @Controller / @Get 是 TS 装饰器，class 加载时立即执行');
console.log('   ② 装饰器用 Reflect.defineMetadata 给 class/方法存元数据');
console.log('   ③ 装饰器不修改类本身，class 代码保持干净');
console.log('   ④ 框架启动时扫描所有 class，读元数据，注册路由');
console.log('   ⑤ 用法像 Java 注解，底层是 TS 装饰器 + reflect-metadata');
