package com.sanad.portal.service;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.domain.CertificateRequestRepository;
import com.sanad.portal.domain.RequestStatus;
import com.sanad.portal.web.form.ApplicationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
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

    /** CCR-11/13: public verification only ever matches approved requests. */
    @Transactional(readOnly = true)
    public Optional<CertificateRequest> findIssuedCertificate(String certificateNumber) {
        return repository.findByCertificateNumberAndStatus(certificateNumber, RequestStatus.APPROVED);
    }

    private Instant now() {
        return clock.instant();
    }
}
