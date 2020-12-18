package com.github.ksokol.mailsink.entity;

import com.github.ksokol.mailsink.controller.MailController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * @author Kamill Sokol
 */
@Component
public class MailResourceProcessor implements RepresentationModelProcessor<EntityModel<Mail>> {

    @Override
    public EntityModel<Mail> process(EntityModel<Mail> resource) {
        Mail mail = resource.getContent();

        resource.add(linkTo(MailController.class)
                .slash(mail.getId())
                .slash("source")
                .withRel("source"));

        if (StringUtils.isNotBlank(mail.getHtml())) {
            resource.add(linkTo(MailController.class)
                    .slash(mail.getId())
                    .slash("html")
                    .slash("query")
                    .withRel("query"));
        }

        return resource;
    }
}
