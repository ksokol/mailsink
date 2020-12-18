package com.github.ksokol.mailsink.entity;

import com.github.ksokol.mailsink.controller.MailAttachmentController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * @author Kamill Sokol
 */
@Component
public class MailAttachmentResourceProcessor implements RepresentationModelProcessor<EntityModel<MailAttachment>> {

    @Override
    public EntityModel<MailAttachment> process(EntityModel<MailAttachment> resource) {
        MailAttachment attachment = resource.getContent();

        resource.add(linkTo(MailAttachmentController.class)
                .slash(attachment.getId())
                .slash("data")
                .withRel("data"));

        return resource;
    }
}
