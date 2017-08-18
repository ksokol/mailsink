package com.github.ksokol.mailsink.repository;

import com.github.ksokol.mailsink.entity.MailAttachment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

/**
 * @author Kamill Sokol
 */
public interface MailAttachmentRepository extends CrudRepository<MailAttachment, Long> {

    @RestResource(exported = false)
    Optional<MailAttachment> findById(Long id);
}
