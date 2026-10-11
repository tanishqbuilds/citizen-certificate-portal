package com.sanad.portal.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {

    @EntityGraph(attributePaths = "history")
    Optional<CertificateRequest> findByTrackingId(String trackingId);

    boolean existsByTrackingId(String trackingId);

    Optional<CertificateRequest> findByCertificateNumberAndStatus(String certificateNumber, RequestStatus status);
}
