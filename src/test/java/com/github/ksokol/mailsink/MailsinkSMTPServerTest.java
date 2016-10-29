package com.github.ksokol.mailsink;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.subethamail.smtp.server.SMTPServer;

import static org.mockito.Mockito.verify;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class MailsinkSMTPServerTest {

    @InjectMocks
    private MailsinkSMTPServer mailsinkSMTPServer;

    @Mock
    private SMTPServer smtpServer;

    @Test
    public void shouldStartSmtpServerOnAfterAllPropertiesSupplied() throws Exception {
        mailsinkSMTPServer.afterPropertiesSet();
        verify(smtpServer).start();
    }

    @Test
    public void shutdownSmtpServerOnBeanDestruction() throws Exception {
        mailsinkSMTPServer.destroy();
        verify(smtpServer).stop();
    }

}
