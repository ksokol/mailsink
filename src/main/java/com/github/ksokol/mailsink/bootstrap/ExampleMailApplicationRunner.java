package com.github.ksokol.mailsink.bootstrap;

import com.github.ksokol.mailsink.repository.MailRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;

@ConditionalOnExampleMailsEnabled
@Component
class ExampleMailApplicationRunner implements ApplicationRunner {

  private final MailRepository mailRepository;
  private final ExampleMails exampleMails;

  public ExampleMailApplicationRunner(MailRepository mailRepository, ExampleMails exampleMails) {
    this.mailRepository = Objects.requireNonNull(mailRepository, "mailRepository is null");
    this.exampleMails = Objects.requireNonNull(exampleMails, "exampleMails is null");
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    exampleMails.listExampleMails().forEach(mailRepository::save);
  }
}
