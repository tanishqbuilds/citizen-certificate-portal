package com.sanad.portal.domain;

public class IllegalStatusTransitionException extends RuntimeException {

    public IllegalStatusTransitionException(RequestStatus from, RequestStatus to) {
        super("A request that is " + from.getLabel().toLowerCase()
                + " cannot be moved to " + to.getLabel().toLowerCase() + ".");
    }
}
