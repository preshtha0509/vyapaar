package com.vyapaar.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyTest {

    @Test
    void groupsDigitsTheIndianWay() {
        assertEquals("0.00", Money.grouped(0.0));
        assertEquals("999.00", Money.grouped(999.0));
        assertEquals("1,000.00", Money.grouped(1000.0));
        assertEquals("1,00,000.00", Money.grouped(100000.0));
        assertEquals("4,52,000.00", Money.grouped(452000.0));
        assertEquals("1,23,45,678.50", Money.grouped(12345678.5));
    }

    @Test
    void addsTheRupeeSign() {
        assertEquals("₹4,52,000.00", Money.rupees(452000.0));
    }

    @Test
    void handlesNegativesAndNulls() {
        assertEquals("-1,500.00", Money.grouped(-1500.0));
        assertEquals("0.00", Money.grouped(null));
    }

    @Test
    void roundsToTwoDecimals() {
        assertEquals("10.01", Money.grouped(10.005));
        assertEquals("10.00", Money.grouped(9.999));
    }

    @Test
    void quantityDropsTrailingZeros() {
        assertEquals("3", Money.quantity(3.0));
        assertEquals("2.5", Money.quantity(2.5));
        assertEquals("0.25", Money.quantity(0.25));
        assertEquals("0", Money.quantity(null));
    }
}
