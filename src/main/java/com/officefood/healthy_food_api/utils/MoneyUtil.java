package com.officefood.healthy_food_api.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {
    private MoneyUtil() { }
    public static double round2(double v) {
        return new BigDecimal(Double.toString(v)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
