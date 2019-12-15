# Mailsink [![Build Status](https://github.com/ksokol/mailsink/workflows/CI/badge.svg)](https://github.com/ksokol/mailsink) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.ksokol%3Amailsink&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.ksokol%3Amailsink)

Mailsink is a simple SMTP server based on [SubEtha](https://github.com/voodoodyne/subetha).

## Features

- capture and persist every incoming email in memory
- browse emails in a web based ui
- stop/start SMTP server
- extract html body fragments with XPath
- HTTP based api for e2e tests

### Ports

- 2500 SMTP
- 2525 HTTP (api and ui)

## Developer notes

### Build and package

- run `mvnw package`
- run `java -jar target/mailsink.jar`

## Docker

Available on Docker Hub [`ksokol/mailsink`](https://hub.docker.com/r/ksokol/mailsink/tags/).
Start Mailsink in a container with `docker run -p 2500:2500 -p 2525:2525 -t ksokol/mailsink`.
