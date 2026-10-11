package com.sanad.portal.support;

import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** In-memory documents with real (or deliberately wrong) magic bytes. */
public final class TestDocuments {

    public static final String VALID_NATIONAL_ID = "2345 6789 0124";
    public static final String BAD_CHECKSUM_NATIONAL_ID = "2345 6789 0123";

    private TestDocuments() {
    }

    public static MockMultipartFile pdf(String name) {
        byte[] body = "%PDF-1.4\n1 0 obj << /Type /Catalog >> endobj\ntrailer << /Root 1 0 R >>\n%%EOF\n"
                .getBytes(StandardCharsets.US_ASCII);
        return new MockMultipartFile("document", name, "application/pdf", body);
    }

    public static MockMultipartFile png(String name) {
        byte[] body = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R'};
        return new MockMultipartFile("document", name, "image/png", body);
    }

    /** A text file pretending to be a PDF — the content type and extension lie. */
    public static MockMultipartFile fakePdf() {
        return new MockMultipartFile("document", "salary-slip.pdf", "application/pdf",
                "this is not really a pdf".getBytes(StandardCharsets.US_ASCII));
    }

    public static MockMultipartFile oversizedPdf(int megabytes) {
        byte[] body = new byte[megabytes * 1024 * 1024];
        Arrays.fill(body, (byte) ' ');
        System.arraycopy("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII), 0, body, 0, 9);
        return new MockMultipartFile("document", "big.pdf", "application/pdf", body);
    }
}
