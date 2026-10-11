package com.sanad.portal.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ViewFormatterTest {

    final ViewFormatter fmt = new ViewFormatter(Clock.fixed(Instant.parse("2026-10-11T12:00:00Z"), ZoneOffset.UTC));

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {"0|₹0", "999|₹999", "1000|₹1,000", "450000|₹4,50,000", "100000000|₹10,00,00,000"})
    void formatsRupeesWithIndianGrouping(long amount, String expected) {
        assertThat(fmt.rupees(amount)).isEqualTo(expected);
    }

    @Test
    void relativeTimes() {
        assertThat(fmt.relative(Instant.parse("2026-10-11T11:59:30Z"))).isEqualTo("just now");
        assertThat(fmt.relative(Instant.parse("2026-10-11T11:15:00Z"))).isEqualTo("45 min ago");
        assertThat(fmt.relative(Instant.parse("2026-10-10T12:00:00Z"))).isEqualTo("1 day ago");
    }

    @Test
    void masksEmailAndComputesAgeInIst() {
        assertThat(fmt.maskEmail("aarav@example.com")).isEqualTo("a••••@example.com");
        assertThat(fmt.age(LocalDate.of(2008, 10, 11))).isEqualTo(18);
        assertThat(fmt.clause("For a scholarship.")).isEqualTo("For a scholarship");
    }
}
