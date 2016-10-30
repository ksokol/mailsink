FROM java:openjdk-8-alpine

ADD target/mailsink.jar /opt/mailsink.jar

EXPOSE 25 2525

CMD [ "java", "-jar", "/opt/mailsink.jar" ]
