package com.sanad.portal.service;

import com.sanad.portal.config.AppProperties;
import com.sanad.portal.domain.CertificateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Validates and stores supporting documents (CCR-5).
 * <p>
 * The declared content type and the file extension are <em>not</em> trusted: the type is detected from
 * the file's magic bytes. Files are stored under random names outside any web-served path.
 */
@Service
public class DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(DocumentStorageService.class);

    /** Supported document kinds, identified by their leading bytes. */
    enum Kind {
        PDF("application/pdf", "pdf", new byte[]{'%', 'P', 'D', 'F', '-'}),
        PNG("image/png", "png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}),
        JPEG("image/jpeg", "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

        final String contentType;
        final String extension;
        final byte[] magic;

        Kind(String contentType, String extension, byte[] magic) {
            this.contentType = contentType;
            this.extension = extension;
            this.magic = magic;
        }

        static Kind detect(byte[] head) {
            for (Kind k : values()) {
                if (head.length >= k.magic.length && Arrays.equals(Arrays.copyOf(head, k.magic.length), k.magic)) {
                    return k;
                }
            }
            return null;
        }

        boolean matchesExtension(String filename) {
            String lower = filename.toLowerCase();
            return switch (this) {
                case PDF -> lower.endsWith(".pdf");
                case PNG -> lower.endsWith(".png");
                case JPEG -> lower.endsWith(".jpg") || lower.endsWith(".jpeg");
            };
        }
    }

    private final Path root;
    private final long maxBytes;

    public DocumentStorageService(AppProperties props) {
        this.root = Path.of(props.dataDir()).toAbsolutePath().normalize().resolve("documents");
        this.maxBytes = props.documents().maxSize().toBytes();
    }

    public long maxBytes() {
        return maxBytes;
    }

    /**
     * Checks presence, size, extension and real content type.
     *
     * @throws DocumentRejectedException with a citizen-friendly message
     */
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new DocumentRejectedException("Attach one supporting document (PDF, PNG or JPEG).");
        }
        if (file.getSize() > maxBytes) {
            throw new DocumentRejectedException("The document is " + humanSize(file.getSize())
                    + ". The maximum allowed size is " + humanSize(maxBytes) + ".");
        }
        Kind kind = detect(file);
        if (kind == null) {
            throw new DocumentRejectedException("Only PDF, PNG or JPEG documents are accepted. "
                    + "The file you attached is not a valid document of these types.");
        }
        if (!kind.matchesExtension(file.getOriginalFilename())) {
            throw new DocumentRejectedException("The file extension does not match its contents (detected "
                    + kind.name() + "). Please attach the original file.");
        }
    }

    /** Validates, then writes the file to {@code <data-dir>/documents/<trackingId>/<uuid>.<ext>}. */
    public CertificateRequest.Document store(String trackingId, MultipartFile file) {
        validate(file);
        Kind kind = detect(file);
        String storedName = UUID.randomUUID() + "." + kind.extension;
        Path dir = root.resolve(trackingId);
        Path target = dir.resolve(storedName);
        try {
            Files.createDirectories(dir);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new DigestInputStream(file.getInputStream(), sha)) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String hash = HexFormat.of().formatHex(sha.digest());
            log.info("Stored document for {} ({} bytes, {}, sha256={})", trackingId, file.getSize(), kind, hash);
            return new CertificateRequest.Document(
                    sanitize(file.getOriginalFilename()), kind.contentType, file.getSize(), hash,
                    root.relativize(target).toString());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store document for " + trackingId, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Best-effort removal, used to avoid orphaned personal data when a submission fails after upload. */
    public void delete(String relativePath) {
        try {
            Path p = resolveInside(relativePath);
            Files.deleteIfExists(p);
            Files.deleteIfExists(p.getParent()); // only succeeds when the request folder is now empty
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Could not delete document {}: {}", relativePath, e.getMessage());
        }
    }

    public Resource load(String relativePath) {
        return new FileSystemResource(resolveInside(relativePath));
    }

    private Path resolveInside(String relativePath) {
        Path p = root.resolve(relativePath).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("Path escapes the document store");
        }
        return p;
    }

    private static Kind detect(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return Kind.detect(in.readNBytes(8));
        } catch (IOException e) {
            return null;
        }
    }

    /** Keeps the original name for display only: strips any path and unusual characters. */
    static String sanitize(String original) {
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("[^A-Za-z0-9._ ()-]", "_");
        return name.length() > 120 ? name.substring(name.length() - 120) : name;
    }

    public static String humanSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return Math.round(bytes / 1024.0) + " KB";
        double mb = bytes / (1024.0 * 1024.0);
        return mb == Math.rint(mb) ? String.format("%d MB", (long) mb) : String.format("%.1f MB", mb);
    }
}
