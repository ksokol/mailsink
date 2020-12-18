package com.github.ksokol.mailsink.sse;

import com.github.ksokol.mailsink.entity.Mail;
import org.springframework.context.ApplicationEvent;

public class IncomingEvent extends ApplicationEvent {

    public IncomingEvent(Object source) {
        super(source);
    }

    protected Mail getIncomingMail() {
        return (Mail) super.getSource();
    }
}
