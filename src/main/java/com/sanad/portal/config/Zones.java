package com.sanad.portal.config;

import java.time.ZoneId;

/** The portal serves Indian citizens: business dates (ages, display times) use IST, not the JVM default. */
public final class Zones {

    public static final ZoneId BUSINESS = ZoneId.of("Asia/Kolkata");

    private Zones() {
    }
}
