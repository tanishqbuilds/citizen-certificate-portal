package com.sanad.portal.web;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.service.CertificateRequestService;
import com.sanad.portal.web.form.TrackForm;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/** CCR-7: status tracking for citizens. */
@Controller
public class TrackingController {

    static final String NOT_FOUND = "We couldn't find a request with that tracking ID and email. "
            + "Check both and try again.";

    private final CertificateRequestService service;

    public TrackingController(CertificateRequestService service) {
        this.service = service;
    }

    @GetMapping("/track")
    public String form(Model model) {
        if (!model.containsAttribute("trackForm")) {
            model.addAttribute("trackForm", new TrackForm());
        }
        return "track";
    }

    @PostMapping("/track")
    public String lookup(@Valid @ModelAttribute("trackForm") TrackForm form, BindingResult result,
                         Model model, HttpServletResponse response, HttpSession session) {
        if (result.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_CONTENT.value());
            return "track";
        }
        Optional<CertificateRequest> found = service.findForCitizen(form.normalizedTrackingId(), form.getEmail());
        if (found.isEmpty()) {
            model.addAttribute("lookupError", NOT_FOUND);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return "track";
        }
        CitizenAccess.grant(session, found.get().getTrackingId());
        return "redirect:/track/" + found.get().getTrackingId();
    }

    @GetMapping("/track/{trackingId}")
    public String status(@PathVariable String trackingId, Model model, HttpSession session,
                         RedirectAttributes redirect) {
        String id = trackingId.toUpperCase();
        Optional<CertificateRequest> request = CitizenAccess.has(session, id) ? service.findByTrackingId(id) : Optional.empty();
        if (request.isEmpty()) {
            TrackForm prefill = new TrackForm();
            prefill.setTrackingId(id);
            redirect.addFlashAttribute("trackForm", prefill);
            redirect.addFlashAttribute("lookupInfo", "Confirm the email address on the application to view this request.");
            return "redirect:/track";
        }
        model.addAttribute("trackForm", new TrackForm());
        model.addAttribute("request", request.get());
        return "track";
    }
}
