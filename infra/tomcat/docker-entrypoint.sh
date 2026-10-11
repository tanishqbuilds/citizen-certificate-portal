#!/bin/sh
set -eu
: "${TOMCAT_DEPLOYER_PASSWORD:?TOMCAT_DEPLOYER_PASSWORD must be set}"
sed "s|__TOMCAT_DEPLOYER_PASSWORD__|${TOMCAT_DEPLOYER_PASSWORD}|" \
    "$CATALINA_HOME/conf/tomcat-users.xml.tpl" > "$CATALINA_HOME/conf/tomcat-users.xml"
exec "$@"
