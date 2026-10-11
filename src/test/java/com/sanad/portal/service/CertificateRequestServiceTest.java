package com.sanad.portal.service;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.domain.CertificateRequestRepository;
import com.sanad.portal.domain.CertificateType;
import com.sanad.portal.support.TestDocuments;
import com.sanad.portal.web.form.ApplicationForm;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CertificateRequestServiceTest {

    @Test
    void deletesStoredDocumentWhenPersistenceFails() {
        CertificateRequestRepository repository = mock(CertificateRequestRepository.class);
        DocumentStorageService documents = mock(DocumentStorageService.class);
        TrackingIdGenerator ids = mock(TrackingIdGenerator.class);
        when(ids.next()).thenReturn("CCR-2026-ABCDEF");
        when(documents.store(any(), any())).thenReturn(
                new CertificateRequest.Document("slip.pdf", "application/pdf", 10, "0".repeat(64), "CCR-2026-ABCDEF/x.pdf"));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        CertificateRequestService service = new CertificateRequestService(repository, documents, ids,
                Clock.fixed(Instant.parse("2026-10-11T10:00:00Z"), ZoneOffset.UTC));

        assertThatThrownBy(() -> service.submit(form(), TestDocuments.pdf("slip.pdf")))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(documents).delete("CCR-2026-ABCDEF/x.pdf");
    }

    private static ApplicationForm form() {
        ApplicationForm f = new ApplicationForm();
        f.setCertificateType(CertificateType.BIRTH);
        f.setApplicantName("Aarav Sharma");
        f.setDateOfBirth(LocalDate.of(1998, 4, 12));
        f.setEmail("aarav@example.com");
        f.setPhone("9876543210");
        f.setNationalId(TestDocuments.VALID_NATIONAL_ID);
        f.setAddress("Flat 4B, Lotus Residency");
        f.setDistrict("Pune");
        f.setPinCode("411001");
        f.setPurpose("School admission records");
        f.setDeclaration(true);
        return f;
    }
}
