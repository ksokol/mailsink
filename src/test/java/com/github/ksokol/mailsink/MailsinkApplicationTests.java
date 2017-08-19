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
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.awaitility.Duration.TWO_SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

public class MailsinkApplicationTests {

    private static final int SMTP_PORT = 12500;
    private static final int SERVER_PORT = 12525;
    private static final String TOPIC_INCOMING_MAIL = "/topic/incoming-mail";
    private static final String TOPIC_SMTP_LOG = "/topic/smtp-log";
    private static final String WEB_SOCKET_PATH = "/ws/websocket";

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
        Handler connection = connectToWebSocket(TOPIC_INCOMING_MAIL);
        sendMail();

        await().atMost(TWO_SECONDS)
                .pollDelay(ONE_HUNDRED_MILLISECONDS)
                .until(() -> connection.getMessages().size() > 0);

        assertThat(connection.getMessages(), hasItem(hasKey("id")));
    }

    @Test
    public void shouldSendSmtpLogThroughWebSocketWhenMailReceived() throws Exception {
        Handler connection = connectToWebSocket(TOPIC_SMTP_LOG);
        sendMail();

        await().atMost(TEN_SECONDS)
                .pollDelay(ONE_HUNDRED_MILLISECONDS)
                .until(() -> connection.getMessages().size() > 12);

        assertThat(connection.getMessages(), hasItem(allOf(
                hasEntry(is("line"), is("Server: 250 Ok")),
                hasEntry(is("time"), notNullValue())
        )));
    }

    private Handler connectToWebSocket(String topic) throws InterruptedException {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        Handler session = new Handler(topic);
        ListenableFuture<StompSession> connecting = stompClient.connect("ws://localhost:" + SERVER_PORT + WEB_SOCKET_PATH, session);
        await().atMost(2, SECONDS).until(connecting::isDone);
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

    private static class Handler extends StompSessionHandlerAdapter {

        private final String topic;

        private List<Map<String, String>> messages = new ArrayList<>();

        Handler(String topic) {
            this.topic = topic;
        }

        List<Map<String, String>> getMessages() throws InterruptedException {
            return messages;
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
                }
            });
        }
    }
}
