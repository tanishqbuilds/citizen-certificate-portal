package com.sanad.portal.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class OfficerQueuePage extends Page {

    public OfficerQueuePage(WebDriver driver) {
        super(driver);
    }

    OfficerQueuePage waitUntilLoaded() {
        visible(testId("kpis"));
        return this;
    }

    public int kpi(String name) {
        return Integer.parseInt(visible(testId("kpi-" + name)).getText().trim());
    }

    public OfficerQueuePage search(String query) {
        type(testId("queue-search"), query);
        reloadsPage(() -> driver.findElement(testId("queue-search")).sendKeys(Keys.ENTER));
        return waitUntilLoaded();
    }

    public boolean shows(String trackingId) {
        return isPresent(testId("row-" + trackingId));
    }

    public OfficerRequestPage open(String trackingId) {
        reloadsPage(() -> click(By.cssSelector("[data-testid='row-" + trackingId + "'] a")));
        return new OfficerRequestPage(driver).waitUntilLoaded();
    }

    public OfficerLoginPage signOut() {
        reloadsPage(() -> click(testId("nav-logout")));
        return new OfficerLoginPage(driver).waitUntilLoaded();
    }
}
