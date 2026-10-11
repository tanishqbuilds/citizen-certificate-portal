package com.sanad.portal.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root for a citizen's certificate request. All status changes go through
 * this class so that the lifecycle rules and the audit trail can never be bypassed.
 */
@Entity
@Table(name = "certificate_request")
public class CertificateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    private String trackingId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "certificate_type", nullable = false, updatable = false)
    private CertificateType certificateType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "national_id_masked", nullable = false)
    private String nationalIdMasked;

    @Column(name = "national_id_hash", nullable = false)
    private String nationalIdHash;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String district;

    @Column(name = "pin_code", nullable = false)
    private String pinCode;

    @Column(nullable = false)
    private String purpose;

    @Column(name = "annual_income")
    private Long annualIncome;

    @Column(name = "spouse_name")
    private String spouseName;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "document_content_type", nullable = false)
    private String documentContentType;

    @Column(name = "document_size", nullable = false)
    private long documentSize;

    @Column(name = "document_sha256", nullable = false)
    private String documentSha256;

    @Column(name = "document_path", nullable = false)
    private String documentPath;

    @Column(name = "certificate_number", unique = true)
    private String certificateNumber;

    @Column(name = "certificate_hash")
    private String certificateHash;

    @Column(name = "decision_remarks")
    private String decisionRemarks;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Version
    private int version;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt ASC, id ASC")
    private List<StatusEvent> history = new ArrayList<>();

    protected CertificateRequest() {
    }

    /** Applicant-supplied details, already validated by the web layer. */
    public record Applicant(String name, LocalDate dateOfBirth, String email, String phone,
                            String nationalIdMasked, String nationalIdHash,
                            String address, String district, String pinCode, String purpose,
                            Long annualIncome, String spouseName) {
    }

    /** Metadata of the stored supporting document. */
    public record Document(String name, String contentType, long size, String sha256, String path) {
    }

    public static CertificateRequest submit(String trackingId, CertificateType type, Applicant applicant,
                                            Document document, Instant now) {
        CertificateRequest r = new CertificateRequest();
        r.trackingId = trackingId;
        r.certificateType = type;
        r.status = RequestStatus.SUBMITTED;
        r.applicantName = applicant.name();
        r.dateOfBirth = applicant.dateOfBirth();
        r.email = applicant.email();
        r.phone = applicant.phone();
        r.nationalIdMasked = applicant.nationalIdMasked();
        r.nationalIdHash = applicant.nationalIdHash();
        r.address = applicant.address();
        r.district = applicant.district();
        r.pinCode = applicant.pinCode();
        r.purpose = applicant.purpose();
        r.annualIncome = type == CertificateType.INCOME ? applicant.annualIncome() : null;
        r.spouseName = type == CertificateType.MARRIAGE ? applicant.spouseName() : null;
        r.documentName = document.name();
        r.documentContentType = document.contentType();
        r.documentSize = document.size();
        r.documentSha256 = document.sha256();
        r.documentPath = document.path();
        r.submittedAt = now;
        r.updatedAt = now;
        r.history.add(new StatusEvent(r, null, RequestStatus.SUBMITTED, "citizen",
                "Request submitted with " + document.name(), now));
        return r;
    }

    /** The email on file must match (case-insensitive) for a citizen to see the request. */
    public boolean isOwnedBy(String candidateEmail) {
        return candidateEmail != null && email.equalsIgnoreCase(candidateEmail.trim());
    }

    void transition(RequestStatus next, String actor, String remarks, Instant now) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStatusTransitionException(status, next);
        }
        history.add(new StatusEvent(this, status, next, actor, remarks, now));
        this.status = next;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public String getTrackingId() { return trackingId; }
    public CertificateType getCertificateType() { return certificateType; }
    public RequestStatus getStatus() { return status; }
    public String getApplicantName() { return applicantName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getNationalIdMasked() { return nationalIdMasked; }
    public String getNationalIdHash() { return nationalIdHash; }
    public String getAddress() { return address; }
    public String getDistrict() { return district; }
    public String getPinCode() { return pinCode; }
    public String getPurpose() { return purpose; }
    public Long getAnnualIncome() { return annualIncome; }
    public String getSpouseName() { return spouseName; }
    public String getDocumentName() { return documentName; }
    public String getDocumentContentType() { return documentContentType; }
    public long getDocumentSize() { return documentSize; }
    public String getDocumentSha256() { return documentSha256; }
    public String getDocumentPath() { return documentPath; }
    public String getCertificateNumber() { return certificateNumber; }
    public String getCertificateHash() { return certificateHash; }
    public String getDecisionRemarks() { return decisionRemarks; }
    public String getReviewedBy() { return reviewedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public List<StatusEvent> getHistory() { return Collections.unmodifiableList(history); }

    public List<StatusEvent> getHistoryNewestFirst() {
        List<StatusEvent> copy = new ArrayList<>(history);
        Collections.reverse(copy);
        return copy;
    }
}
