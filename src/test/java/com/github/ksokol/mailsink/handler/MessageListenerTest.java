package com.github.ksokol.mailsink.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageListenerTest {

    @InjectMocks
    private MessageListener messageListener;

    @Test
    public void shouldAcceptEveryMessage() throws Exception {
        assertThat(messageListener.accept("irrelevant", "irrelevant"), is(true));
    }

    @Test
    public void deliver() throws Exception {

    }

}
