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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

public class MailsinkApplicationTests {

    private static final int SMTP_PORT = 12500;
    private static final int SERVER_PORT = 12525;
    private static final String TOPIC_INCOMING_MAIL = "/topic/incoming-mail";
    private static final String TOPIC_SMTP_LOG = "/topic/smtp-log";
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
    public void shouldSendIncomingMailThroughWebSocketWhenMailReceived() throws Exception {
        WebSocketConnection connection = connectToIncomingLogTopic();
        sendMail();

        assertThat(connection.getMessages(), hasItem(hasEntry("sender", "from")));
    }

    @Test
    public void shouldSendSmtpLogThroughWebSocketWhenMailReceived() throws Exception {
        WebSocketConnection connection = connectToSmtpLogTopic();
        sendMail();

        assertThat(connection.getMessages(), hasItem(allOf(
                hasEntry(is("line"), is("Server: 250 Ok")),
                hasEntry(is("time"), notNullValue())
        )));
    }

    private WebSocketConnection connectToIncomingLogTopic() throws InterruptedException {
        return connectToWebSocket(TOPIC_INCOMING_MAIL, 1);
    }

    private WebSocketConnection connectToSmtpLogTopic() throws InterruptedException {
        return connectToWebSocket(TOPIC_SMTP_LOG, 13);
    }

    private WebSocketConnection connectToWebSocket(String topic, int messageCount) throws InterruptedException {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        MyStompSessionHandler session = new MyStompSessionHandler(topic, messageCount);
        stompClient.connect("ws://localhost:" + SERVER_PORT + WEB_SOCKET_PATH, session);
        Thread.sleep(2000); // wait one second before proceeding
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

        private final CountDownLatch latch;
        private final String topic;

        private List<Map<String, String>> messages = new ArrayList<>();

        public MyStompSessionHandler(String topic, int messageCount) {
            this.topic = topic;
            this.latch = new CountDownLatch(messageCount);
        }

        @Override
        public List<Map<String, String>> getMessages() throws InterruptedException {
            if (latch.await(5, TimeUnit.SECONDS)) {
                return messages;
            }
            throw new AssertionError("no message received");
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            session.subscribe(topic, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    messages.add((Map<String, String>) payload);
                    latch.countDown();
                }
            });
        }
    }

    private interface WebSocketConnection {
        List<Map<String, String>> getMessages() throws InterruptedException;
    }
}
