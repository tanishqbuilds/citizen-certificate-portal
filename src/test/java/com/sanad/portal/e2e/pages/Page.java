package com.sanad.portal.e2e.pages;

import com.sanad.portal.e2e.support.E2EConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Base page object: navigation and explicit waits on {@code data-testid} hooks (never on styling). */
public abstract class Page {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected Page(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(E2EConfig.timeoutSeconds()));
    }

    protected static By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    protected void go(String path) {
        driver.get(E2EConfig.baseUrl() + path);
    }

    protected WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected boolean isPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }

    protected void type(By by, String text) {
        WebElement el = visible(by);
        el.clear();
        if (text != null && !text.isEmpty()) {
            el.sendKeys(text);
        }
    }

    protected void click(By by) {
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    /**
     * Runs an action that submits a form or follows a link, and waits until the browser has replaced the page.
     * Without this, a wait for an element that exists on both pages (e.g. a status badge) can match the old page.
     */
    protected void reloadsPage(Runnable action) {
        WebElement oldRoot = driver.findElement(By.tagName("html"));
        action.run();
        wait.until(ExpectedConditions.stalenessOf(oldRoot));
    }

    public String title() {
        return driver.getTitle();
    }

    public String currentPath() {
        return driver.getCurrentUrl().replace(E2EConfig.baseUrl(), "");
    }
}
