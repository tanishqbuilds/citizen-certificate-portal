package com.sanad.portal.web;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.domain.CertificateType;
import com.sanad.portal.service.CertificateRequestService;
import com.sanad.portal.service.DocumentRejectedException;
import com.sanad.portal.service.DocumentStorageService;
import com.sanad.portal.web.form.ApplicationForm;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;

/** CCR-3 … CCR-6: application form, validation and acknowledgement. */
@Controller
@RequestMapping("/apply")
public class ApplyController {

    /** What the acknowledgement page shows. Carried as a flash attribute, never looked up by URL alone. */
    public record Acknowledgement(String trackingId, CertificateType type, String email, Instant submittedAt) {
    }

    private final CertificateRequestService service;
    private final DocumentStorageService documents;

    public ApplyController(CertificateRequestService service, DocumentStorageService documents) {
        this.service = service;
        this.documents = documents;
    }

    @InitBinder("form")
    void trimStrings(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    @ModelAttribute
    void common(Model model) {
        model.addAttribute("types", CertificateType.values());
        model.addAttribute("maxBytes", documents.maxBytes());
        model.addAttribute("maxSize", DocumentStorageService.humanSize(documents.maxBytes()));
    }

    @GetMapping
    public String form(@RequestParam(name = "type", required = false) CertificateType type, Model model) {
        if (!model.containsAttribute("form")) {
            ApplicationForm form = new ApplicationForm();
            form.setCertificateType(type);
            model.addAttribute("form", form);
        }
        return "apply";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("form") ApplicationForm form,
                         BindingResult result,
                         @RequestParam(name = "document", required = false) MultipartFile document,
                         Model model,
                         HttpServletResponse response,
                         HttpSession session,
                         RedirectAttributes redirect) {
        String documentError = null;
        try {
            documents.validate(document);
        } catch (DocumentRejectedException e) {
            documentError = e.getMessage();
        }

        if (result.hasErrors() || documentError != null) {
            model.addAttribute("documentError", documentError);
            model.addAttribute("errorCount", result.getErrorCount() + (documentError != null ? 1 : 0));
            response.setStatus(HttpStatus.UNPROCESSABLE_CONTENT.value());
            return "apply";
        }

        form.normalize();
        CertificateRequest saved = service.submit(form, document);
        CitizenAccess.grant(session, saved.getTrackingId());
        redirect.addFlashAttribute("ack", new Acknowledgement(
                saved.getTrackingId(), saved.getCertificateType(), saved.getEmail(), saved.getSubmittedAt()));
        return "redirect:/apply/submitted/" + saved.getTrackingId();
    }

    @GetMapping("/submitted/{trackingId}")
    public String submitted(@PathVariable String trackingId, Model model) {
        if (!model.containsAttribute("ack")) {
            // Refresh or shared link: the acknowledgement is one-time; send them to tracking instead.
            return "redirect:/track/" + trackingId;
        }
        return "submitted";
    }
}
