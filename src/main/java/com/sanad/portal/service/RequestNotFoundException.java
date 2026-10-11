package com.sanad.portal.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(String trackingId) {
        super("No request " + trackingId);
    }
}
