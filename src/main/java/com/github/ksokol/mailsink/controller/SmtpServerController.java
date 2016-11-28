package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.subehtamail.SmtpServerWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * @author Kamill Sokol
 */
@RestController
@RequestMapping("smtpServer")
public class SmtpServerController {

    private final SmtpServerWrapper smtpServerWrapper;

    public SmtpServerController(SmtpServerWrapper smtpServerWrapper) {
        this.smtpServerWrapper = smtpServerWrapper;
    }

    @GetMapping("status")
    public Map<String, Object> status() {
        return statusResponse();
    }

    @PostMapping("status/toggle")
    public Map<String, Object> toggle() {
        if(smtpServerWrapper.isRunning()) {
            smtpServerWrapper.stop();
        } else {
            smtpServerWrapper.start();
        }
        return statusResponse();
    }

    private Map<String, Object> statusResponse() {
        return Collections.singletonMap("isRunning", smtpServerWrapper.isRunning());
    }
}
