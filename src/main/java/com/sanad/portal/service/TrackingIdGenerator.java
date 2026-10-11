package com.sanad.portal.service;

import com.sanad.portal.domain.CertificateRequestRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Year;

/**
 * Generates non-sequential tracking IDs such as {@code CCR-2026-7K4QXZ}.
 * The alphabet omits look-alike characters (0/O, 1/I/L) so IDs survive being read over the phone.
 * 31^6 ≈ 887 million combinations per year makes guessing impractical.
 */
@Component
public class TrackingIdGenerator {

    static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    public static final String PATTERN = "CCR-\\d{4}-[" + ALPHABET + "]{6}";

    private final SecureRandom random = new SecureRandom();
    private final CertificateRequestRepository repository;
    private final Clock clock;

    public TrackingIdGenerator(CertificateRequestRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public String next() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = candidate();
            if (!repository.existsByTrackingId(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique tracking ID");
    }

    String candidate() {
        StringBuilder sb = new StringBuilder("CCR-").append(Year.now(clock).getValue()).append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
