package com.github.ksokol.mailsink.entity;

import com.github.ksokol.mailsink.controller.MailAttachmentController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author Kamill Sokol
 */
@Component
public class MailAttachmentResourceProcessor implements ResourceProcessor<Resource<MailAttachment>> {

    @Override
    public Resource<MailAttachment> process(Resource<MailAttachment> resource) {
        MailAttachment attachment = resource.getContent();

        resource.add(linkTo(MailAttachmentController.class)
                .slash(attachment.getId())
                .slash("download")
                .withRel("download"));

        return resource;
    }
}
