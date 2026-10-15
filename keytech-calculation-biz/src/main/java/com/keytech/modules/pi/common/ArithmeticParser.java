package com.keytech.modules.pi.common;

import cn.hutool.extra.expression.engine.jexl.JexlEngine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Stack;

/**
 * 四则运算
 * @author aolei
 */
public class ArithmeticParser {

    // 定义操作符优先级
    private static final int PRECEDENCE_ADD = 1;
    private static final int PRECEDENCE_SUBTRACT = 1;
    private static final int PRECEDENCE_MULTIPLY = 2;
    private static final int PRECEDENCE_DIVIDE = 2;

    // 判断是否是操作符
    private static boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    // 获取操作符的优先级
    private static int precedence(char op) {
        switch (op) {
            case '+':
            case '-':
                return PRECEDENCE_ADD;
            case '*':
            case '/':
                return PRECEDENCE_MULTIPLY;
        }
        return -1;
    }

    // 应用操作符到两个操作数
    private static double applyOperation(double a, double b, char op) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
        }
        return 0;
    }

    // 解析并计算表达式
    public static double evaluate(String expression) {
        // 移除空格
        expression = expression.replaceAll("\\s+", "");

        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (Character.isDigit(ch) || ch == '.') {
                // 解析数字
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() &&
                        (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i++));
                }
                i--; // 因为上面的while循环会使i多走一步
                values.push(Double.parseDouble(sb.toString()));
            } else if (isOperator(ch)) {
                // 处理操作符
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(ch)) {
                    double val2 = values.pop();
                    double val1 = values.pop();
                    char op = ops.pop();
                    values.push(applyOperation(val1, val2, op));
                }
                ops.push(ch);
            } else if (ch == '(') {
                // 左括号，直接压栈
                ops.push(ch);
            } else if (ch == ')') {
                // 右括号，处理所有直到左括号的操作
                while (!ops.isEmpty() && ops.peek() != '(') {
                    double val2 = values.pop();
                    double val1 = values.pop();
                    char op = ops.pop();
                    values.push(applyOperation(val1, val2, op));
                }
                if (!ops.isEmpty()) {
                    ops.pop(); // 弹出左括号
                }
            }
        }

        // 处理栈中剩余的操作符和操作数
        while (!ops.isEmpty()) {
            double val2 = values.pop();
            double val1 = values.pop();
            char op = ops.pop();
            values.push(applyOperation(val1, val2, op));
        }

        // 栈中应该只剩下一个值，即最终结果
        return values.pop();
    }

    public static void main(String[] args) {
//        String expression = "2678-3";
//        try {
//            double result = evaluate(expression);
//            System.out.println("Result: " + result);
//        } catch (NumberFormatException e) {
//            System.out.println("Invalid number format in expression.");
//        } catch (ArithmeticException e) {
//            System.out.println(e.getMessage());
//        }
        String expressionString = "-3--3";
        JexlEngine jexlEngine = new JexlEngine();
        Object obj = jexlEngine.eval(expressionString,null);
        System.out.println(obj);

    }
}
