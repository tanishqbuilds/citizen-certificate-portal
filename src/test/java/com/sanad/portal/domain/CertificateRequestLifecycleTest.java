package com.sanad.portal.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** CCR-10 state machine and CCR-12 audit trail, without Spring. */
class CertificateRequestLifecycleTest {

    static final Instant T0 = Instant.parse("2026-10-01T05:00:00Z");

    static CertificateRequest submitted() {
        return CertificateRequest.submit("CCR-2026-ABCDEF", CertificateType.INCOME,
                new CertificateRequest.Applicant("Aarav Sharma", LocalDate.of(1998, 4, 12), "a@example.com", "9876543210",
                        "XXXX XXXX 0124", "f".repeat(64), "Flat 4B, Lotus Residency", "Pune", "411001",
                        "Scholarship application", 450_000L, null),
                new CertificateRequest.Document("slip.pdf", "application/pdf", 100, "0".repeat(64), "x/y.pdf"), T0);
    }

    @Test
    void happyPathRecordsEveryTransition() {
        CertificateRequest r = submitted();
        r.startReview("officer", T0.plusSeconds(60));
        r.approve("officer", "Matches income proof", "SND-INC-2026-000001", "a".repeat(64), T0.plusSeconds(120));

        assertThat(r.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(r.getCertificateNumber()).isEqualTo("SND-INC-2026-000001");
        assertThat(r.getDecidedAt()).isEqualTo(T0.plusSeconds(120));
        assertThat(r.getHistory()).extracting(StatusEvent::getToStatus)
                .containsExactly(RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW, RequestStatus.APPROVED);
        assertThat(r.getHistory()).extracting(StatusEvent::getActor).containsExactly("citizen", "officer", "officer");
    }

    @Test
    void rejectionKeepsTheReason() {
        CertificateRequest r = submitted();
        r.startReview("officer", T0);
        r.reject("officer", "  Document is illegible, please upload a clearer scan.  ", T0);

        assertThat(r.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(r.getDecisionRemarks()).isEqualTo("Document is illegible, please upload a clearer scan.");
        assertThat(r.getCertificateNumber()).isNull();
    }

    @Test
    void cannotDecideBeforeReviewStarts() {
        CertificateRequest r = submitted();
        assertThatThrownBy(() -> r.approve("officer", null, "N", "H", T0))
                .isInstanceOf(IllegalStatusTransitionException.class).hasMessageContaining("submitted");
        assertThat(r.getHistory()).hasSize(1);
    }

    @Test
    void rejectionWithoutReasonIsRefused() {
        CertificateRequest r = submitted();
        r.startReview("officer", T0);
        assertThatThrownBy(() -> r.reject("officer", "   ", T0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(r.getStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);
    }

    @Test
    void finalStatesAreTerminal() {
        CertificateRequest r = submitted();
        r.startReview("officer", T0);
        r.reject("officer", "Wrong document type attached", T0);
        assertThatThrownBy(() -> r.startReview("officer", T0)).isInstanceOf(IllegalStatusTransitionException.class);
    }

    @ParameterizedTest
    @EnumSource(RequestStatus.class)
    void onlyDocumentedTransitionsAreAllowed(RequestStatus from) {
        for (RequestStatus to : RequestStatus.values()) {
            boolean expected = (from == RequestStatus.SUBMITTED && to == RequestStatus.UNDER_REVIEW)
                    || (from == RequestStatus.UNDER_REVIEW && (to == RequestStatus.APPROVED || to == RequestStatus.REJECTED));
            assertThat(from.canTransitionTo(to)).as(from + " → " + to).isEqualTo(expected);
        }
    }
}
