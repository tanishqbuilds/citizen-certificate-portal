package com.sanad.portal.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Normalises, validates, masks and fingerprints 12-digit national IDs. The raw ID is never stored. */
public final class NationalIds {

    private NationalIds() {
    }

    public static String normalize(String raw) {
        return raw == null ? "" : raw.replaceAll("[\\s-]", "");
    }

    /** 12 digits, does not start with 0 or 1 (as for Aadhaar), valid Verhoeff checksum. */
    public static boolean isValid(String raw) {
        String id = normalize(raw);
        return id.matches("[2-9]\\d{11}") && Verhoeff.isValid(id);
    }

    public static String mask(String raw) {
        String id = normalize(raw);
        return "XXXX XXXX " + id.substring(id.length() - 4);
    }

    public static String fingerprint(String raw) {
        return sha256Hex(normalize(raw));
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
