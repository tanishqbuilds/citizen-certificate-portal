package com.sanad.portal.web;

import com.sanad.portal.domain.CertificateRequestRepository;
import com.sanad.portal.domain.RequestStatus;
import com.sanad.portal.support.TestDocuments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CCR-8 … CCR-12 through the full stack.
 * <p>
 * The officer password is pinned with a test property: those outrank OS environment variables, so the
 * test is unaffected by an {@code APP_OFFICER_PASSWORD} exported on the build machine (e.g. Jenkins).
 */
@SpringBootTest(properties = "app.officer.password=" + OfficerWorkflowIntegrationTest.PASSWORD)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class OfficerWorkflowIntegrationTest {

    static final String PASSWORD = "Test@12345";
    static final RequestPostProcessor OFFICER = user("officer").roles("OFFICER");

    @Autowired
    MockMvc mvc;

    @Autowired
    CertificateRequestRepository repository;

    String trackingId;
    MockHttpSession citizen;

    @BeforeEach
    void submitOne() throws Exception {
        citizen = new MockHttpSession();
        String location = mvc.perform(CitizenFlowIntegrationTest.validIncomeApplication(TestDocuments.pdf("slip.pdf")).session(citizen))
                .andReturn().getResponse().getRedirectedUrl();
        trackingId = location.substring(location.lastIndexOf('/') + 1);
    }

    @Test
    void officerAreaRequiresSignIn() throws Exception {
        mvc.perform(get("/officer")).andExpect(redirectedUrl("/officer/login"));
        mvc.perform(get("/officer/requests/" + trackingId + "/document")).andExpect(redirectedUrl("/officer/login"));
    }

    @Test
    void formLoginWithConfiguredCredentials() throws Exception {
        mvc.perform(formLogin("/officer/login").user("officer").password(PASSWORD))
                .andExpect(authenticated().withRoles("OFFICER"))
                .andExpect(redirectedUrl("/officer"));
        mvc.perform(formLogin("/officer/login").user("officer").password("wrong-password"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/officer/login?error"));
    }

    @Test
    void queueShowsKpisAndTheNewRequest() throws Exception {
        mvc.perform(get("/officer").with(OFFICER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(trackingId)))
                .andExpect(content().string(containsString("data-testid=\"kpi-submitted\"")));
        mvc.perform(get("/officer").param("status", "APPROVED").param("q", trackingId).with(OFFICER))
                .andExpect(content().string(containsString("Nothing here")));
    }

    @Test
    void approveIssuesACertificateAndCitizenCanOpenIt() throws Exception {
        mvc.perform(post("/officer/requests/" + trackingId + "/start-review").with(OFFICER).with(csrf()))
                .andExpect(redirectedUrl("/officer/requests/" + trackingId));
        mvc.perform(post("/officer/requests/" + trackingId + "/approve").param("remarks", "Income proof verified")
                        .with(OFFICER).with(csrf()))
                .andExpect(flash().attribute("success", containsString("SND-INC-")));

        var r = repository.findByTrackingId(trackingId).orElseThrow();
        assertThat(r.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(r.getCertificateNumber()).matches("SND-INC-\\d{4}-\\d{6}");
        assertThat(r.getCertificateHash()).hasSize(64);
        assertThat(r.getHistory()).hasSize(3);

        mvc.perform(get("/certificate/" + trackingId).session(citizen))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(r.getCertificateNumber())));
        // A stranger without a verified session gets a 404, not the certificate.
        mvc.perform(get("/certificate/" + trackingId)).andExpect(status().isNotFound());
    }

    @Test
    void rejectRequiresAReason() throws Exception {
        mvc.perform(post("/officer/requests/" + trackingId + "/start-review").with(OFFICER).with(csrf()));
        mvc.perform(post("/officer/requests/" + trackingId + "/reject").param("remarks", "no").with(OFFICER).with(csrf()))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.decision"));
        assertThat(repository.findByTrackingId(trackingId).orElseThrow().getStatus()).isEqualTo(RequestStatus.UNDER_REVIEW);

        mvc.perform(post("/officer/requests/" + trackingId + "/reject")
                        .param("remarks", "The income proof is for a different person.").with(OFFICER).with(csrf()))
                .andExpect(flash().attribute("success", containsString("rejected")));
        mvc.perform(get("/track/" + trackingId).session(citizen))
                .andExpect(content().string(containsString("The income proof is for a different person.")));
    }

    @Test
    void illegalTransitionIsShownNotApplied() throws Exception {
        mvc.perform(post("/officer/requests/" + trackingId + "/approve").with(OFFICER).with(csrf()))
                .andExpect(flash().attribute("failure", containsString("cannot be moved to approved")));
        assertThat(repository.findByTrackingId(trackingId).orElseThrow().getStatus()).isEqualTo(RequestStatus.SUBMITTED);
    }

    @Test
    void certificateOfUnapprovedRequestIsNotFound() throws Exception {
        mvc.perform(get("/certificate/" + trackingId).session(citizen)).andExpect(status().isNotFound());
    }

    @Test
    void officerCanOpenTheDocumentInline() throws Exception {
        mvc.perform(get("/officer/requests/" + trackingId + "/document").with(OFFICER))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Disposition", containsString("inline")));
    }

    @Test
    void unknownRequestIs404() throws Exception {
        mvc.perform(get("/officer/requests/CCR-2026-ZZZZZZ").with(OFFICER)).andExpect(status().isNotFound());
    }
}
