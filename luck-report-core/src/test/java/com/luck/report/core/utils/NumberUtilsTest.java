package com.luck.report.core.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NumberUtilsTest {

    @Test
    void largeDouble_avoidsScientificNotation() {
        assertEquals("10653980", NumberUtils.toPlainString(10653980d));
        assertEquals("10000000", NumberUtils.toPlainString(1e7));
    }

    @Test
    void smallDouble_avoidsScientificNotation() {
        assertEquals("0.0001", NumberUtils.toPlainString(1e-4));
    }

    @Test
    void midRangeDouble_keepsPlainDecimal() {
        assertEquals("9999999", NumberUtils.toPlainString(9999999d));
        assertEquals("0.001", NumberUtils.toPlainString(0.001d));
    }

    @Test
    void bigDecimal_usesPlainString() {
        assertEquals("10653980", NumberUtils.toPlainString(new BigDecimal("1.065398E7")));
    }

    @Test
    void nonNumber_passthrough() {
        assertEquals("hello", NumberUtils.toPlainString("hello"));
        assertEquals("10,653,980", NumberUtils.toPlainString("10,653,980"));
        assertNull(NumberUtils.toPlainString(null));
    }
}
