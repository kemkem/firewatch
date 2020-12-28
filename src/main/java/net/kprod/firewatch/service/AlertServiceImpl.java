package net.kprod.firewatch.service;

import net.kprod.firewatch.data.BodyContentCheck;
import net.kprod.firewatch.data.CheckContext;
import net.kprod.firewatch.data.CheckResult;
import net.kprod.firewatch.data.LiveStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl implements AlertService {
    public static final String SLACK_FACE_SUNNY = ":sunny:";
    public static final String SLACK_FACE_FIRE = ":fire:";
    private Logger LOG = LoggerFactory.getLogger(CheckService.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private SlackService slackService;

    @Value("${firewatch.slack.channel.stats}")
    private String statChannel;

    @Override
    public void sendCheckAlert(CheckContext cc, CheckResult cr) {
        String subject = cc.getName() + " alert";
        StringBuilder sbBody = new StringBuilder();

        sbBody
                .append("`").append(cc.getName()).append("`")
                .append(" (url ").append(cc.getUrl()).append(")")
                .append(" ")
                .append(" is `").append(cr.getLiveStatus()).append("`. ");
        if(cr.getStatus().equals(HttpStatus.I_AM_A_TEAPOT) == false || cr.getReponseTime() != -1) {
            sbBody
                    .append("Http response `").append(cr.getStatus()).append("`")
                    .append(" response time `" + cr.getReponseTime() + "ms`. ");
        }
        if(cr.getBodyContentCheck().equals(BodyContentCheck.ko)) {
            sbBody
                    .append("But content check fails. ");
        }
        if(cr.getOptException().isPresent()) {
            Exception e = cr.getOptException().get();
            sbBody
                    .append("Exception `")
                    .append(e.getClass().getSimpleName())
                    .append("` with message `")
                    .append(e.getMessage()).append("`.");
        }

        String body = sbBody.toString();

        //SLACK MESSAGE
        String face = cr.getLiveStatus().equals(LiveStatus.up) ? SLACK_FACE_SUNNY : SLACK_FACE_FIRE;
        slackService.sendChannelMessage(cc.getSlackChannel(), face + " " + body);

        if(cc.getListGroups().isEmpty()) {
            LOG.info("(no alerts defined)");
            LOG.info("----");
            LOG.info(body);
            LOG.info("----");
        } else {
            //MAIL MESSAGE
            LOG.info("send alert to groups " + cc.getListGroups());
            mailService.sendGroupMessage(cc.getListGroups(), subject, body);
        }
    }

    @Override
    public void sendStatMessage(String body) {
        slackService.sendChannelMessage(statChannel, ":thermometer: " + body);
    }
}
