package com.sanad.portal.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class WebExceptionHandler {

    /** Uploads above the transport limit never reach the controller; explain instead of showing a 413. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String uploadTooLarge(RedirectAttributes redirect) {
        redirect.addFlashAttribute("uploadError", "That document is far larger than the 2 MB limit. "
                + "Please attach a smaller scan or photo.");
        return "redirect:/apply";
    }
}
