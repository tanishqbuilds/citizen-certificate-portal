package com.sanad.portal.e2e;

import com.sanad.portal.e2e.pages.ApplyPage;
import com.sanad.portal.e2e.pages.OfficerLoginPage;
import com.sanad.portal.e2e.pages.OfficerRequestPage;
import com.sanad.portal.e2e.pages.TrackPage;
import com.sanad.portal.e2e.support.BaseJourney;
import com.sanad.portal.e2e.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

/** TC-05 — A rejection must carry a reason, and the citizen sees it. */
@DisplayName("TC-05 Officer rejects with a mandatory reason")
class OfficerRejectionJourney extends BaseJourney {

    static final String REASON = "The invitation does not show the date of the ceremony. Please upload the registration form.";

    @Test
    @DisplayName("reject without a reason is refused; with a reason the citizen sees it")
    void rejectionReasonReachesTheCitizen() {
        TrackPage citizen = new ApplyPage(driver()).open().fill(TestData.applicant("marriage"))
                .confirmDeclaration().submit().trackStatus();
        String trackingId = citizen.trackingId();

        WebDriver officerBrowser = newBrowser();
        try {
            OfficerRequestPage request = new OfficerLoginPage(officerBrowser).open().signIn()
                    .search(trackingId).open(trackingId)
                    .startReview();

            request.reject("No");
            assertThat(request.remarksError()).contains("at least 10 characters");
            assertThat(request.status()).isEqualTo("Under review");

            request.reject(REASON);
            assertThat(request.status()).isEqualTo("Rejected");
        } finally {
            officerBrowser.quit();
        }

        citizen.refresh();
        assertThat(citizen.status()).isEqualTo("Rejected");
        assertThat(citizen.rejectionReason()).isEqualTo(REASON);
    }
}
