package com.sanad.portal.service;

import com.sanad.portal.config.AppProperties;
import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.support.TestDocuments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentStorageServiceTest {

    @TempDir
    Path dataDir;

    DocumentStorageService service;

    @BeforeEach
    void setUp() {
        service = new DocumentStorageService(new AppProperties("test", dataDir.toString(),
                new AppProperties.Documents(DataSize.ofMegabytes(2)), null));
    }

    @Test
    void storesValidPdfWithHashAndRandomName() throws Exception {
        CertificateRequest.Document doc = service.store("CCR-2026-ABCDEF", TestDocuments.pdf("income.pdf"));

        assertThat(doc.contentType()).isEqualTo("application/pdf");
        assertThat(doc.sha256()).hasSize(64);
        assertThat(doc.name()).isEqualTo("income.pdf");
        assertThat(doc.path()).startsWith("CCR-2026-ABCDEF/").endsWith(".pdf").doesNotContain("income");
        assertThat(Files.size(dataDir.resolve("documents").resolve(doc.path()))).isEqualTo(doc.size());
    }

    @Test
    void acceptsPng() {
        assertThat(service.store("CCR-2026-ABCDEG", TestDocuments.png("photo.png")).contentType()).isEqualTo("image/png");
    }

    @Test
    void rejectsMissingDocument() {
        MockMultipartFile empty = new MockMultipartFile("document", "", "application/octet-stream", new byte[0]);
        assertThatThrownBy(() -> service.validate(empty)).isInstanceOf(DocumentRejectedException.class)
                .hasMessageContaining("Attach one supporting document");
    }

    @Test
    void rejectsFileWhoseContentIsNotWhatItClaims() {
        assertThatThrownBy(() -> service.validate(TestDocuments.fakePdf()))
                .isInstanceOf(DocumentRejectedException.class).hasMessageContaining("Only PDF, PNG or JPEG");
    }

    @Test
    void rejectsFilesOverTheLimit() {
        assertThatThrownBy(() -> service.validate(TestDocuments.oversizedPdf(3)))
                .isInstanceOf(DocumentRejectedException.class).hasMessageContaining("maximum allowed size is 2 MB");
    }

    @Test
    void rejectsExtensionThatDoesNotMatchContent() throws Exception {
        MockMultipartFile pngNamedPdf = new MockMultipartFile("document", "scan.pdf", "application/pdf",
                TestDocuments.png("x.png").getBytes());
        assertThatThrownBy(() -> service.validate(pngNamedPdf))
                .isInstanceOf(DocumentRejectedException.class).hasMessageContaining("extension does not match");
    }

    @Test
    void sanitizesPathsInOriginalNames() {
        assertThat(DocumentStorageService.sanitize("../../etc/passwd")).isEqualTo("passwd");
        assertThat(DocumentStorageService.sanitize("C:\\Users\\me\\proof <1>.pdf")).isEqualTo("proof _1_.pdf");
    }

    @Test
    void refusesToLoadOutsideTheStore() {
        assertThatThrownBy(() -> service.load("../../secret")).isInstanceOf(IllegalArgumentException.class);
    }
}
