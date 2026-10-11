package com.sanad.portal.e2e.support;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Captures a PNG screenshot, the page source and the URL the moment a journey fails — before {@code @AfterEach}
 * closes the browser. Files land in {@code <screenshotDir>/failed/} and are archived by Jenkins.
 * With {@code -De2e.captureAll=true}, passing journeys are captured too (in {@code passed/}).
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean failed = context.getExecutionException().isPresent();
        if (!failed && !E2EConfig.captureAll()) {
            return;
        }
        Object instance = context.getRequiredTestInstance();
        if (!(instance instanceof BaseJourney journey) || journey.driver() == null) {
            return;
        }
        String name = context.getRequiredTestClass().getSimpleName() + "_" + context.getRequiredTestMethod().getName()
                + "_" + LocalDateTime.now().format(STAMP);
        Path dir = E2EConfig.screenshotDir().resolve(failed ? "failed" : "passed");
        save(journey.driver(), dir, name, failed ? context.getExecutionException().get() : null);
    }

    static void save(WebDriver driver, Path dir, String name, Throwable failure) {
        try {
            Files.createDirectories(dir);
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(dir.resolve(name + ".png"), png);
            if (failure != null) {
                Files.writeString(dir.resolve(name + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
                Files.writeString(dir.resolve(name + ".txt"), "URL: " + driver.getCurrentUrl()
                        + System.lineSeparator() + "Failure: " + failure, StandardCharsets.UTF_8);
            }
            System.out.println("[e2e] screenshot saved: " + dir.resolve(name + ".png"));
        } catch (IOException | RuntimeException e) {
            System.err.println("[e2e] could not save screenshot " + name + ": " + e.getMessage());
        }
    }
}
