package com.github.ksokol.mailsink.handler;

import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamill Sokol
 */
@Component
public class MessageListener implements SimpleMessageListener {

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream body) throws IOException {

    }
}
