package com.sanad.portal.e2e.pages;

import com.sanad.portal.e2e.support.E2EConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OfficerLoginPage extends Page {

    public OfficerLoginPage(WebDriver driver) {
        super(driver);
    }

    public OfficerLoginPage open() {
        go("/officer/login");
        return waitUntilLoaded();
    }

    public OfficerLoginPage waitUntilLoaded() {
        visible(testId("login-form"));
        return this;
    }

    public OfficerQueuePage signIn() {
        return signIn(E2EConfig.officerUser(), E2EConfig.officerPassword());
    }

    public OfficerQueuePage signIn(String user, String password) {
        submit(user, password);
        return new OfficerQueuePage(driver).waitUntilLoaded();
    }

    public OfficerLoginPage signInExpectingFailure(String user, String password) {
        submit(user, password);
        visible(testId("login-error"));
        return this;
    }

    private void submit(String user, String password) {
        type(By.id("username"), user);
        type(By.id("password"), password);
        reloadsPage(() -> click(testId("login-submit")));
    }

    public String error() {
        return visible(testId("login-error")).getText();
    }
}
