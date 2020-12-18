package com.github.ksokol.mailsink.repository;

import com.github.ksokol.mailsink.entity.Mail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface MailRepository extends CrudRepository<Mail, Long> {

    @Query("select m from Mail as m order by m.createdAt desc")
    List<Mail> findAllOrderByCreatedAtDesc();

    @Query("select m from Mail as m where recipient = ?1")
    List<Mail> findByRecipient(@Param("recipient") String recipient);
}
