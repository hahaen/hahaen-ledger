package com.hahaen.ledger.common.util;
import com.hahaen.ledger.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MoneyUtilsTest {
    @Test void convertsDecimalToCentsWithoutFloatingPointError(){ assertEquals(10L, MoneyUtils.toCents("0.10")); assertEquals("100.00", MoneyUtils.format(10000)); }
    @Test void rejectsInvalidMoney(){ assertThrows(BusinessException.class, () -> MoneyUtils.toCents("0.001")); assertThrows(BusinessException.class, () -> MoneyUtils.toCents("0")); }
}
