FROM java:openjdk-8-alpine

ADD target/mailsink.jar /opt/mailsink.jar

EXPOSE 25 2525

CMD [ "java", "-Dspring.mail.port=25","-jar", "/opt/mailsink.jar" ]
