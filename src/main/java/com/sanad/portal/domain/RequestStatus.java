package com.sanad.portal.domain;

import java.util.Set;

/**
 * Request lifecycle. Allowed transitions:
 * SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED.
 */
public enum RequestStatus {

    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under review"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }

    public boolean canTransitionTo(RequestStatus next) {
        return allowedNext().contains(next);
    }

    public Set<RequestStatus> allowedNext() {
        return switch (this) {
            case SUBMITTED -> Set.of(UNDER_REVIEW);
            case UNDER_REVIEW -> Set.of(APPROVED, REJECTED);
            case APPROVED, REJECTED -> Set.of();
        };
    }
}
