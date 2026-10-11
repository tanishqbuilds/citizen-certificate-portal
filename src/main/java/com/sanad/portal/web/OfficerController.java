package com.sanad.portal.web;

import com.sanad.portal.domain.CertificateRequest;
import com.sanad.portal.domain.IllegalStatusTransitionException;
import com.sanad.portal.domain.RequestStatus;
import com.sanad.portal.service.CertificateRequestService;
import com.sanad.portal.web.form.DecisionForm;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Supplier;

/** Officer workspace: sign-in page, queue with KPIs, request detail and decisions (CCR-8 … CCR-12). */
@Controller
@RequestMapping("/officer")
public class OfficerController {

    private final CertificateRequestService service;

    public OfficerController(CertificateRequestService service) {
        this.service = service;
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())
                ? "redirect:/officer" : "officer/login";
    }

    @GetMapping
    public String queue(@RequestParam(name = "status", required = false) RequestStatus status,
                        @RequestParam(name = "q", required = false) String q,
                        Model model) {
        model.addAttribute("dashboard", service.dashboard());
        model.addAttribute("requests", service.queue(status, q));
        model.addAttribute("filter", status);
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("statuses", RequestStatus.values());
        return "officer/queue";
    }

    @GetMapping("/requests/{trackingId}")
    public String detail(@PathVariable String trackingId, Model model) {
        CertificateRequest r = service.get(trackingId);
        model.addAttribute("request", r);
        if (!model.containsAttribute("decision")) {
            model.addAttribute("decision", new DecisionForm());
        }
        return "officer/request";
    }

    @GetMapping("/requests/{trackingId}/document")
    public ResponseEntity<Resource> document(@PathVariable String trackingId) {
        CertificateRequest r = service.get(trackingId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(r.getDocumentContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(r.getDocumentName()).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(service.document(r));
    }

    @PostMapping("/requests/{trackingId}/start-review")
    public String startReview(@PathVariable String trackingId, Authentication auth, RedirectAttributes redirect) {
        return act(trackingId, redirect, "Review started. The citizen can now see the request is with an officer.",
                () -> service.startReview(trackingId, auth.getName()));
    }

    @PostMapping("/requests/{trackingId}/approve")
    public String approve(@PathVariable String trackingId, @Valid @ModelAttribute("decision") DecisionForm form,
                          BindingResult result, Authentication auth, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return backWithErrors(trackingId, form, result, redirect);
        }
        return act(trackingId, redirect, null, () -> service.approve(trackingId, auth.getName(), form.getRemarks()));
    }

    @PostMapping("/requests/{trackingId}/reject")
    public String reject(@PathVariable String trackingId, @Valid @ModelAttribute("decision") DecisionForm form,
                         BindingResult result, Authentication auth, RedirectAttributes redirect) {
        if (!form.hasValidRejectionReason()) {
            result.rejectValue("remarks", "reason.required",
                    "Give the citizen a reason of at least " + DecisionForm.MIN_REASON + " characters.");
        }
        if (result.hasErrors()) {
            return backWithErrors(trackingId, form, result, redirect);
        }
        return act(trackingId, redirect, "Request rejected. The reason is visible to the citizen.",
                () -> service.reject(trackingId, auth.getName(), form.getRemarks()));
    }

    private String act(String trackingId, RedirectAttributes redirect, String message,
                       Supplier<CertificateRequest> action) {
        try {
            CertificateRequest r = action.get();
            redirect.addFlashAttribute("success", message != null ? message
                    : "Approved. Certificate " + r.getCertificateNumber() + " has been issued.");
        } catch (IllegalStatusTransitionException e) {
            redirect.addFlashAttribute("failure", e.getMessage());
        } catch (ObjectOptimisticLockingFailureException e) {
            redirect.addFlashAttribute("failure", "Another officer changed this request at the same time. "
                    + "The latest version is shown below.");
        }
        return "redirect:/officer/requests/" + trackingId;
    }

    private static String backWithErrors(String trackingId, DecisionForm form, BindingResult result,
                                         RedirectAttributes redirect) {
        redirect.addFlashAttribute("org.springframework.validation.BindingResult.decision", result);
        redirect.addFlashAttribute("decision", form);
        return "redirect:/officer/requests/" + trackingId;
    }
}
