package com.vyapaar.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptTokensTest {

    @Test
    void tokenIsLongAndUrlSafe() {
        String token = ReceiptTokens.newToken();
        assertEquals(43, token.length());
        assertTrue(token.matches("[A-Za-z0-9_-]+"), token);
    }

    @Test
    void tokensDoNotRepeat() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            seen.add(ReceiptTokens.newToken());
        }
        assertEquals(1000, seen.size());
    }

    @Test
    void financialYearRunsAprilToMarch() {
        assertEquals("2026-27", ReceiptTokens.financialYear(LocalDate.of(2026, 7, 19)));
        assertEquals("2026-27", ReceiptTokens.financialYear(LocalDate.of(2026, 4, 1)));
        assertEquals("2026-27", ReceiptTokens.financialYear(LocalDate.of(2027, 3, 31)));
        assertEquals("2027-28", ReceiptTokens.financialYear(LocalDate.of(2027, 4, 1)));
        assertEquals("2025-26", ReceiptTokens.financialYear(LocalDate.of(2026, 1, 15)));
    }

    @Test
    void financialYearBoundsAreCorrect() {
        LocalDate inside = LocalDate.of(2026, 7, 19);
        assertEquals(LocalDate.of(2026, 4, 1), ReceiptTokens.financialYearStart(inside));
        assertEquals(LocalDate.of(2027, 3, 31), ReceiptTokens.financialYearEnd(inside));

        LocalDate january = LocalDate.of(2027, 1, 5);
        assertEquals(LocalDate.of(2026, 4, 1), ReceiptTokens.financialYearStart(january));
        assertEquals(LocalDate.of(2027, 3, 31), ReceiptTokens.financialYearEnd(january));
    }

    @Test
    void receiptNumberIsZeroPadded() {
        assertEquals("VY/2026-27/0042", ReceiptTokens.receiptNumber(LocalDate.of(2026, 7, 19), 42));
        assertEquals("VY/2026-27/0001", ReceiptTokens.receiptNumber(LocalDate.of(2026, 7, 19), 1));
        assertEquals("VY/2026-27/1234", ReceiptTokens.receiptNumber(LocalDate.of(2026, 7, 19), 1234));
    }
}
