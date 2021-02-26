FROM library/tomcat:9.0-jdk11

# CF app listens on this port. Only one port supported per image
# see: https://docs.cloudfoundry.org/devguide/deploy-apps/push-docker.html#-port-configuration
EXPOSE 8080

# copy application WAR (with libraries inside)
COPY entity-management-web/target/*.war /app.war