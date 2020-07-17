FROM openjdk:11-jre-slim

ADD target/mailsink.jar /opt/mailsink.jar

EXPOSE 2500 2525

CMD [ "java" ,"-jar", "/opt/mailsink.jar" ]
