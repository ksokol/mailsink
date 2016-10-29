package com.github.ksokol.mailsink.handler;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageListenerTest {

    @InjectMocks
    private MessageListener messageListener;

    @Mock
    private MailRepository mailRepository;

    @Test
    public void shouldAcceptEveryMessage() throws Exception {
        assertThat(messageListener.accept("irrelevant", "irrelevant"), is(true));
    }

    @Test
    public void shouldPersistRecieviedMessageInMailRepository() throws Exception {
        messageListener.deliver("from", "recipient", null);

        verify(mailRepository).save(argThat(Matchers.<Mail>allOf(
                hasProperty("sender", is("from")),
                hasProperty("recipient", is("recipient"))
        )));
    }
}
