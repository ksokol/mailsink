package com.github.ksokol.mailsink.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

@JsonSerialize(using = MailSerializer.class)
@Access(AccessType.PROPERTY)
@Entity
public class Mail {

  private Long id;
  private String messageId;
  private String sender;
  private String recipient;
  private String subject;
  private String text;
  private String html;
  private String source;
  private List<MailAttachment> attachments;
  private Date createdAt;

  @Id
  @GeneratedValue
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  @Lob
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Lob
  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  @Lob
  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  @OneToMany(mappedBy = "mail", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public List<MailAttachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<MailAttachment> attachments) {
    this.attachments = attachments;
  }

  @Temporal(TemporalType.TIMESTAMP)
  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }
}
