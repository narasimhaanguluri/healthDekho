package com.healthdekho.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyFormatter {
    private MoneyFormatter() {
    }

    public static String format(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
