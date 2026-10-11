package com.sanad.portal.e2e.pages;

import com.sanad.portal.e2e.support.TestData;
import com.sanad.portal.e2e.support.TestData.Applicant;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** The citizen application form (/apply). */
public class ApplyPage extends Page {

    private static final By FORM = testId("apply-form");
    private static final By SUBMIT = testId("submit-application");
    private static final By ERROR_SUMMARY = testId("form-errors");

    public ApplyPage(WebDriver driver) {
        super(driver);
    }

    public ApplyPage open() {
        go("/apply");
        return waitUntilLoaded();
    }

    ApplyPage waitUntilLoaded() {
        visible(FORM);
        return this;
    }

    /** The radio inputs are transparent overlays; click the visible card (its label). */
    public ApplyPage chooseType(String type) {
        click(By.xpath("//input[@id='type-" + type + "']/ancestor::label"));
        return this;
    }

    public ApplyPage fill(Applicant a) {
        chooseType(a.certificateType());
        type(By.id("applicantName"), a.name());
        setDate("dateOfBirth", a.dateOfBirth());
        type(By.id("nationalId"), a.nationalId());
        type(By.id("email"), a.email());
        type(By.id("phone"), a.phone());
        type(By.id("address"), a.address());
        type(By.id("district"), a.district());
        type(By.id("pinCode"), a.pinCode());
        type(By.id("purpose"), a.purpose());
        if (!a.annualIncome().isEmpty()) type(By.id("annualIncome"), a.annualIncome());
        if (!a.spouseName().isEmpty()) type(By.id("spouseName"), a.spouseName());
        attach(a.document());
        return this;
    }

    /** Native date inputs depend on the browser locale when typed into; set the ISO value directly. */
    private void setDate(String id, String isoDate) {
        WebElement el = visible(By.id(id));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change'))", el, isoDate);
    }

    public ApplyPage attach(String fileName) {
        driver.findElement(By.id("document")).sendKeys(TestData.document(fileName).toString());
        return this;
    }

    public ApplyPage confirmDeclaration() {
        click(By.cssSelector("label.checkbox"));
        return this;
    }

    public boolean isFieldVisible(String id) {
        return driver.findElement(By.id(id)).isDisplayed();
    }

    public AcknowledgementPage submit() {
        reloadsPage(() -> click(SUBMIT));
        return new AcknowledgementPage(driver).waitUntilLoaded();
    }

    /** Submit expecting the server to reject the form. */
    public ApplyPage submitExpectingErrors() {
        reloadsPage(() -> click(SUBMIT));
        visible(ERROR_SUMMARY);
        return this;
    }

    public String errorSummary() {
        return visible(ERROR_SUMMARY).getText();
    }

    public String fieldError(String field) {
        return visible(testId("error-" + field)).getText();
    }

    public boolean hasFieldError(String field) {
        return isPresent(testId("error-" + field));
    }

    public String valueOf(String id) {
        return driver.findElement(By.id(id)).getAttribute("value");
    }

    public List<String> fieldsWithErrors() {
        return driver.findElements(By.cssSelector("[data-testid^='error-']")).stream()
                .map(e -> e.getAttribute("data-testid").substring("error-".length())).toList();
    }
}
