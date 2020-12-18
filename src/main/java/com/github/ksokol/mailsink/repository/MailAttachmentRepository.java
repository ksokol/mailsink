package com.github.ksokol.mailsink.repository;

import com.github.ksokol.mailsink.entity.MailAttachment;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Kamill Sokol
 */
public interface MailAttachmentRepository extends CrudRepository<MailAttachment, Long> {}
