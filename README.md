# Mailsink [![Build Status](https://api.travis-ci.org/ksokol/mailsink.png?branch=master)](https://travis-ci.org/ksokol/mailsink) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=com.github.ksokol:mailsink)](https://sonarqube.com/dashboard/index/com.github.ksokol:mailsink) [![Technical debt ratio](https://sonarqube.com/api/badges/measure?key=com.github.ksokol:mailsink&metric=sqale_debt_ratio)](https://sonarqube.com/dashboard/index/com.github.ksokol:mailsink) 

Mailsink is a simple SMTP server based on [SubEtha](https://github.com/voodoodyne/subetha) with a web UI.

## Installation

### Prerequisite

- Java 8

### Build and package

- run `mvnw package`
- You will find a fat jar (Spring Boot application) under `target`
- run `java -jar target/mailsink.jar`

Mailsink listens on port `2500` for SMTP connections. The web UI is available on port `2525`.

### Docker

Build your own Docker image with the provided `Dockerfile` or pull the image from the Docker Hub registry [`ksokol/mailsink`](https://hub.docker.com/r/ksokol/mailsink/tags/) .
Inside Docker Mailsink listens on port `25` for SMTP connections. The web UI is available on port `2525`.
Run on local machine with `docker run -p 25:25 -p 2525:2525 -t ksokol/mailsink`
