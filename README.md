# Mailsink [![Build Status](https://api.travis-ci.org/ksokol/mailsink.png?branch=master)](https://travis-ci.org/ksokol/mailsink) [![Coverage Status](https://coveralls.io/repos/ksokol/mailsink/badge.png?branch=master)](https://coveralls.io/r/ksokol/mailsink?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/ae860d680b2f459f973e1386064b260d)](https://www.codacy.com/app/dev_9/mailsink)

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
