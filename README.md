# Mailsink [![Build Status](https://api.travis-ci.org/ksokol/mailsink.png?branch=master)](https://travis-ci.org/ksokol/mailsink) [![Quality Gate](https://sonarcloud.io/api/badges/gate?key=com.github.ksokol:mailsink)](https://sonarcloud.io/dashboard/index/com.github.ksokol:mailsink) [![Technical debt ratio](https://sonarqube.com/api/badges/measure?key=com.github.ksokol:mailsink&metric=sqale_debt_ratio)](https://sonarqube.com/dashboard/index/com.github.ksokol:mailsink) 

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
