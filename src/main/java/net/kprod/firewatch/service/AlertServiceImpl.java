package net.kprod.firewatch.service;

import net.kprod.firewatch.data.BodyContentCheck;
import net.kprod.firewatch.data.WatchedElement;
import net.kprod.firewatch.data.WatchResult;
import net.kprod.firewatch.data.LiveStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl implements AlertService {
    public static final String SLACK_FACE_SUNNY = ":sunny:";
    public static final String SLACK_FACE_FIRE = ":fire:";
    public static final String SLACK_FACE_RAIN = ":rain_cloud:";
    private Logger LOG = LoggerFactory.getLogger(WatchService.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private SlackService slackService;

    @Value("${firewatch.slack.channel.stats}")
    private String statChannel;

    @Override
    public void sendCheckAlert(WatchedElement we, WatchResult wr) {
        String subject = we.getName() + " alert";
        StringBuilder sbBody = new StringBuilder();

        sbBody
                .append("`").append(we.getName()).append("`")
                .append(" (url ").append(we.getUrl()).append(")")
                .append(" ")
                .append(" is `").append(wr.getLiveStatus()).append("`. ");
        if(wr.getStatus() != null) {
            sbBody
                    .append("Http response `").append(wr.getStatus()).append("`");
        }
        if(wr.getReponseTime() != -1) {
            sbBody
                    .append(" response time `" + wr.getReponseTime() + "ms`. ");
        }
        if(wr.getBodyContentCheck().equals(BodyContentCheck.ko)) {
            sbBody
                    .append("But content check fails. ");
        }
        if(wr.getOptException().isPresent()) {
            Exception e = wr.getOptException().get();
            sbBody
                    .append("Exception `")
                    .append(e.getClass().getSimpleName())
                    .append("` with message `")
                    .append(e.getMessage()).append("`.");
        }

        String body = sbBody.toString();

        //SLACK MESSAGE
        //smiley selection
        String face = SLACK_FACE_SUNNY;                              //it's up (sunny)
        if(wr.getBodyContentCheck().equals(BodyContentCheck.ko)) {  //up but content fails (rain)
            face = SLACK_FACE_RAIN;
        }
        if(wr.getLiveStatus().equals(LiveStatus.down)) {            //down (FIRE !)
            face = SLACK_FACE_FIRE;
        }
        slackService.sendChannelMessage(we.getSlackChannel(), face + " " + body);

        //MAIL MESSAGE
        if(we.getListGroups() == null) {
            LOG.info("(no recipients defined)");
            LOG.info("----");
            LOG.info(body);
            LOG.info("----");
        } else {
            LOG.info("send alert to groups " + we.getListGroups());
            mailService.sendGroupMessage(we.getListGroups(), subject, body);
        }
    }

    @Override
    public void sendStatMessage(String body) {
        slackService.sendChannelMessage(statChannel, ":thermometer: " + body);
    }
}
