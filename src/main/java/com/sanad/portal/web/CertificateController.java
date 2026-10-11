package com.sanad.portal.web;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.domain.RequestStatus;
import com.sanad.portal.service.CertificateRequestService;
import com.sanad.portal.service.RequestNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Printable certificate (CCR-11). Visible to the citizen who proved ownership in this session,
 * or to a signed-in officer. Requests that are not approved behave as if they do not exist.
 */
@Controller
public class CertificateController {

    private final CertificateRequestService service;

    public CertificateController(CertificateRequestService service) {
        this.service = service;
    }

    @GetMapping("/certificate/{trackingId}")
    public String certificate(@PathVariable String trackingId, HttpSession session, Authentication auth, Model model) {
        String id = trackingId.toUpperCase();
        boolean officer = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER"));
        if (!officer && !CitizenAccess.has(session, id)) {
            throw new RequestNotFoundException(id);
        }
        CertificateRequest r = service.get(id);
        if (r.getStatus() != RequestStatus.APPROVED) {
            throw new RequestNotFoundException(id);
        }
        model.addAttribute("request", r);
        return "certificate";
    }
}
