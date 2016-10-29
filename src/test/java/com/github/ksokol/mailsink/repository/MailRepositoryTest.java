package com.github.ksokol.mailsink.repository;

import com.github.ksokol.mailsink.entity.Mail;
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
import java.util.Date;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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

    @Test
    public void shouldPersistMail() throws Exception {
        Date cratedAt = toDate(EPOCH_UTC);

        Mail mail = new Mail();
        mail.setSubject("subject");
        mail.setSender("sender");
        mail.setRecipient("recipient");
        mail.setBody("body");
        mail.setCreatedAt(cratedAt);

        mail = em.persist(mail);
        Mail expected = mailRepository.findOne(mail.getId());

        assertThat(expected.getSender(), is("sender"));
        assertThat(expected.getRecipient(), is("recipient"));
        assertThat(expected.getSubject(), is("subject"));
        assertThat(expected.getBody(), is("body"));
        assertThat(expected.getCreatedAt(), is(cratedAt));
    }

    @Test
    public void shouldOrderMailsByCreationDateDescending() throws Exception {
        Mail mail1 = new Mail();
        mail1.setCreatedAt(toDate(EPOCH_UTC));
        em.persist(mail1);

        Mail mail2 = new Mail();
        mail2.setCreatedAt(toDate(EPOCH_UTC.plusMinutes(1)));
        em.persist(mail2);

        assertThat(
                "should order mails by creation date descending",
                mailRepository.findAllOrderByCreatedAtDesc(),
                contains(hasProperty("id", is(2L)), hasProperty("id", is(1L)))
        );
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}
