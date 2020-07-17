package com.github.ksokol.mailsink.repository;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailAttachment;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = false)
public class MailRepositoryTest {

    private static final LocalDateTime EPOCH_UTC = LocalDateTime.now(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC")));

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private TestEntityManager em;

    @Before
    @After
    public void cleanUp() {
        mailRepository.deleteAll();
    }

    @Test
    public void shouldPersistMail() {
        Date cratedAt = toDate(EPOCH_UTC);

        Mail mail = new Mail();
        mail.setMessageId("messageId");
        mail.setSubject("subject");
        mail.setSender("sender");
        mail.setRecipient("recipient");
        mail.setText("plain");
        mail.setHtml("html");
        mail.setSource("source");
        mail.setCreatedAt(cratedAt);

        mail = em.persist(mail);
        Mail expected = mailRepository.findById(mail.getId()).orElseThrow(() -> new AssertionFailedError("mail not found"));

        assertThat(expected.getId()).isEqualTo(mail.getId());
        assertThat(expected.getMessageId()).isEqualTo("messageId");
        assertThat(expected.getSender()).isEqualTo("sender");
        assertThat(expected.getRecipient()).isEqualTo("recipient");
        assertThat(expected.getSubject()).isEqualTo("subject");
        assertThat(expected.getText()).isEqualTo("plain");
        assertThat(expected.getHtml()).isEqualTo("html");
        assertThat(expected.getSource()).isEqualTo("source");
        assertThat(expected.getCreatedAt()).isEqualTo(cratedAt);
    }

    @Test
    public void shouldOrderMailsByCreationDateDescending() {
        Mail mail1 = new Mail();
        mail1.setCreatedAt(toDate(EPOCH_UTC));
        em.persist(mail1);

        Mail mail2 = new Mail();
        mail2.setCreatedAt(toDate(EPOCH_UTC.plusMinutes(1)));
        em.persist(mail2);

        assertThat(mailRepository.findAllOrderByCreatedAtDesc())
                .as("should order mails by creation date descending")
                .containsExactly(mail2, mail1);
    }

    @Test
    public void shouldSaveMailWithAttachment() {
        Mail mail = new Mail();
        MailAttachment mailAttachment = new MailAttachment();
        mailAttachment.setMail(mail);
        mail.setAttachments(Collections.singletonList(mailAttachment));

        em.persist(mail);

        assertThat(mailRepository.findAllOrderByCreatedAtDesc())
                .as("should save mail with attachment")
                .hasSize(1);
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}
