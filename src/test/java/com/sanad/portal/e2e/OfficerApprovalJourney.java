package com.sanad.portal.e2e;

import com.sanad.portal.e2e.pages.ApplyPage;
import com.sanad.portal.e2e.pages.CertificatePage;
import com.sanad.portal.e2e.pages.OfficerLoginPage;
import com.sanad.portal.e2e.pages.OfficerQueuePage;
import com.sanad.portal.e2e.pages.OfficerRequestPage;
import com.sanad.portal.e2e.pages.TrackPage;
import com.sanad.portal.e2e.support.BaseJourney;
import com.sanad.portal.e2e.support.TestData;
import com.sanad.portal.e2e.support.TestData.Applicant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

/** TC-04 — End to end: citizen applies, officer reviews and approves, citizen opens the certificate. */
@DisplayName("TC-04 Officer approves and the citizen receives a certificate")
class OfficerApprovalJourney extends BaseJourney {

    @Test
    @DisplayName("submit → start review → approve → certificate visible to the citizen")
    void approvalIssuesACertificate() {
        Applicant aarav = TestData.applicant("income");
        TrackPage citizen = new ApplyPage(driver()).open().fill(aarav).confirmDeclaration().submit().trackStatus();
        String trackingId = citizen.trackingId();

        WebDriver officerBrowser = newBrowser();
        try {
            OfficerQueuePage queue = new OfficerLoginPage(officerBrowser).open().signIn();
            assertThat(queue.kpi("submitted")).isPositive();
            OfficerRequestPage request = queue.search(trackingId).open(trackingId);
            assertThat(request.applicantName()).isEqualTo(aarav.name());
            assertThat(request.status()).isEqualTo("Submitted");

            request.startReview();
            assertThat(citizen.refresh().status()).isEqualTo("Under review");

            request.approve("Salary slip matches the declared annual income.");
            assertThat(request.status()).isEqualTo("Approved");
            assertThat(request.flashSuccess()).contains("has been issued");
            String number = request.certificateNumber();
            assertThat(number).matches("SND-INC-\\d{4}-\\d{6}");
            assertThat(request.auditTrail()).containsExactly(
                    "Under review → Approved", "Submitted → Under review", "Submitted");

            assertThat(citizen.refresh().status()).isEqualTo("Approved");
            CertificatePage certificate = citizen.openCertificate();
            assertThat(certificate.certificateNumber()).isEqualTo(number);
            assertThat(certificate.verificationHash()).hasSize(64);
            assertThat(certificate.text()).contains(aarav.name(), "₹4,50,000");
        } finally {
            officerBrowser.quit();
        }
    }
}
