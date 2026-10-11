package com.sanad.portal.web.form;

import jakarta.validation.constraints.Size;

/** Officer decision remarks. Rejection additionally requires a reason of at least 10 characters. */
public class DecisionForm {

    public static final int MIN_REASON = 10;

    @Size(max = 500, message = "Remarks must be at most 500 characters.")
    private String remarks;

    public boolean hasValidRejectionReason() {
        return remarks != null && remarks.trim().length() >= MIN_REASON;
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
