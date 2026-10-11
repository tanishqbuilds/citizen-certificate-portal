package com.sanad.portal.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Officer request detail with the decision panel. */
public class OfficerRequestPage extends Page {

    public OfficerRequestPage(WebDriver driver) {
        super(driver);
    }

    OfficerRequestPage waitUntilLoaded() {
        visible(testId("detail-status"));
        return this;
    }

    public String status() {
        return visible(testId("detail-status")).getText().trim();
    }

    public String applicantName() {
        return visible(testId("detail-name")).getText().trim();
    }

    public OfficerRequestPage startReview() {
        reloadsPage(() -> click(testId("start-review")));
        visible(testId("panel-decide"));
        return this;
    }

    public OfficerRequestPage approve(String remarks) {
        type(By.id("remarks"), remarks);
        reloadsPage(() -> click(testId("approve")));
        visible(testId("panel-approved"));
        return this;
    }

    public OfficerRequestPage reject(String reason) {
        type(By.id("remarks"), reason);
        reloadsPage(() -> click(testId("reject")));
        wait.until(d -> !d.findElements(testId("panel-rejected")).isEmpty()
                || !d.findElements(testId("error-remarks")).isEmpty());
        return this;
    }

    public String remarksError() {
        return visible(testId("error-remarks")).getText();
    }

    public String flashSuccess() {
        return visible(testId("flash-success")).getText();
    }

    public String certificateNumber() {
        return visible(testId("certificate-number")).getText().trim();
    }

    public List<String> auditTrail() {
        return visible(testId("audit-trail")).findElements(By.cssSelector(".t-title")).stream()
                .map(WebElement::getText).toList();
    }
}
