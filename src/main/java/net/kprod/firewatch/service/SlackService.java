package net.kprod.firewatch.service;

public interface SlackService {
    void configure();
    void welcomeMessage(String message);
    void sendChannelMessage(String slackChannelName, String message);
}
