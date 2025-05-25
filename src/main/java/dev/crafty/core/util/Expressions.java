package dev.crafty.core.util;

import com.googlecode.aviator.AviatorEvaluator;

public class Expressions {
    public static boolean evaluateForBoolean(String expr) {
        Object result = AviatorEvaluator.execute(expr);
        if (result instanceof Boolean) {
            return (boolean) result;
        } else {
            return false;
        }
    }
}
