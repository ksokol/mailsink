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
class MailAttachmentResourceTest {

  @Autowired
  private MockMvc mvc;

  @Test
  void shouldContainExpectedCollectionResponse() throws Exception {
    mvc.perform(get("/mailAttachments"))
      .andExpect(status().isOk())
      .andExpect(header().string(CONTENT_TYPE, HAL_JSON_VALUE))
      .andExpect(jsonPath("_embedded.mailAttachments.length()").value(1))
      .andExpect(jsonPath("_embedded.mailAttachments[0].id").doesNotExist())
      .andExpect(jsonPath("_embedded.mailAttachments[0].mail").doesNotExist())
      .andExpect(jsonPath("_embedded.mailAttachments[0].data").doesNotExist())
      .andExpect(jsonPath("_embedded.mailAttachments[0].contentId").doesNotExist())
      .andExpect(jsonPath("_embedded.mailAttachments[0].filename").value("expectedFilename.jpg"))
      .andExpect(jsonPath("_embedded.mailAttachments[0].mimeType").value("image/jpeg"))
      .andExpect(jsonPath("_embedded.mailAttachments[0].dispositionType").value("inline"))
      .andExpect(jsonPath("_embedded.mailAttachments[0]._links.data.href").value("http://localhost/mailAttachments/0/data"));
  }

  @Test
  void shouldContainExpectedResourceResponse() throws Exception {
    mvc.perform(get("/mailAttachments/0"))
      .andExpect(status().isOk())
      .andExpect(header().string(CONTENT_TYPE, HAL_JSON_VALUE))
      .andExpect(jsonPath("_embedded.mailAttachments[0].id").doesNotExist())
      .andExpect(jsonPath("_embedded.mailAttachments[0].mail").doesNotExist())
      .andExpect(jsonPath("_embedded.mailAttachments[0].data").doesNotExist())
      .andExpect(jsonPath("_embedded.mailAttachments[0].contentId").doesNotExist())
      .andExpect(jsonPath("filename").value("expectedFilename.jpg"))
      .andExpect(jsonPath("mimeType").value("image/jpeg"))
      .andExpect(jsonPath("dispositionType").value("inline"))
      .andExpect(jsonPath("_links.data.href").value("http://localhost/mailAttachments/0/data"));
  }
}
