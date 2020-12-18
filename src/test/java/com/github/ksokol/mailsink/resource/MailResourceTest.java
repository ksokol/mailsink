package com.github.ksokol.mailsink.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class MailResourceTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void shouldContainExpectedCollectionResponse() throws Exception {
    mvc.perform(get("/mails"))
      .andExpect(status().isOk())
      .andExpect(header().string(CONTENT_TYPE, HAL_JSON_VALUE))
      .andExpect(jsonPath("_embedded.mails.length()").value(2))
      .andExpect(jsonPath("_embedded.mails[0].id").doesNotExist())
      .andExpect(jsonPath("_embedded.mails[0].source").doesNotExist())
      .andExpect(jsonPath("_embedded.mails[0].messageId").value("<208544674.1.1477820621771.JavaMail.localhost@localhost>"))
      .andExpect(jsonPath("_embedded.mails[0].recipient").value("recipient@localhost"))
      .andExpect(jsonPath("_embedded.mails[0].sender").value("Display Name <sender@localhost>"))
      .andExpect(jsonPath("_embedded.mails[0].subject").value("mail1"))
      .andExpect(jsonPath("_embedded.mails[0].text").value("Mail body\n new line\n another line\n"))
      .andExpect(jsonPath("_embedded.mails[0].attachments").value(false))
      .andExpect(jsonPath("_embedded.mails[0].html").isEmpty())
      .andExpect(jsonPath("_embedded.mails[0].createdAt").value("2016-10-30T10:10:10.000+00:00"))
      .andExpect(jsonPath("_embedded.mails[0]._links.source.href").value("http://localhost/mails/0/source"))
      .andExpect(jsonPath("_embedded.mails[0]._links.attachments.href").value("http://localhost/mails/0/attachments"))
      .andExpect(jsonPath("_embedded.mails[0]._links.query").doesNotExist())
      .andExpect(jsonPath("_embedded.mails[1].id").doesNotExist())
      .andExpect(jsonPath("_embedded.mails[1].source").doesNotExist())
      .andExpect(jsonPath("_embedded.mails[1].messageId").value("<208544674.1.1477820621771.JavaMail.localhost@localhost>"))
      .andExpect(jsonPath("_embedded.mails[1].recipient").value("recipient1@localhost"))
      .andExpect(jsonPath("_embedded.mails[1].sender").value("sender@localhost"))
      .andExpect(jsonPath("_embedded.mails[1].subject").value("mail2"))
      .andExpect(jsonPath("_embedded.mails[1].text").value(""))
      .andExpect(jsonPath("_embedded.mails[1].attachments").value(true))
      .andExpect(jsonPath("_embedded.mails[1].html").value("inline image <img src=\"http://localhost/mailAttachments/0/data\">\n"))
      .andExpect(jsonPath("_embedded.mails[1].createdAt").value("2017-10-30T10:10:10.000+00:00"))
      .andExpect(jsonPath("_embedded.mails[1]._links.source.href").value("http://localhost/mails/1/source"))
      .andExpect(jsonPath("_embedded.mails[1]._links.attachments.href").value("http://localhost/mails/1/attachments"))
      .andExpect(jsonPath("_embedded.mails[1]._links.query.href").value("http://localhost/mails/1/html/query"));
  }

  @Test
  void shouldContainExpectedResourceResponse() throws Exception {
    mvc.perform(get("/mails/1"))
      .andExpect(status().isOk())
      .andExpect(header().string(CONTENT_TYPE, HAL_JSON_VALUE))
      .andExpect(jsonPath("id").doesNotExist())
      .andExpect(jsonPath("source").doesNotExist())
      .andExpect(jsonPath("messageId").value("<208544674.1.1477820621771.JavaMail.localhost@localhost>"))
      .andExpect(jsonPath("recipient").value("recipient1@localhost"))
      .andExpect(jsonPath("sender").value("sender@localhost"))
      .andExpect(jsonPath("subject").value("mail2"))
      .andExpect(jsonPath("text").value(""))
      .andExpect(jsonPath("attachments").value(true))
      .andExpect(jsonPath("html").value("inline image <img src=\"http://localhost/mailAttachments/0/data\">\n"))
      .andExpect(jsonPath("createdAt").value("2017-10-30T10:10:10.000+00:00"))
      .andExpect(jsonPath("_links.source.href").value("http://localhost/mails/1/source"))
      .andExpect(jsonPath("_links.attachments.href").value("http://localhost/mails/1/attachments"))
      .andExpect(jsonPath("_links.query.href").value("http://localhost/mails/1/html/query"));
  }

  @Test
  void shouldReturnMailsFromCustomFinderMethodFindByRecipient() throws Exception {
    var expectedRecipient = "recipient1@localhost";

    mvc.perform(get("/mails/search/findByRecipient?recipient={recipient}", expectedRecipient))
      .andExpect(status().isOk())
      .andExpect(header().string(CONTENT_TYPE, HAL_JSON_VALUE))
      .andExpect(jsonPath("_embedded.mails.length()").value(1))
      .andExpect(jsonPath("_embedded.mails..recipient").value(expectedRecipient));
  }

  @Test
  void shouldReturnMailsFromCustomFinderMethodFindAllOrderByCreatedAtDesc() throws Exception {
    mvc.perform(get("/mails/search/findAllOrderByCreatedAtDesc"))
      .andExpect(status().isOk())
      .andExpect(header().string(CONTENT_TYPE, HAL_JSON_VALUE))
      .andExpect(jsonPath("_embedded.mails.length()").value(2))
      .andExpect(jsonPath("_embedded.mails[0].createdAt").value("2017-10-30T10:10:10.000+00:00"))
      .andExpect(jsonPath("_embedded.mails[1].createdAt").value("2016-10-30T10:10:10.000+00:00"));
  }
}
