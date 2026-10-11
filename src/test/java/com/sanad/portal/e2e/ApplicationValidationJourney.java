package com.sanad.portal.e2e;

import com.sanad.portal.e2e.pages.ApplyPage;
import com.sanad.portal.e2e.support.BaseJourney;
import com.sanad.portal.e2e.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** TC-02 — Invalid data and a disguised document are rejected before anything reaches an officer. */
@DisplayName("TC-02 Server-side validation of data and documents")
class ApplicationValidationJourney extends BaseJourney {

    @Test
    @DisplayName("invalid fields and a text file renamed to .pdf are reported, input is kept")
    void invalidApplicationIsRejectedWithFieldMessages() {
        ApplyPage form = new ApplyPage(driver()).open()
                .fill(TestData.applicant("invalid"))
                .submitExpectingErrors();

        assertThat(form.errorSummary()).contains("things need your attention");
        assertThat(form.fieldsWithErrors()).contains(
                "applicantName", "dateOfBirth", "email", "phone", "nationalId", "pinCode", "purpose",
                "annualIncome", "declaration", "document");
        assertThat(form.fieldError("nationalId")).contains("check digit");
        assertThat(form.fieldError("document")).contains("Only PDF, PNG or JPEG");
        assertThat(form.fieldError("pinCode")).contains("6-digit PIN");

        // What the citizen typed is preserved
        assertThat(form.valueOf("district")).isEqualTo("Pune");
        assertThat(form.currentPath()).startsWith("/apply");
    }

    @Test
    @DisplayName("a valid form with one bad field fails only on that field")
    void singleInvalidFieldIsPinpointed() {
        TestData.Applicant ok = TestData.applicant("birth");
        ApplyPage form = new ApplyPage(driver()).open().fill(ok).confirmDeclaration();
        form.attach("not-really-a-pdf.pdf").submitExpectingErrors();

        assertThat(form.fieldsWithErrors()).containsExactly("document");
        assertThat(form.errorSummary()).contains("One thing needs your attention");
    }
}
