package net.kprod.firewatch.service;

import net.kprod.firewatch.data.config.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MailServiceImpl implements MailService {
    private Logger LOG = LoggerFactory.getLogger(WatchService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${firewatch.email.enable}")
    private boolean enable;

    private Map<String, Recipient> mapGroups;

    public void setRecipient(String name, Recipient r) {
        if(mapGroups == null) {
            mapGroups = new HashMap<>();
        }
        LOG.info(" - Create recipient [{}] with [{}] address", name, r.getEmails().size());
        mapGroups.put(name, r);
    }

    @Override
    public void sendGroupMessage(List<String> groups, String subject, String body) {
        List<String> addresses = groups.stream()
                .flatMap(g -> mapGroups.get(g).getEmails().stream())
                .collect(Collectors.toList());

        String[]to = addresses.stream().toArray(String[]::new);
        sendSimpleMessage(to, subject, body);
    }

    public void sendSimpleMessage(String[] to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("supervision.hy@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        if(enable) {
            emailSender.send(message);
        } else {
            LOG.info("- Email messaging is disabled");
            LOG.info("- to [{}]", to);
            LOG.info("- subject [{}]", subject);
            LOG.info("- ------- MESSAGE ----- -");
            LOG.info(body);
            LOG.info("- --------------------- -");
        }
    }

}
