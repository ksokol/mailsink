package com.github.ksokol.mailsink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Access(AccessType.PROPERTY)
@Entity
public class MailAttachment {

  private Long id;
  private String contentId;
  private String filename;
  private String mimeType;
  private String dispositionType;
  private byte[] data;
  private Mail mail;

  @Id
  @GeneratedValue
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @JsonIgnore
  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getDispositionType() {
    return dispositionType;
  }

  public void setDispositionType(String dispositionType) {
    this.dispositionType = dispositionType;
  }

  @JsonIgnore
  @Lob
  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  @JsonIgnore
  @ManyToOne(optional = false)
  public Mail getMail() {
    return mail;
  }

  public void setMail(Mail mail) {
    this.mail = mail;
  }
}
