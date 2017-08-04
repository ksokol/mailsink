package com.github.ksokol.mailsink.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Kamill Sokol
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException {}
