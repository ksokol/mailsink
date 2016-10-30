package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
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

    @Mock
    private MailConverter mailConverter;

    @Test
    public void shouldAcceptEveryMessage() throws Exception {
        assertThat(messageListener.accept("irrelevant", "irrelevant"), is(true));
    }

    @Test
    public void shouldCallMailConverter() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{});

        messageListener.deliver("irrelevant", "irrelevant", inputStream);

        verify(mailConverter).convert(inputStream);
    }

    @Test
    public void shouldPersistReceivedMessageInMailRepository() throws Exception {
        Mail mail = new Mail();

        given(mailConverter.convert(any())).willReturn(mail);

        messageListener.deliver("irrelevant", "irrelevant", null);

        verify(mailRepository).save(argThat(is(mail)));
    }

    @Test
    public void shouldConvertReceivedMessaageBeforeSavingInMailRepository() throws Exception {
        messageListener.deliver("irrelevant", "irrelevant", null);

        InOrder callOrder = inOrder(mailConverter, mailRepository);

        callOrder.verify(mailConverter).convert(null);
        callOrder.verify(mailRepository).save(Mockito.<Mail>any());
    }
}
