package net.kprod.firewatch.service;

public interface SlackService {
    void configure();
    void defaultChannelMessage(String message);
    void sendChannelMessage(String slackChannelName, String message);
}
