package com.vyapaar.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneNumbersTest {

    @Test
    void acceptsATenDigitMobile() {
        assertEquals(Optional.of("919876543210"), PhoneNumbers.toE164India("9876543210"));
    }

    @Test
    void stripsFormattingAndCountryCode() {
        assertEquals(Optional.of("919876543210"), PhoneNumbers.toE164India("+91 98765 43210"));
        assertEquals(Optional.of("919876543210"), PhoneNumbers.toE164India("+91-98765-43210"));
        assertEquals(Optional.of("919876543210"), PhoneNumbers.toE164India("919876543210"));
        assertEquals(Optional.of("919876543210"), PhoneNumbers.toE164India("(91) 9876543210"));
    }

    @Test
    void dropsALeadingTrunkZero() {
        assertEquals(Optional.of("919876543210"), PhoneNumbers.toE164India("09876543210"));
    }

    @Test
    void rejectsWhatItCannotUse() {
        assertTrue(PhoneNumbers.toE164India("12345").isEmpty());
        assertTrue(PhoneNumbers.toE164India("").isEmpty());
        assertTrue(PhoneNumbers.toE164India(null).isEmpty());
        assertTrue(PhoneNumbers.toE164India("abcdefghij").isEmpty());
        // Indian mobile numbers never start with 1 to 5.
        assertTrue(PhoneNumbers.toE164India("1234567890").isEmpty());
        assertTrue(PhoneNumbers.toE164India("5876543210").isEmpty());
    }
}
