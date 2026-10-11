package com.sanad.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point for both deployment styles:
 * <ul>
 *   <li>{@code java -jar ccrp.war} — embedded Tomcat (Docker, Ansible-managed node)</li>
 *   <li>drop the WAR into an external Tomcat 11 — {@link #configure} bootstraps the context</li>
 * </ul>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PortalApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PortalApplication.class);
    }
}
