package net.kprod.firewatch.service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import net.kprod.firewatch.data.CheckContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class SlackServiceImpl implements SlackService {
    public static final String SLACK_FACE_ROBOT = ":robot_face:";
    private Logger LOG = LoggerFactory.getLogger(CheckService.class);

    @Value("${firewatch.slack.token}")
    private String token;

    @Value("${firewatch.slack.enable}")
    private boolean enable;

    @Value("${firewatch.slack.channel.default}")
    private String defaultChannel;

    private MethodsClient methods;

    @Override
    public void configure() {
        LOG.info("- Configure Slack firewatch bot");
        try {
            Slack slack = Slack.getInstance();
            methods = slack.methods(token);

        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void welcomeMessage(String message) {
        sendChannelMessage(defaultChannel, SLACK_FACE_ROBOT + " " + message);
    }

    @Override
    public void sendChannelMessage(String slackChannelName, String message) {
        if(enable == false) {
            LOG.info("- Slack messaging is disabled");
            LOG.info("- ------- MESSAGE ----- -");
            LOG.info(message);
            LOG.info("- --------------------- -");
            return;
        }
        LOG.info("- Sending slack channel [{}] message [{}]", slackChannelName, message);
        try {
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel("#" + slackChannelName)
                    .text(message)
                    .build();
            methods.chatPostMessage(request);
        } catch (Exception e) {
            LOG.error("Failed to send message", e);
        }
    }
}