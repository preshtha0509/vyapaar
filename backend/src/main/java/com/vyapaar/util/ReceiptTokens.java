package com.vyapaar.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;

/**
 * Receipt identifiers.
 *
 * The public token is the only thing protecting a receipt link, so it must come from SecureRandom.
 * java.util.Random and UUID.randomUUID() are not acceptable here.
 */
public final class ReceiptTokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private ReceiptTokens() {
    }

    /** 32 random bytes as URL-safe Base64 without padding, so it is 43 characters long. */
    public static String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Human-readable receipt number, for example VY/2026-27/0042.
     * The year part is the Indian financial year of the receipt date, which runs April to March.
     */
    public static String receiptNumber(LocalDate date, long sequence) {
        return "VY/" + financialYear(date) + "/" + String.format("%04d", sequence);
    }

    /** "2026-27" for any date from 1 April 2026 to 31 March 2027. */
    public static String financialYear(LocalDate date) {
        int startYear = date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
        return startYear + "-" + String.format("%02d", (startYear + 1) % 100);
    }

    /** First day of the financial year containing the given date. */
    public static LocalDate financialYearStart(LocalDate date) {
        int startYear = date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
        return LocalDate.of(startYear, 4, 1);
    }

    /** Last day of the financial year containing the given date. */
    public static LocalDate financialYearEnd(LocalDate date) {
        return financialYearStart(date).plusYears(1).minusDays(1);
    }
}
