package com.sanad.portal.e2e.support;

import java.nio.file.Path;

/**
 * Settings for the Selenium suite, passed as system properties by Maven failsafe (see the e2e profile in pom.xml).
 *
 * <pre>
 * e2e.baseUrl          application under test               default http://localhost:18080
 * e2e.remoteUrl        Selenium Grid URL; empty = local Chrome  default ""
 * e2e.headless         run Chrome without a window           default true
 * e2e.screenshotDir    where screenshots are written         default target/selenium-screenshots
 * e2e.captureAll       also screenshot passing journeys      default false
 * e2e.officerUser      officer account                       default officer
 * e2e.officerPassword  officer password                      default Officer@123
 * e2e.timeoutSeconds   explicit-wait timeout                 default 15
 * </pre>
 */
public final class E2EConfig {

    private E2EConfig() {
    }

    public static String baseUrl() {
        return strip(System.getProperty("e2e.baseUrl", "http://localhost:18080"));
    }

    public static String remoteUrl() {
        return System.getProperty("e2e.remoteUrl", "").trim();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("e2e.headless", "true"));
    }

    public static Path screenshotDir() {
        return Path.of(System.getProperty("e2e.screenshotDir", "target/selenium-screenshots"));
    }

    public static boolean captureAll() {
        return Boolean.parseBoolean(System.getProperty("e2e.captureAll", "false"));
    }

    public static String officerUser() {
        return System.getProperty("e2e.officerUser", "officer");
    }

    public static String officerPassword() {
        String fromEnv = System.getenv("E2E_OFFICER_PASSWORD");
        return System.getProperty("e2e.officerPassword", fromEnv != null && !fromEnv.isBlank() ? fromEnv : "Officer@123");
    }

    public static int timeoutSeconds() {
        return Integer.parseInt(System.getProperty("e2e.timeoutSeconds", "15"));
    }

    private static String strip(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
