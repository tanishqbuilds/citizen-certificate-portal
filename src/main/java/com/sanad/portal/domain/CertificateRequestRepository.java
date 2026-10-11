package com.sanad.portal.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {

    @EntityGraph(attributePaths = "history")
    Optional<CertificateRequest> findByTrackingId(String trackingId);

    boolean existsByTrackingId(String trackingId);

    long countByStatus(RequestStatus status);

    /** Officer queue (CCR-9): optional status filter, optional search on tracking ID or applicant name. */
    @Query("""
            select r from CertificateRequest r
            where (:status is null or r.status = :status)
              and (:q is null or upper(r.trackingId) like :q or upper(r.applicantName) like :q)
            order by r.submittedAt asc
            """)
    List<CertificateRequest> search(@Param("status") RequestStatus status, @Param("q") String q, Pageable page);
}
