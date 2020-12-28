package net.kprod.firewatch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.kprod.firewatch.data.CheckContext;
import net.kprod.firewatch.data.config.Element;
import net.kprod.firewatch.data.config.FWConfigJson;
import net.kprod.firewatch.data.config.Recipient;
import net.kprod.firewatch.exc.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Configure {
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Value("${CONFIG_JSON_PATH}")
    private String filepath;

    @Autowired
    private CheckService checkService;

    @Autowired
    private MailService mailService;

    @Autowired
    private SlackService slackService;

    @Value("${firewatch.slack.channel.default}")
    private String defaultSlackChannel;

    @Value("${firewatch.slack.welcome.enabled}")
    private boolean enabledWelcome;

    @Value("${firewatch.slack.welcome.list}")
    private boolean enabledWelcomeList;

    private List<CheckContext> listCC = new ArrayList<>();

    @PostConstruct
    public void configureAtStartup() {
        LOG.info("- Startup configuration loading");
        slackService.configure();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            FWConfigJson configJson = objectMapper.readValue(Path.of(filepath).toFile(), FWConfigJson.class);

            for (Recipient r : configJson.getRecipients()) {
                mailService.setRecipient(r.getName(), r);
            }

            for (Element e : configJson.getElements()) {
                CheckContext cc = new CheckContext.Builder()
                    .setName(e.getName())
                    .setUrl(e.getUrl())
                    .setParams(e.getParams())
                    .setContent(e.getContent())
                    .setUsername(e.getUsername())
                    .setPassword(e.getPassword())
                    .setBearer(e.getBearer())
                    .setListGroups(e.getRecipients())
                    .setDelay(e.getDelay() != null ? e.getDelay() : configJson.getDefaults().getDelay())
                    .setRetry(e.getRetry() != null ? e.getRetry() : configJson.getDefaults().getRetry())
                    .setRetry_delay(e.getRetryDelay() != null ? e.getRetryDelay() : configJson.getDefaults().getRetryDelay())
                    .setTimeout(e.getTimeout() != null ? e.getTimeout() : configJson.getDefaults().getTimeout())
                    .setSlackChannel(e.getSlackChannel() != null ? e.getSlackChannel() : configJson.getDefaults().getSlackChannel())
                    .setActive(e.getEnabled() != null ? e.getEnabled() : true)
                    .build();

                    listCC.add(cc);
                    checkService.checkUrl(cc);
            }
        } catch (IOException e) {
            LOG.error("Failed to load config", e);
        } catch (ConfigException e) {
            LOG.error("Failed to configure an element", e);
        }

        slackSummary();
    }

    private void slackSummary() {
        StringBuilder sbSummary = new StringBuilder()
                .append("I'm monitoring `").append(getListCC().size()).append("` urls (")
                .append(
                        getListCC().stream()
                                .sorted(Comparator.comparing(CheckContext::getName))
                                .map(cc -> "`" + cc.getName() + "`")
                                .collect(Collectors.joining(", ")))
                .append("). Have a good day :sun_with_face:");

        if(enabledWelcome) {
            slackService.welcomeMessage(sbSummary.toString());
            if(enabledWelcomeList) {
                for (CheckContext cc : listCC) {
                    slackService.welcomeMessage(" >" + cc.toSlack());
                }
            }
        }
    }

    public List<CheckContext> getListCC() {
        return listCC;
    }
}