package com.sanad.portal.e2e.pages;

import org.openqa.selenium.WebDriver;

public class CertificatePage extends Page {

    public CertificatePage(WebDriver driver) {
        super(driver);
    }

    CertificatePage waitUntilLoaded() {
        visible(testId("certificate"));
        return this;
    }

    public String certificateNumber() {
        return visible(testId("cert-number")).getText().trim();
    }

    public String verificationHash() {
        return visible(testId("cert-hash")).getText().trim();
    }

    public String text() {
        return visible(testId("certificate")).getText();
    }
}
