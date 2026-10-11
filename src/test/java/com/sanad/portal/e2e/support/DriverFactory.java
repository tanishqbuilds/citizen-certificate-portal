package com.sanad.portal.e2e.support;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

/**
 * Local Chrome (driver resolved by Selenium Manager) or a remote Selenium Grid, chosen by {@code e2e.remoteUrl}.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        ChromeOptions options = new ChromeOptions();
        if (E2EConfig.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,900", "--disable-gpu", "--no-sandbox",
                "--disable-dev-shm-usage", "--disable-search-engine-choice-screen", "--lang=en-IN");
        options.setAcceptInsecureCerts(true);
        // Chrome flags demo passwords such as "Officer@123" as breached and opens a native
        // "Change your password" bubble after sign-in, which swallows every following click.
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));

        WebDriver driver;
        String remote = E2EConfig.remoteUrl();
        if (remote.isEmpty()) {
            driver = new ChromeDriver(options);
        } else {
            try {
                RemoteWebDriver rwd = new RemoteWebDriver(URI.create(remote).toURL(), options);
                rwd.setFileDetector(new LocalFileDetector()); // upload files from this machine to the grid node
                driver = rwd;
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid e2e.remoteUrl: " + remote, e);
            }
        }
        driver.manage().window().setSize(new Dimension(1440, 900));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // explicit waits only
        return driver;
    }
}
