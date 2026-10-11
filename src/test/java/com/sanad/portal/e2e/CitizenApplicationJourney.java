package com.sanad.portal.e2e;

import com.sanad.portal.e2e.pages.AcknowledgementPage;
import com.sanad.portal.e2e.pages.ApplyPage;
import com.sanad.portal.e2e.pages.HomePage;
import com.sanad.portal.e2e.pages.TrackPage;
import com.sanad.portal.e2e.support.BaseJourney;
import com.sanad.portal.e2e.support.TestData;
import com.sanad.portal.e2e.support.TestData.Applicant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** TC-01 — A citizen applies for a certificate and receives a tracking ID. */
@DisplayName("TC-01 Citizen submits a certificate application")
class CitizenApplicationJourney extends BaseJourney {

    @Test
    @DisplayName("valid income application → tracking ID → status 'Submitted'")
    void citizenAppliesAndSeesSubmittedStatus() {
        Applicant aarav = TestData.applicant("income");

        ApplyPage form = new HomePage(driver()).open().startApplication();
        AcknowledgementPage ack = form.fill(aarav).confirmDeclaration().submit();

        String trackingId = ack.trackingId();
        assertThat(trackingId).matches("CCR-\\d{4}-[A-Z2-9]{6}");
        assertThat(ack.certificateType()).isEqualTo("Income certificate");

        TrackPage status = ack.trackStatus();
        assertThat(status.trackingId()).isEqualTo(trackingId);
        assertThat(status.status()).isEqualTo("Submitted");
        assertThat(status.timelineTitles()).containsExactly("Request submitted");
    }

    @Test
    @DisplayName("type-specific fields appear only for the chosen certificate")
    void conditionalFieldsFollowTheCertificateType() {
        ApplyPage form = new ApplyPage(driver()).open();

        form.chooseType("INCOME");
        assertThat(form.isFieldVisible("annualIncome")).isTrue();
        assertThat(form.isFieldVisible("spouseName")).isFalse();

        form.chooseType("MARRIAGE");
        assertThat(form.isFieldVisible("annualIncome")).isFalse();
        assertThat(form.isFieldVisible("spouseName")).isTrue();
    }
}
