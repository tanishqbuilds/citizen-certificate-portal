#!/bin/sh
# Two Spring Boot contexts (staging + production) fit comfortably in 1 GB.
export CATALINA_OPTS="$CATALINA_OPTS -Xms256m -Xmx1g -Djava.security.egd=file:/dev/urandom -Duser.timezone=UTC"
