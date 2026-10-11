package com.sanad.portal.e2e.pages;

import org.openqa.selenium.WebDriver;

/** Shown once after a successful submission. */
public class AcknowledgementPage extends Page {

    public AcknowledgementPage(WebDriver driver) {
        super(driver);
    }

    AcknowledgementPage waitUntilLoaded() {
        visible(testId("acknowledgement"));
        return this;
    }

    public String trackingId() {
        return visible(testId("tracking-id")).getText().trim();
    }

    public String certificateType() {
        return visible(testId("ack-type")).getText().trim();
    }

    public TrackPage trackStatus() {
        reloadsPage(() -> click(testId("go-track")));
        return new TrackPage(driver).waitForResult();
    }
}
