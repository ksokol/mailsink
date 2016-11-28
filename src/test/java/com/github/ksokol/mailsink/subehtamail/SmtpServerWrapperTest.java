package com.github.ksokol.mailsink.subehtamail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.subethamail.smtp.server.SMTPServer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class SmtpServerWrapperTest {

    private MockedSmtpServerWrapper smtpServerWrapper;

    @Before
    public void setUp() throws Exception {
        MailProperties mailProperties = new MailProperties();
        mailProperties.setPort(25252);
        smtpServerWrapper = new MockedSmtpServerWrapper(mailProperties);
    }

    @Test
    public void shouldSetPortAndStartServer() throws Exception {
        smtpServerWrapper.start();

        verify(smtpServerWrapper.smtpServer).setPort(25252);
        verify(smtpServerWrapper.smtpServer).start();
    }

    @Test
    public void shouldReturnStatusOfServer() throws Exception {
        smtpServerWrapper.start();
        smtpServerWrapper.isRunning();

        verify(smtpServerWrapper.smtpServer).isRunning();
    }

    @Test
    public void shouldStopServer() throws Exception {
        smtpServerWrapper.start();
        smtpServerWrapper.stop();

        verify(smtpServerWrapper.smtpServer).stop();
    }

    private static class MockedSmtpServerWrapper extends SmtpServerWrapper {

        private SMTPServer smtpServer = mock(SMTPServer.class);

        private MockedSmtpServerWrapper(MailProperties mailProperties) {
            super(null, mailProperties);
        }

        @Override
        protected SMTPServer create() {
            return smtpServer;
        }
    }
}
