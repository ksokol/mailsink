package com.github.ksokol.mailsink.repository;

import com.github.ksokol.mailsink.entity.Mail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

/**
 * @author Kamill Sokol
 */
public interface MailRepository extends CrudRepository<Mail, Long> {

    @Query("select m from Mail as m order by m.createdAt desc")
    List<Mail> findAllOrderByCreatedAtDesc();

    @RestResource(exported = false)
    Optional<Mail> findById(Long id);
}
