package com.sanad.portal.e2e.pages;

import org.openqa.selenium.WebDriver;

public class HomePage extends Page {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        go("/");
        visible(testId("cta-apply"));
        return this;
    }

    public ApplyPage startApplication() {
        click(testId("cta-apply"));
        return new ApplyPage(driver).waitUntilLoaded();
    }

    public TrackPage goToTracking() {
        click(testId("nav-track"));
        return new TrackPage(driver).waitForForm();
    }

    public String buildInfo() {
        return visible(testId("build-info")).getText();
    }
}
