package com.sanad.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/** One immutable entry in a request's audit trail (CCR-12). */
@Entity
@Table(name = "status_event")
public class StatusEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id")
    private CertificateRequest request;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "from_status")
    private RequestStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "to_status", nullable = false)
    private RequestStatus toStatus;

    @Column(nullable = false)
    private String actor;

    private String remarks;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected StatusEvent() {
    }

    StatusEvent(CertificateRequest request, RequestStatus fromStatus, RequestStatus toStatus,
                String actor, String remarks, Instant occurredAt) {
        this.request = request;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actor = actor;
        this.remarks = remarks;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public RequestStatus getFromStatus() { return fromStatus; }
    public RequestStatus getToStatus() { return toStatus; }
    public String getActor() { return actor; }
    public String getRemarks() { return remarks; }
    public Instant getOccurredAt() { return occurredAt; }
}
