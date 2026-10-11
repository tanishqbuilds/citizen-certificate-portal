package com.sanad.portal.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Citizen status lookup (CCR-7): tracking ID plus the email used on the application. */
public class TrackForm {

    @NotBlank(message = "Enter your tracking ID.")
    @Pattern(regexp = "^\\s*[Cc][Cc][Rr]-\\d{4}-[A-Za-z0-9]{6}\\s*$", message = "Tracking IDs look like CCR-2026-7K4QXZ.")
    private String trackingId;

    @NotBlank(message = "Enter the email address used on the application.")
    @Email(message = "Enter a valid email address.")
    private String email;

    public String normalizedTrackingId() {
        return trackingId == null ? null : trackingId.trim().toUpperCase();
    }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
