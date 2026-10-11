package com.sanad.portal.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NationalIdsTest {

    @Test
    void ignoresSpacesAndDashes() {
        assertThat(NationalIds.isValid("2345 6789 0124")).isTrue();
        assertThat(NationalIds.isValid("2345-6789-0124")).isTrue();
    }

    @Test
    void rejectsIdsStartingWithZeroOrOne() {
        String base = "12345678901";
        assertThat(NationalIds.isValid(base + Verhoeff.checkDigit(base))).isFalse();
    }

    @Test
    void masksAllButLastFourDigits() {
        assertThat(NationalIds.mask("2345 6789 0124")).isEqualTo("XXXX XXXX 0124");
    }

    @Test
    void fingerprintIsStableAndDoesNotContainTheId() {
        String a = NationalIds.fingerprint("2345 6789 0124");
        assertThat(a).hasSize(64).isEqualTo(NationalIds.fingerprint("234567890124")).doesNotContain("234567890124");
    }
}
