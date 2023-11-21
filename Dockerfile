FROM eclipse-temurin:17-jre-alpine
LABEL Author="Europeana Foundation <development@europeana.eu>"

ENV ELASTIC_APM_VERSION 1.34.1
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/$ELASTIC_APM_VERSION/elastic-apm-agent-$ELASTIC_APM_VERSION.jar /usr/local/elastic-apm-agent.jar

COPY entity-management-web/target/entity-management-web-executable.jar /opt/app/entity-management-api.jar
ENTRYPOINT ["java", "-jar","/opt/app/entity-management-api.jar"]