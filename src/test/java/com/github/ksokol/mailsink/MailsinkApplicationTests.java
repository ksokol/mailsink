package com.github.ksokol.mailsink;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.fail;

public class MailsinkApplicationTests {

    private static final int SMTP_PORT = 12500;
    private static final int SERVER_PORT = 12525;
    private static final String TOPIC = "/topic/incoming-mail";
    private static final String WEB_SOCKET_PATH = "/incoming-mail/websocket";

    @BeforeClass
    public static void setUp() throws Exception {
        MailsinkApplication.main(new String[] { "--spring.mail.port=" + SMTP_PORT, "--server.port=" + SERVER_PORT });
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if(MailsinkApplication.applicationContext != null) {
            SpringApplication.exit(MailsinkApplication.applicationContext);
        }
    }

    @Test
    public void shouldListenOnSmtpPort() throws IOException {
        try {
            new Socket("localhost", SMTP_PORT);
        } catch(ConnectException exception) {
            fail("smtp server does not listen on port " + SMTP_PORT);
        } catch (Exception exception) {
            fail("failed with unexpected exception: " + exception.getMessage());
        }
    }

    @Test
    public void shouldSendMessageThrowWebSocketWhenMailReceived() throws Exception {
        WebSocketConnection connection = connectToWebSocket();
        sendMail();

        assertThat(connection.getMessage(), hasEntry("sender", "from"));
    }

    private WebSocketConnection connectToWebSocket() throws InterruptedException {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        MyStompSessionHandler session = new MyStompSessionHandler();
        stompClient.connect("ws://localhost:" + SERVER_PORT + WEB_SOCKET_PATH, session);
        Thread.sleep(1000); // wait one second before proceeding
        return session;
    }

    private void sendMail() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(SMTP_PORT);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("from");
        message.setTo("to");
        message.setText("text");

        mailSender.send(message);
    }

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter implements WebSocketConnection {

        private final CountDownLatch latch = new CountDownLatch(1);

        private Map<String, String> res;

        @Override
        public Map<String, String> getMessage() throws InterruptedException {
            if (latch.await(5, TimeUnit.SECONDS)) {
                return res;
            }
            throw new AssertionError("no message received");
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            session.subscribe(TOPIC, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    latch.countDown();
                    res = (Map<String, String>) payload;
                }
            });
        }
    }

    private interface WebSocketConnection {
        Map<String, String> getMessage() throws InterruptedException;
    }
}
