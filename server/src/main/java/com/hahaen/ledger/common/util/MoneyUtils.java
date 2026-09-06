package com.hahaen.ledger.common.util;

import com.hahaen.ledger.common.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {
    private static final BigDecimal MAX = new BigDecimal("999999999.99");
    private MoneyUtils() {}
    public static long toCents(String value) {
        try {
            BigDecimal amount = new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY);
            if (amount.signum() <= 0 || amount.compareTo(MAX) > 0) throw new NumberFormatException();
            return amount.movePointRight(2).longValueExact();
        } catch (Exception e) { throw new BusinessException("INVALID_AMOUNT", "金额必须在 ¥0.01～¥999,999,999.99 之间"); }
    }
    public static String format(long cents) { return BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.HALF_UP).toPlainString(); }
}
