package com.sanad.portal.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Citizen status lookup and result (/track, /track/{id}). */
public class TrackPage extends Page {

    public TrackPage(WebDriver driver) {
        super(driver);
    }

    public TrackPage open() {
        go("/track");
        return waitForForm();
    }

    TrackPage waitForForm() {
        visible(testId("track-form"));
        return this;
    }

    TrackPage waitForResult() {
        visible(testId("result-status"));
        return this;
    }

    public TrackPage lookUp(String trackingId, String email) {
        type(By.id("trackingId"), trackingId);
        type(By.id("email"), email);
        reloadsPage(() -> click(testId("track-submit")));
        wait.until(d -> !d.findElements(testId("result-status")).isEmpty()
                || !d.findElements(testId("lookup-error")).isEmpty()
                || !d.findElements(testId("error-trackingId")).isEmpty());
        return this;
    }

    public boolean hasResult() {
        return isPresent(testId("result-status"));
    }

    public String lookupError() {
        return visible(testId("lookup-error")).getText();
    }

    public String status() {
        return visible(testId("result-status")).getText().trim();
    }

    public String trackingId() {
        return visible(testId("result-tracking-id")).getText().trim();
    }

    public List<String> timelineTitles() {
        return visible(testId("timeline")).findElements(By.cssSelector(".t-title")).stream()
                .map(WebElement::getText).toList();
    }

    public String rejectionReason() {
        return visible(testId("rejection-reason")).getText().trim();
    }

    public CertificatePage openCertificate() {
        reloadsPage(() -> click(testId("view-certificate")));
        return new CertificatePage(driver).waitUntilLoaded();
    }

    public TrackPage refresh() {
        reloadsPage(() -> driver.navigate().refresh());
        return waitForResult();
    }
}
