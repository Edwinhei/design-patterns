/**
 * ============================================================
 * 🚫 不用解释器 · 硬编码字符串解析
 * ============================================================
 *
 * 运行方式：
 *   java BadInterpreter.java
 */

public class BadInterpreter {

    public static void main(String[] args) {
        System.out.println("=== 土办法：字符串硬解析 ===\n");

        System.out.println("3 + 5 = " + evaluate("3 + 5"));
        System.out.println("10 - 4 = " + evaluate("10 - 4"));

        // 这个能跑吗？
        try {
            System.out.println("3 + 5 - 2 = " + evaluate("3 + 5 - 2"));
        } catch (Exception e) {
            System.out.println("❌ 复杂表达式无法解析: " + e.getMessage());
        }

        System.out.println("\n⚠️  问题：");
        System.out.println("1. 硬编码解析逻辑，每加一种语法都要改");
        System.out.println("2. 多级运算（3+5-2）、括号、优先级 → 代码爆炸");
        System.out.println("3. 变量 / 函数 → 完全做不到");
        System.out.println("4. 错误处理极困难");

        System.out.println("\n👉 看 InterpreterDemo.java 如何用解释器模式优雅表达语法");
    }

    // 极简解析：只支持"单步二元运算"
    static int evaluate(String expr) {
        expr = expr.replace(" ", "");

        int plus = expr.indexOf('+');
        int minus = expr.indexOf('-');

        if (plus > 0) {
            return Integer.parseInt(expr.substring(0, plus))
                 + Integer.parseInt(expr.substring(plus + 1));
        }
        if (minus > 0) {
            return Integer.parseInt(expr.substring(0, minus))
                 - Integer.parseInt(expr.substring(minus + 1));
        }
        throw new IllegalArgumentException("解析失败");
    }
}
