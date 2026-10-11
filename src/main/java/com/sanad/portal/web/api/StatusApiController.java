package com.sanad.portal.web.api;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.domain.CertificateType;
import com.sanad.portal.domain.RequestStatus;
import com.sanad.portal.service.CertificateRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Read-only JSON API (CCR-13). Stateless and CSRF-exempt; it never returns personal data beyond
 * what the caller already proved they know (tracking ID + email), or initials for verification.
 */
@RestController
@RequestMapping(path = "/api/v1", produces = "application/json")
public class StatusApiController {

    public record CertificateTypeView(String code, String label, int slaDays, String requiredDocument) {
        static CertificateTypeView of(CertificateType t) {
            return new CertificateTypeView(t.name(), t.getLabel(), t.getSlaDays(), t.getRequiredDocument());
        }
    }

    public record StatusQuery(@NotBlank String trackingId, @NotBlank @Email String email) {
    }

    public record HistoryView(RequestStatus status, String actor, String remarks, Instant at) {
    }

    public record StatusView(String trackingId, RequestStatus status, String statusLabel, CertificateType certificateType,
                             Instant submittedAt, Instant updatedAt, String certificateNumber, List<HistoryView> history) {
        static StatusView of(CertificateRequest r) {
            return new StatusView(r.getTrackingId(), r.getStatus(), r.getStatus().getLabel(), r.getCertificateType(),
                    r.getSubmittedAt(), r.getUpdatedAt(), r.getCertificateNumber(),
                    r.getHistory().stream()
                            .map(e -> new HistoryView(e.getToStatus(), "citizen".equals(e.getActor()) ? "citizen" : "officer",
                                    e.getFromStatus() == null ? null : e.getRemarks(), e.getOccurredAt()))
                            .toList());
        }
    }

    public record VerificationView(boolean valid, String certificateNumber, CertificateType certificateType,
                                   String holderInitials, Instant issuedAt, String verificationHash) {
    }

    private final CertificateRequestService service;

    public StatusApiController(CertificateRequestService service) {
        this.service = service;
    }

    @GetMapping("/certificate-types")
    public List<CertificateTypeView> certificateTypes() {
        return Arrays.stream(CertificateType.values()).map(CertificateTypeView::of).toList();
    }

    @PostMapping(path = "/status", consumes = "application/json")
    public ResponseEntity<?> status(@Valid @RequestBody StatusQuery query) {
        return service.findForCitizen(query.trackingId().trim().toUpperCase(), query.email())
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(StatusView.of(r)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "not_found",
                        "message", "No request matches that tracking ID and email.")));
    }

    @GetMapping("/certificates/{number}/verify")
    public ResponseEntity<VerificationView> verify(@PathVariable String number) {
        return service.findIssuedCertificate(number.trim().toUpperCase())
                .map(r -> ResponseEntity.ok(new VerificationView(true, r.getCertificateNumber(), r.getCertificateType(),
                        initials(r.getApplicantName()), r.getDecidedAt(), r.getCertificateHash())))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(new VerificationView(false, number, null, null, null, null)));
    }

    private static String initials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            sb.append(Character.toUpperCase(part.charAt(0))).append('.');
        }
        return sb.toString();
    }
}
