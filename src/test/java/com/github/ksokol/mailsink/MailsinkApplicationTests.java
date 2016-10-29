package com.github.ksokol.mailsink;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import static org.junit.Assert.fail;

public class MailsinkApplicationTests {

    private static final int SMTP_PORT = 25;

    @Test
    public void shouldListenOnSmtpPort() throws IOException {
        MailsinkApplication.main(new String[]{});

        try {
            new Socket("localhost", SMTP_PORT);
        } catch(ConnectException exception) {
            fail("smtp server does not listen on port " + SMTP_PORT);
        } catch (Exception exception) {
            fail("failed with unexpected exception: " + exception.getMessage());
        } finally {
            if(MailsinkApplication.applicationContext != null) {
                SpringApplication.exit(MailsinkApplication.applicationContext);
            }
        }
    }

}
