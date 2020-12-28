package net.kprod.firewatch.service;

import net.kprod.firewatch.data.config.Recipient;

import java.util.List;

public interface MailService {
    void sendGroupMessage(List<String> groups, String subject, String body);
    void sendSimpleMessage(String[] to, String subject, String body);
    void setRecipient(String name, Recipient recipient);
}
