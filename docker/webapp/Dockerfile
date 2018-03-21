#WEB
FROM tomcat:8
RUN apt-get update && apt-get install -y \
software-properties-common \
openjfx


RUN rm -rf /usr/local/tomcat/webapps/*

EXPOSE 8080

ADD application.properties jdbc.properties messaging.properties /root/
ADD application.properties jdbc.properties messaging.properties /usr/local/tomcat/
COPY ROOT.war /usr/local/tomcat/webapps/



