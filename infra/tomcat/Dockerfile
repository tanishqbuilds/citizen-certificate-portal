# Tomcat 11 deployment target (Jakarta Servlet 6.1, required by Spring Boot 4).
FROM tomcat:11.0.26-jre21-temurin

# Only the Manager app is enabled (no docs/examples/host-manager) — used by Jenkins over the text API.
RUN cp -r "$CATALINA_HOME/webapps.dist/manager" "$CATALINA_HOME/webapps/" \
 && mkdir -p "$CATALINA_HOME/deployments" "$CATALINA_HOME/data" "$CATALINA_HOME/conf/Catalina/localhost"

COPY manager-context.xml   $CATALINA_HOME/webapps/manager/META-INF/context.xml
COPY tomcat-users.xml.tpl  $CATALINA_HOME/conf/tomcat-users.xml.tpl
COPY setenv.sh             $CATALINA_HOME/bin/setenv.sh
COPY docker-entrypoint.sh  /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh "$CATALINA_HOME/bin/setenv.sh"

# There is no ROOT application, so "/" answers 404. Any HTTP status means Catalina is serving;
# only a connection failure (curl exit ≠ 0 without -f) marks the container unhealthy.
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD curl -sS -o /dev/null http://localhost:8080/ || exit 1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["catalina.sh", "run"]
