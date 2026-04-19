/**
 * ============================================================
 * ✅ 解释器模式 · 数学表达式求值
 * ============================================================
 *
 * 设计：
 *   - Expression 接口（所有文法节点）
 *   - 终结符：NumberExpr / VariableExpr
 *   - 非终结符：AddExpr / SubExpr / MulExpr
 *   - Context：保存变量值
 *
 * 运行方式：
 *   java InterpreterDemo.java
 */

import java.util.*;

public class InterpreterDemo {

    public static void main(String[] args) {
        Context ctx = new Context();

        // === 场景 1：纯数字表达式 ===
        // (3 + 5) - 2
        Expression e1 = new SubExpr(
            new AddExpr(new NumberExpr(3), new NumberExpr(5)),
            new NumberExpr(2)
        );
        System.out.println("表达式: (3 + 5) - 2 = " + e1.interpret(ctx));

        // === 场景 2：带变量 ===
        ctx.set("a", 10);
        ctx.set("b", 20);

        Expression e2 = new AddExpr(
            new VariableExpr("a"),
            new VariableExpr("b")
        );
        System.out.println("表达式: a + b (a=10, b=20) = " + e2.interpret(ctx));

        // === 场景 3：复杂嵌套 ===
        // (a + b) * 3 - 5
        Expression e3 = new SubExpr(
            new MulExpr(
                new AddExpr(
                    new VariableExpr("a"),
                    new VariableExpr("b")
                ),
                new NumberExpr(3)
            ),
            new NumberExpr(5)
        );
        System.out.println("表达式: (a + b) * 3 - 5 = " + e3.interpret(ctx));

        // === 场景 4：改变量值 → 同表达式不同结果 ===
        ctx.set("a", 100);
        System.out.println("改 a=100 后，同表达式 = " + e3.interpret(ctx));

        System.out.println("\n✨ 解释器模式的威力：");
        System.out.println("  ① 每种语法规则 = 一个类");
        System.out.println("  ② 表达式 = 树结构（AST）");
        System.out.println("  ③ 递归求值");
        System.out.println("  ④ 加新操作符（除法/幂）→ 加一个类");
        System.out.println("  ⑤ 支持变量 + Context 上下文");
    }
}

// ================================================================
// Context：存储变量
// ================================================================
class Context {
    private final Map<String, Integer> variables = new HashMap<>();

    public void set(String name, int value) {
        variables.put(name, value);
    }

    public int get(String name) {
        Integer v = variables.get(name);
        if (v == null) throw new IllegalArgumentException("未定义变量: " + name);
        return v;
    }
}

// ================================================================
// 抽象表达式
// ================================================================
interface Expression {
    int interpret(Context ctx);
}

// ================================================================
// 终结符：数字
// ================================================================
class NumberExpr implements Expression {
    private final int value;

    public NumberExpr(int value) {
        this.value = value;
    }

    public int interpret(Context ctx) {
        return value;
    }
}

// ================================================================
// 终结符：变量
// ================================================================
class VariableExpr implements Expression {
    private final String name;

    public VariableExpr(String name) {
        this.name = name;
    }

    public int interpret(Context ctx) {
        return ctx.get(name);
    }
}

// ================================================================
// 非终结符：加法
// ================================================================
class AddExpr implements Expression {
    private final Expression left;
    private final Expression right;

    public AddExpr(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public int interpret(Context ctx) {
        return left.interpret(ctx) + right.interpret(ctx);     // 递归求值
    }
}

// ================================================================
// 非终结符：减法
// ================================================================
class SubExpr implements Expression {
    private final Expression left;
    private final Expression right;

    public SubExpr(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public int interpret(Context ctx) {
        return left.interpret(ctx) - right.interpret(ctx);
    }
}

// ================================================================
// 非终结符：乘法（练习：加除法就照葫芦画瓢）
// ================================================================
class MulExpr implements Expression {
    private final Expression left;
    private final Expression right;

    public MulExpr(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public int interpret(Context ctx) {
        return left.interpret(ctx) * right.interpret(ctx);
    }
}
