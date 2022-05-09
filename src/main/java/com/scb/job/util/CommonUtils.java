package com.scb.job.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CommonUtils {
    private static final Integer ROUND_PLACES = 2;

    public static Double round(Double value) {
        if (value == null)
            return null;
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(ROUND_PLACES, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
