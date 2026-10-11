package com.sanad.portal.e2e.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

/**
 * Every journey gets a fresh browser session (no shared cookies), so journeys are independent and can run in any order.
 */
@Tag("e2e")
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseJourney {

    private WebDriver driver;

    @BeforeEach
    void openBrowser() {
        driver = DriverFactory.create();
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    public WebDriver driver() {
        return driver;
    }

    /** A second, independent browser — e.g. an officer acting while the citizen's session stays open. */
    protected WebDriver newBrowser() {
        return DriverFactory.create();
    }
}
