package com.github.ksokol.mailsink.bootstrap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ConditionalOnProperty(prefix = "mailsink", name = "example-mails", matchIfMissing = true, havingValue = "true")
@interface ConditionalOnExampleMailsEnabled {
}
