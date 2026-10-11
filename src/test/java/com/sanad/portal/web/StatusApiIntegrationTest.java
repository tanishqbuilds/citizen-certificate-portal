package com.sanad.portal.web;

import com.sanad.portal.support.TestDocuments;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StatusApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listsCertificateTypes() throws Exception {
        mvc.perform(get("/api/v1/certificate-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[1].code").value("INCOME"))
                .andExpect(jsonPath("$[1].slaDays").value(5));
    }

    @Test
    void returnsStatusForMatchingTrackingIdAndEmail() throws Exception {
        String location = mvc.perform(CitizenFlowIntegrationTest.validIncomeApplication(TestDocuments.pdf("slip.pdf")))
                .andReturn().getResponse().getRedirectedUrl();
        String trackingId = location.substring(location.lastIndexOf('/') + 1);

        mvc.perform(post("/api/v1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingId\":\"" + trackingId + "\",\"email\":\"aarav.sharma@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId").value(trackingId))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.history", hasSize(1)))
                .andExpect(jsonPath("$.history[0].actor").value("citizen"));
    }

    @Test
    void unknownRequestIs404WithoutLeakingWhy() throws Exception {
        mvc.perform(post("/api/v1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackingId\":\"CCR-2026-ZZZZZZ\",\"email\":\"x@example.com\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void invalidBodyIs400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/status").contentType(MediaType.APPLICATION_JSON).content("{\"trackingId\":\"\",\"email\":\"nope\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void unknownCertificateFailsVerification() throws Exception {
        mvc.perform(get("/api/v1/certificates/SND-INC-2026-999999/verify"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
