package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.MailAttachment;
import com.github.ksokol.mailsink.repository.MailAttachmentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@WebMvcTest(MailAttachmentController.class)
public class MailAttachmentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MailAttachmentRepository mailAttachmentRepository;

    private MailAttachment mailAttachment;

    @Before
    public void setUp() throws Exception {
        mailAttachment = new MailAttachment();
        mailAttachment.setId(999L);
        mailAttachment.setMimeType("text/plain");
        mailAttachment.setData(new byte[] {97});
        mailAttachment.setFilename("file.txt");
    }

    @Test
    public void shouldReturnAttachmentDataWithContentDispositionAttachment() throws Exception {
        mailAttachment.setDispositionType("attachment");

        given(mailAttachmentRepository.findById(999L)).willReturn(Optional.of(mailAttachment));

        mvc.perform(get("/mailAttachments/999/data"))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, "text/plain"))
                .andExpect(header().string(CONTENT_DISPOSITION, "attachment; filename=\"file.txt\""))
                .andExpect(content().string("a"));
    }

    @Test
    public void shouldReturnAttachmentDataWithContentDispositionInline() throws Exception {
        mailAttachment.setDispositionType("inline");

        given(mailAttachmentRepository.findById(999L)).willReturn(Optional.of(mailAttachment));

        mvc.perform(get("/mailAttachments/999/data"))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, "text/plain"))
                .andExpect(header().string(CONTENT_DISPOSITION, "inline; filename=\"file.txt\""))
                .andExpect(content().string("a"));
    }
}
