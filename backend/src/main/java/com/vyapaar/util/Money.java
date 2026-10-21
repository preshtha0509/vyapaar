package com.vyapaar.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Indian rupee formatting with Indian digit grouping: the last three digits, then groups of two.
 * 452000 becomes 4,52,000.00.
 *
 * The grouping is done by hand rather than with NumberFormat so the output does not depend on which
 * CLDR locale data the JVM happens to ship with.
 */
public final class Money {

    private Money() {
    }

    /** "₹4,52,000.00" */
    public static String rupees(Double value) {
        return "₹" + grouped(value);
    }

    /** "4,52,000.00" with no currency symbol. */
    public static String grouped(Double value) {
        BigDecimal amount = BigDecimal.valueOf(value == null ? 0.0 : value)
                .setScale(2, RoundingMode.HALF_UP);

        boolean negative = amount.signum() < 0;
        String plain = amount.abs().toPlainString();

        int dot = plain.indexOf('.');
        String whole = dot < 0 ? plain : plain.substring(0, dot);
        String fraction = dot < 0 ? "00" : plain.substring(dot + 1);

        return (negative ? "-" : "") + groupIndian(whole) + "." + fraction;
    }

    /** Plain number with up to three decimals and no trailing zeros, for quantities. */
    public static String quantity(Double value) {
        BigDecimal amount = BigDecimal.valueOf(value == null ? 0.0 : value)
                .setScale(3, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return amount.scale() <= 0 ? amount.toBigInteger().toString() : amount.toPlainString();
    }

    private static String groupIndian(String whole) {
        if (whole.length() <= 3) {
            return whole;
        }
        String lastThree = whole.substring(whole.length() - 3);
        String rest = whole.substring(0, whole.length() - 3);

        StringBuilder grouped = new StringBuilder();
        while (rest.length() > 2) {
            grouped.insert(0, "," + rest.substring(rest.length() - 2));
            rest = rest.substring(0, rest.length() - 2);
        }
        if (!rest.isEmpty()) {
            grouped.insert(0, rest);
        }
        return grouped + "," + lastThree;
    }
}
