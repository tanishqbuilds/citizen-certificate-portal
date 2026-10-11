package com.sanad.portal.e2e.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Loads applicants from {@code src/test/resources/e2e/applicants.csv} and resolves sample documents. */
public final class TestData {

    /** One row of the CSV. Emails are made unique per run so journeys never collide on a shared environment. */
    public record Applicant(String key, String certificateType, String name, String dateOfBirth, String email,
                            String phone, String nationalId, String address, String district, String pinCode,
                            String purpose, String annualIncome, String spouseName, String document) {
    }

    private static final String RUN = Long.toString(System.currentTimeMillis(), 36);
    private static final Map<String, Applicant> APPLICANTS = load();

    private TestData() {
    }

    public static Applicant applicant(String key) {
        return Objects.requireNonNull(APPLICANTS.get(key), "No test applicant '" + key + "'");
    }

    public static Path document(String fileName) {
        try {
            return Path.of(Objects.requireNonNull(TestData.class.getResource("/e2e/documents/" + fileName),
                    "Missing test document " + fileName).toURI()).toAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Applicant> load() {
        Map<String, Applicant> map = new LinkedHashMap<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(TestData.class.getResourceAsStream("/e2e/applicants.csv")), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] f = line.split("\\|", -1);
                String email = f[4].contains("@") || f[4].startsWith("not-") ? f[4] : f[4] + "+e2e-" + RUN + "@example.com";
                map.put(f[0], new Applicant(f[0], f[1], f[2], f[3], email, f[5], f[6], f[7], f[8], f[9], f[10],
                        f[11], f[12], f[13]));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read test data", e);
        }
        return map;
    }
}
