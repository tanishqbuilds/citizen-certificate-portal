package com.sanad.portal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class VerhoeffTest {

    @ParameterizedTest
    @ValueSource(strings = {"2363", "234567890124", "987654321012", "456789012341"})
    void acceptsNumbersWithCorrectCheckDigit(String number) {
        assertThat(Verhoeff.isValid(number)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2364", "234567890123", "243567890124", "", "12a4"})
    void rejectsWrongDigitsTranspositionsAndGarbage(String number) {
        assertThat(Verhoeff.isValid(number)).isFalse();
    }

    @Test
    void checkDigitRoundTrips() {
        String base = "56473829101";
        assertThat(Verhoeff.isValid(base + Verhoeff.checkDigit(base))).isTrue();
    }
}
