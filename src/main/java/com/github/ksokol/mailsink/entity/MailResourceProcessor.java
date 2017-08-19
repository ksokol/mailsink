package com.github.ksokol.mailsink.entity;

import com.github.ksokol.mailsink.controller.MailController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author Kamill Sokol
 */
@Component
public class MailResourceProcessor implements ResourceProcessor<Resource<Mail>> {

    @Override
    public Resource<Mail> process(Resource<Mail> resource) {
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
