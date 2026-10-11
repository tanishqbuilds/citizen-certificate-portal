package com.sanad.portal.web;

import jakarta.servlet.http.HttpSession;

import java.util.HashSet;
import java.util.Set;

/**
 * Remembers, per browser session, which tracking IDs the citizen proved ownership of (ID + email).
 * This lets /track/{id} and /certificate/{id} be bookmark-friendly GETs without putting the email in the URL.
 */
final class CitizenAccess {

    private static final String KEY = "ccrp.verifiedTrackingIds";

    private CitizenAccess() {
    }

    static void grant(HttpSession session, String trackingId) {
        verified(session).add(trackingId);
    }

    static boolean has(HttpSession session, String trackingId) {
        return session != null && verified(session).contains(trackingId);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> verified(HttpSession session) {
        Object v = session.getAttribute(KEY);
        if (v instanceof Set<?> set) {
            return (Set<String>) set;
        }
        Set<String> fresh = new HashSet<>();
        session.setAttribute(KEY, fresh);
        return fresh;
    }
}
