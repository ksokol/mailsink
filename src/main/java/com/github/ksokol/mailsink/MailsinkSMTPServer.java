package com.github.ksokol.mailsink;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.server.SMTPServer;

/**
 * @author Kamill Sokol
 */
@Component
public class MailsinkSMTPServer implements InitializingBean, DisposableBean {

    private final SMTPServer smtpServer;

    public MailsinkSMTPServer(SMTPServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        smtpServer.start();
    }

    @Override
    public void destroy() throws Exception {
        smtpServer.stop();
    }
}
