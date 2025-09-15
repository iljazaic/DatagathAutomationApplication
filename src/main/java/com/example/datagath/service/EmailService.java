package com.example.datagath.service;

import com.itextpdf.text.DocumentException;

import jakarta.mail.MessagingException;

public interface EmailService {
      public void sendSimpleMessage(String to, String subject, String text);
      public void sendMessageWithAttachment(String to, String subject, String text, byte[] document) throws MessagingException, DocumentException;
}
