FROM library/tomcat:9.0-jdk11
# copy application WAR (with libraries inside)
COPY entity-management-web/target/*.war /app.war