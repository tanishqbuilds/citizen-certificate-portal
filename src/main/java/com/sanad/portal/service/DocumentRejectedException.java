package com.sanad.portal.service;

/** The uploaded supporting document failed validation. The message is safe to show to the citizen. */
public class DocumentRejectedException extends RuntimeException {

    public DocumentRejectedException(String message) {
        super(message);
    }
}
