package com.sanad.portal.web;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.domain.CertificateRequestRepository;
import com.sanad.portal.domain.RequestStatus;
import com.sanad.portal.support.TestDocuments;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** CCR-3 … CCR-7 through the full MVC stack (validation, persistence, templates). */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CitizenFlowIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    CertificateRequestRepository repository;

    static MockMultipartHttpServletRequestBuilder validIncomeApplication(MockMultipartFile document) {
        return validApplication("INCOME", document);
    }

    static MockMultipartHttpServletRequestBuilder validApplication(String type, MockMultipartFile document) {
        return (MockMultipartHttpServletRequestBuilder) multipart("/apply").file(document)
                .param("certificateType", type)
                .param("applicantName", "Aarav Sharma")
                .param("dateOfBirth", "1998-04-12")
                .param("email", "Aarav.Sharma@Example.com")
                .param("phone", "9876543210")
                .param("nationalId", TestDocuments.VALID_NATIONAL_ID)
                .param("address", "Flat 4B, Lotus Residency, MG Road")
                .param("district", "Pune")
                .param("pinCode", "411001")
                .param("purpose", "Required for a post-graduate scholarship application.")
                .param("annualIncome", "450000")
                .param("declaration", "true")
                .with(csrf());
    }

    @Test
    void applyFormRendersAllCertificateTypes() throws Exception {
        mvc.perform(get("/apply").param("type", "MARRIAGE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("type-BIRTH")))
                .andExpect(content().string(containsString("type-MARRIAGE")))
                .andExpect(content().string(containsString("Marriage certificate")));
    }

    @Test
    void validApplicationIsStoredAndAcknowledged() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MvcResult result = mvc.perform(validIncomeApplication(TestDocuments.pdf("salary-slip.pdf")).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/apply/submitted/CCR-*"))
                .andReturn();

        String trackingId = result.getResponse().getRedirectedUrl().substring("/apply/submitted/".length());
        assertThat(trackingId).matches("CCR-\\d{4}-[A-Z2-9]{6}");

        CertificateRequest saved = repository.findByTrackingId(trackingId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(saved.getEmail()).isEqualTo("aarav.sharma@example.com");
        assertThat(saved.getNationalIdMasked()).isEqualTo("XXXX XXXX 0124");
        assertThat(saved.getAnnualIncome()).isEqualTo(450_000L);
        assertThat(saved.getDocumentSha256()).hasSize(64);
        assertThat(saved.getHistory()).hasSize(1);

        // The flash attribute carries the acknowledgement to the next page, whose "Track status"
        // button must open this request's own status page (regression: ccrp-pipeline #7).
        mvc.perform(get(result.getResponse().getRedirectedUrl())
                        .flashAttrs(result.getFlashMap()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(trackingId)))
                .andExpect(content().string(containsString("href=\"/track/" + trackingId + "\" data-testid=\"go-track\"")));

        // The same session can open the status page straight away.
        mvc.perform(get("/track/" + trackingId).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Submitted")));
    }

    @Test
    void invalidFieldsAreReportedAndInputIsKept() throws Exception {
        mvc.perform(multipart("/apply").file(TestDocuments.pdf("x.pdf"))
                        .param("certificateType", "INCOME")
                        .param("applicantName", "A1")
                        .param("dateOfBirth", LocalDate.now().plusDays(3).toString())
                        .param("email", "not-an-email")
                        .param("phone", "12345")
                        .param("nationalId", TestDocuments.BAD_CHECKSUM_NATIONAL_ID)
                        .param("address", "short")
                        .param("district", "Pune")
                        .param("pinCode", "011001")
                        .param("purpose", "Scholarship application")
                        .with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(model().attributeHasFieldErrors("form",
                        "applicantName", "dateOfBirth", "email", "phone", "nationalId", "address", "pinCode",
                        "annualIncome", "declaration"))
                .andExpect(content().string(containsString("check digit")))
                .andExpect(content().string(containsString("value=\"Pune\"")));
    }

    @Test
    void marriageRequiresSpouseName() throws Exception {
        mvc.perform(validApplication("MARRIAGE", TestDocuments.pdf("invite.pdf")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(model().attributeHasFieldErrors("form", "spouseName"));
    }

    @Test
    void documentWithFakeContentIsRejected() throws Exception {
        mvc.perform(validIncomeApplication(TestDocuments.fakePdf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(model().attributeExists("documentError"))
                .andExpect(content().string(containsString("Only PDF, PNG or JPEG")));
    }

    @Test
    void submissionWithoutCsrfTokenIsRefused() throws Exception {
        mvc.perform(multipart("/apply").file(TestDocuments.pdf("x.pdf")).param("certificateType", "BIRTH"))
                .andExpect(status().isForbidden());
    }

    @Test
    void trackingRequiresMatchingEmail() throws Exception {
        MvcResult created = mvc.perform(validIncomeApplication(TestDocuments.pdf("slip.pdf"))).andReturn();
        String trackingId = created.getResponse().getRedirectedUrl().substring("/apply/submitted/".length());

        mvc.perform(post("/track").param("trackingId", trackingId).param("email", "someone.else@example.com").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("find a request with that tracking ID")));

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/track").param("trackingId", trackingId.toLowerCase())
                        .param("email", "AARAV.SHARMA@example.com").with(csrf()).session(session))
                .andExpect(redirectedUrl("/track/" + trackingId));
        mvc.perform(get("/track/" + trackingId).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Under review")))
                .andExpect(content().string(containsString("XXXX XXXX 0124")));
    }

    @Test
    void statusPageWithoutVerifiedSessionAsksForEmail() throws Exception {
        mvc.perform(get("/track/CCR-2026-AAAAAA"))
                .andExpect(redirectedUrl("/track"));
    }

    @Test
    void malformedTrackingIdIsAValidationError() throws Exception {
        mvc.perform(post("/track").param("trackingId", "hello").param("email", "a@b.in").with(csrf()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(model().attributeHasFieldErrors("trackForm", "trackingId"));
    }
}
