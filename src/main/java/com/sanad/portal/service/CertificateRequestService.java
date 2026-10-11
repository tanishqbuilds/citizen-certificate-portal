package com.sanad.portal.service;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.config.Zones;
import com.sanad.portal.domain.CertificateRequestRepository;
import com.sanad.portal.domain.RequestStatus;
import com.sanad.portal.web.form.ApplicationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Application service for the certificate request lifecycle. */
@Service
public class CertificateRequestService {

    private static final Logger log = LoggerFactory.getLogger(CertificateRequestService.class);

    private final CertificateRequestRepository repository;
    private final DocumentStorageService documents;
    private final TrackingIdGenerator trackingIds;
    private final Clock clock;

    public CertificateRequestService(CertificateRequestRepository repository,
                                     DocumentStorageService documents,
                                     TrackingIdGenerator trackingIds,
                                     Clock clock) {
        this.repository = repository;
        this.documents = documents;
        this.trackingIds = trackingIds;
        this.clock = clock;
    }

    /** CCR-3/5/6: stores the document, persists the request and returns it with its tracking ID. */
    @Transactional
    public CertificateRequest submit(ApplicationForm form, MultipartFile document) {
        String trackingId = trackingIds.next();
        CertificateRequest.Document stored = documents.store(trackingId, document);
        CertificateRequest.Applicant applicant = new CertificateRequest.Applicant(
                form.getApplicantName(), form.getDateOfBirth(), form.getEmail(), form.getPhone(),
                NationalIds.mask(form.getNationalId()), NationalIds.fingerprint(form.getNationalId()),
                form.getAddress(), form.getDistrict(), form.getPinCode(), form.getPurpose(),
                form.getAnnualIncome(), form.getSpouseName());
        CertificateRequest saved;
        try {
            // Flush now so constraint violations surface here, while we can still clean up the file.
            saved = repository.saveAndFlush(
                    CertificateRequest.submit(trackingId, form.getCertificateType(), applicant, stored, now()));
        } catch (RuntimeException e) {
            documents.delete(stored.path());
            throw e;
        }
        log.info("Request {} submitted ({})", saved.getTrackingId(), saved.getCertificateType());
        return saved;
    }

    /** CCR-7: a citizen sees a request only with the matching email. Unknown and mismatched look the same. */
    @Transactional(readOnly = true)
    public Optional<CertificateRequest> findForCitizen(String trackingId, String email) {
        return repository.findByTrackingId(trackingId).filter(r -> r.isOwnedBy(email));
    }

    @Transactional(readOnly = true)
    public Optional<CertificateRequest> findByTrackingId(String trackingId) {
        return repository.findByTrackingId(trackingId);
    }

    // ------------------------------------------------------------------ officer side (CCR-9 … CCR-12)

    /** Counts per status for the KPI cards. */
    public record Dashboard(long submitted, long underReview, long approved, long rejected) {
        public long total() {
            return submitted + underReview + approved + rejected;
        }

        public long pending() {
            return submitted + underReview;
        }
    }

    @Transactional(readOnly = true)
    public Dashboard dashboard() {
        return new Dashboard(repository.countByStatus(RequestStatus.SUBMITTED),
                repository.countByStatus(RequestStatus.UNDER_REVIEW),
                repository.countByStatus(RequestStatus.APPROVED),
                repository.countByStatus(RequestStatus.REJECTED));
    }

    public static final int QUEUE_LIMIT = 200;

    /** Oldest first, so requests are handled in arrival order. Capped at {@link #QUEUE_LIMIT} rows for the MVP. */
    @Transactional(readOnly = true)
    public List<CertificateRequest> queue(RequestStatus status, String query) {
        String q = query == null || query.isBlank() ? null : "%" + query.trim().toUpperCase() + "%";
        return repository.search(status, q, PageRequest.of(0, QUEUE_LIMIT));
    }

    @Transactional(readOnly = true)
    public CertificateRequest get(String trackingId) {
        return repository.findByTrackingId(trackingId).orElseThrow(() -> new RequestNotFoundException(trackingId));
    }

    @Transactional
    public CertificateRequest startReview(String trackingId, String officer) {
        CertificateRequest r = get(trackingId);
        r.startReview(officer, now());
        log.info("Request {} under review by {}", trackingId, officer);
        return repository.saveAndFlush(r);
    }

    @Transactional
    public CertificateRequest approve(String trackingId, String officer, String remarks) {
        CertificateRequest r = get(trackingId);
        Instant now = now();
        String number = "SND-%s-%d-%06d".formatted(r.getCertificateType().getCode(),
                now.atZone(Zones.BUSINESS).getYear(), r.getId());
        String hash = NationalIds.sha256Hex(String.join("|", number, r.getTrackingId(), r.getApplicantName(),
                r.getDateOfBirth().toString(), r.getCertificateType().name(), now.toString()));
        r.approve(officer, remarks, number, hash, now);
        log.info("Request {} approved by {} — certificate {}", trackingId, officer, number);
        return repository.saveAndFlush(r);
    }

    @Transactional
    public CertificateRequest reject(String trackingId, String officer, String reason) {
        CertificateRequest r = get(trackingId);
        r.reject(officer, reason, now());
        log.info("Request {} rejected by {}", trackingId, officer);
        return repository.saveAndFlush(r);
    }

    public Resource document(CertificateRequest r) {
        return documents.load(r.getDocumentPath());
    }

    /** CCR-11/13: public verification only ever matches approved requests. */
    @Transactional(readOnly = true)
    public Optional<CertificateRequest> findIssuedCertificate(String certificateNumber) {
        return repository.findByCertificateNumberAndStatus(certificateNumber, RequestStatus.APPROVED);
    }

    private Instant now() {
        return clock.instant();
    }
}
