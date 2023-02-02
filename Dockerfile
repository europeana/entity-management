#FROM library/tomcat:9.0-jdk11
#
## CF app listens on this port. Only one port supported per image
## see: https://docs.cloudfoundry.org/devguide/deploy-apps/push-docker.html#-port-configuration
#EXPOSE 8080
#
## copy application WAR (with libraries inside)
#COPY entity-management-web/target/*.war /app.war

# Builds a docker image from a locally built Maven war. Requires 'mvn package' to have been run beforehand
FROM tomcat:9.0-jdk11-slim
LABEL Author="Europeana Foundation <development@europeana.eu>"
WORKDIR /usr/local/tomcat/webapps

ENV ELASTIC_APM_VERSION 1.34.1
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/$ELASTIC_APM_VERSION/elastic-apm-agent-$ELASTIC_APM_VERSION.jar /usr/local/elastic-apm-agent.jar

# Copy unzipped directory so we can mount config files in Kubernetes pod
COPY entity-management-web/target/entity-management-web ./ROOT/