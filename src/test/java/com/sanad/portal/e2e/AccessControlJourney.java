package com.sanad.portal.e2e;

import com.sanad.portal.e2e.pages.OfficerLoginPage;
import com.sanad.portal.e2e.pages.OfficerQueuePage;
import com.sanad.portal.e2e.support.BaseJourney;
import com.sanad.portal.e2e.support.E2EConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** TC-06 — The officer workspace is closed to anonymous users and wrong credentials. */
@DisplayName("TC-06 Officer access control")
class AccessControlJourney extends BaseJourney {

    @Test
    @DisplayName("anonymous → login page; wrong password refused; sign in and out")
    void officerAreaIsProtected() {
        driver().get(E2EConfig.baseUrl() + "/officer");
        OfficerLoginPage login = new OfficerLoginPage(driver()).waitUntilLoaded();
        assertThat(login.currentPath()).endsWith("/officer/login");

        login.signInExpectingFailure(E2EConfig.officerUser(), "definitely-wrong");
        assertThat(login.error()).contains("Incorrect username or password");

        OfficerQueuePage queue = login.signIn();
        assertThat(queue.currentPath()).endsWith("/officer");

        queue.signOut();
        assertThat(driver().getCurrentUrl()).contains("signedOut");
    }
}
