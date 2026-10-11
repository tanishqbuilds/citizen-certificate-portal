<?xml version="1.0" encoding="UTF-8"?>
<tomcat-users xmlns="http://tomcat.apache.org/xml"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd"
              version="1.0">
  <!-- manager-script = text API only (no HTML GUI). Password injected at start-up. -->
  <role rolename="manager-script"/>
  <user username="deployer" password="__TOMCAT_DEPLOYER_PASSWORD__" roles="manager-script"/>
</tomcat-users>
