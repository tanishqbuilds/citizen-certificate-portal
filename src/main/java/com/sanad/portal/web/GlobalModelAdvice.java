package com.sanad.portal.web;

import com.sanad.portal.config.AppProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds data every page needs: the signed-in officer (if any) and build metadata for the footer. */
@ControllerAdvice
public class GlobalModelAdvice {

    public record BuildView(String version, String commit, String environment) {}

    private final BuildView build;

    public GlobalModelAdvice(AppProperties props,
                             ObjectProvider<BuildProperties> buildProps,
                             ObjectProvider<GitProperties> gitProps) {
        BuildProperties bp = buildProps.getIfAvailable();
        GitProperties gp = gitProps.getIfAvailable();
        this.build = new BuildView(
                bp != null ? bp.getVersion() : "dev",
                gp != null ? gp.getShortCommitId() : null,
                props.environment());
    }

    @ModelAttribute("build")
    public BuildView build() {
        return build;
    }

    @ModelAttribute("currentOfficer")
    public String currentOfficer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }
}
