package com.sanad.portal.web;

import com.sanad.portal.config.Zones;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatting helpers for templates, used as {@code ${@fmt.dateTime(x)}}. All times shown in IST. */
@Component("fmt")
public class ViewFormatter {

    private static final ZoneId ZONE = Zones.BUSINESS;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private final Clock clock;

    public ViewFormatter(Clock clock) {
        this.clock = clock;
    }

    public String dateTime(Instant instant) {
        return instant == null ? "—" : DATE_TIME.format(instant.atZone(ZONE));
    }

    public String date(Instant instant) {
        return instant == null ? "—" : DATE.format(instant.atZone(ZONE));
    }

    public String date(LocalDate date) {
        return date == null ? "—" : DATE.format(date);
    }

    /** "just now", "5 min ago", "3 h ago", "2 days ago", then an absolute date. */
    public String relative(Instant instant) {
        if (instant == null) return "—";
        Duration d = Duration.between(instant, clock.instant());
        if (d.isNegative() || d.toSeconds() < 60) return "just now";
        if (d.toMinutes() < 60) return d.toMinutes() + " min ago";
        if (d.toHours() < 24) return d.toHours() + " h ago";
        if (d.toDays() < 7) return d.toDays() + (d.toDays() == 1 ? " day ago" : " days ago");
        return date(instant);
    }

    /** Indian digit grouping: 4,50,000 and 1,00,00,000 (lakh / crore), which java.text cannot express. */
    public String rupees(Long amount) {
        if (amount == null) return "—";
        String digits = Long.toString(Math.abs(amount));
        StringBuilder sb = new StringBuilder();
        int len = digits.length();
        for (int i = 0; i < len; i++) {
            sb.append(digits.charAt(i));
            int remaining = len - i - 1;
            if (remaining > 0 && (remaining == 3 || (remaining > 3 && (remaining - 3) % 2 == 0))) {
                sb.append(',');
            }
        }
        return (amount < 0 ? "-₹" : "₹") + sb;
    }

    /** Drops trailing punctuation so text can be embedded mid-sentence. */
    public String clause(String text) {
        return text == null ? "" : text.strip().replaceAll("[.!;,\\s]+$", "");
    }

    public String size(long bytes) {
        return com.sanad.portal.service.DocumentStorageService.humanSize(bytes);
    }

    /** a••••@example.com — enough for the citizen to recognise, not enough to harvest. */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "—";
        int at = email.indexOf('@');
        return email.charAt(0) + "•".repeat(Math.max(1, Math.min(at - 1, 5))) + email.substring(at);
    }

    public int age(LocalDate dateOfBirth) {
        return dateOfBirth == null ? 0 : java.time.Period.between(dateOfBirth, LocalDate.now(clock.withZone(ZONE))).getYears();
    }

    public String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}
