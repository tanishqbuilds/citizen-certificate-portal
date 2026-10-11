package com.sanad.portal.e2e;

import com.sanad.portal.e2e.pages.ApplyPage;
import com.sanad.portal.e2e.pages.TrackPage;
import com.sanad.portal.e2e.support.BaseJourney;
import com.sanad.portal.e2e.support.TestData;
import com.sanad.portal.e2e.support.TestData.Applicant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

/** TC-03 — A citizen tracks a request from a different browser using tracking ID + email. */
@DisplayName("TC-03 Citizen tracks a request")
class StatusTrackingJourney extends BaseJourney {

    @Test
    @DisplayName("tracking needs the right email; ID is case-insensitive; malformed IDs are rejected")
    void trackingRequiresMatchingEmail() {
        Applicant kabir = TestData.applicant("domicile");
        String trackingId = new ApplyPage(driver()).open().fill(kabir).confirmDeclaration().submit().trackingId();

        WebDriver otherDevice = newBrowser();
        try {
            TrackPage track = new TrackPage(otherDevice).open().lookUp(trackingId, "someone.else@example.com");
            assertThat(track.hasResult()).isFalse();
            assertThat(track.lookupError()).contains("find a request with that tracking ID and email");

            track.open().lookUp("CCR-1", kabir.email());
            assertThat(track.hasResult()).isFalse();

            track.open().lookUp(trackingId.toLowerCase(), kabir.email().toUpperCase());
            assertThat(track.trackingId()).isEqualTo(trackingId);
            assertThat(track.status()).isEqualTo("Submitted");
        } finally {
            otherDevice.quit();
        }
    }
}
