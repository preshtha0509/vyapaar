package com.vyapaar.util;

import java.util.Optional;

/**
 * Turns the phone numbers typed into the app into the digits-only form that wa.me expects.
 * Customers are Indian, so a bare 10-digit number gets the 91 country code.
 */
public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    /**
     * Returns country code and number with no plus sign and no separators, for example
     * 919876543210. Returns empty when the input cannot be read as an Indian mobile number.
     */
    public static Optional<String> toE164India(String raw) {
        if (raw == null) {
            return Optional.empty();
        }

        String digits = raw.replaceAll("[^0-9]", "");

        if (digits.length() == 10) {
            return startsWithMobilePrefix(digits) ? Optional.of("91" + digits) : Optional.empty();
        }
        if (digits.length() == 11 && digits.startsWith("0")) {
            String local = digits.substring(1);
            return startsWithMobilePrefix(local) ? Optional.of("91" + local) : Optional.empty();
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            String local = digits.substring(2);
            return startsWithMobilePrefix(local) ? Optional.of(digits) : Optional.empty();
        }
        return Optional.empty();
    }

    // Indian mobile numbers start with 6, 7, 8 or 9. This rejects landlines and typos early.
    private static boolean startsWithMobilePrefix(String tenDigits) {
        char first = tenDigits.charAt(0);
        return first >= '6' && first <= '9';
    }
}
