package net.kprod.firewatch.service;

import net.kprod.firewatch.data.*;
import net.kprod.firewatch.exc.CheckException;
import net.kprod.firewatch.persistence.Event;
import net.kprod.firewatch.persistence.EventHistory;
import net.kprod.firewatch.persistence.RepositoryEvent;
import net.kprod.firewatch.persistence.RepositoryHistoryEvent;
import net.kprod.firewatch.sched.RunnableTask;
import org.apache.commons.codec.binary.Base64;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CheckServiceImpl implements CheckService {
    private Logger LOG = LoggerFactory.getLogger(CheckService.class);

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private RepositoryEvent repositoryEvent;

    @Autowired
    private RepositoryHistoryEvent repositoryHistoryEvent;

    @Autowired
    private AlertService alertService;

    @Value("${jasypt.encryptor.password}")
    private String cryptKey;

    private ClientHttpRequestFactory getClientHttpRequestFactory(int readTimeout) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        //clientHttpRequestFactory.setConnectTimeout(1000);
        //clientHttpRequestFactory.setConnectionRequestTimeout(5000);
        clientHttpRequestFactory.setReadTimeout(readTimeout);
        return clientHttpRequestFactory;
    }

    private String decryptCandidate(String candidateText) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(cryptKey);
        Pattern p = Pattern.compile("ENC\\((.+)\\)");
        Matcher m = p.matcher(candidateText);

        if(m.find()) {
            return textEncryptor.decrypt(m.group(1));
        }
        return candidateText;
    }

    @Override
    public void checkUrl(CheckContext cc) {

        if(cc.isActive() == false) {
            return;
        }

        boolean state = false;
        int count = cc.getRetry();

        CheckResult cr = check(cc);

        if (cr.getCheckStatus().equals(CheckStatus.ok)) {
            state = true;
        } else {
            LOG.warn(cc.getName() + " failed " + (cc.getRetry() > 0 ? "(retries left " + cc.getRetry() + ")" : ""));
            while(state == false && count > 0) {
                sleep(cc.getRetry_delay());
                cr = check(cc);
                state = cr.getCheckStatus().equals(CheckStatus.ok);
                count--;
                if(count > 0) {
                    LOG.warn(cc.getName() + " retry #" + (count + 1) + " failed (retries left " + (cc.getRetry() - count) + ")");
                } else {
                    LOG.warn(cc.getName() + " retry #" + (count + 1) + " failed");
                }
            }
        }
        List<Event> events = repositoryEvent.findEventByNameOrderByEventDateDesc(cc.getName());
        Optional<Event> optLastEvent = events.stream().findFirst();
        if(state == false) {

            if(optLastEvent.isPresent()) {
                Event lastEvent = optLastEvent.get();
                if(lastEvent.getStatus().equals("down")) {
                    LOG.error(cc.getName() + " is still down");
                } else {
                    LOG.error(cc.getName() + " is down");
                    Event event = new Event(ZonedDateTime.now(), cc.getName(), "down");
                    repositoryEvent.save(event);
                    alertService.sendCheckAlert(cc, cr);
                }
            } else {
                LOG.error(cc.getName() + " is down");
                Event event = new Event(ZonedDateTime.now(), cc.getName(), "down");
                repositoryEvent.save(event);
                alertService.sendCheckAlert(cc, cr);
            }
        } else {
            if(optLastEvent.isPresent()) {
                Event lastEvent = optLastEvent.get();
                if(lastEvent.getStatus().equals("down")) {
                    LOG.info(cc.getName() + " is up again");
                    Event event = new Event(ZonedDateTime.now(), cc.getName(), "up");
                    repositoryEvent.save(event);
                    alertService.sendCheckAlert(cc, cr);
                } else {
                    LOG.info(cc.getName() + " is up");
                }
            } else {
                LOG.info(cc.getName() + " is up");
                Event event = new Event(ZonedDateTime.now(), cc.getName(), "up");
                repositoryEvent.save(event);
                alertService.sendCheckAlert(cc, cr);
            }
        }
        EventHistory eH = new EventHistory(cc.getName(), TimeUnit.event, cr.getLiveStatus(), cr.getReponseTime());
        repositoryHistoryEvent.save(eH);
    }

    private void sleep(int interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    private CheckResult check(CheckContext cc) {
        CheckResult checkResult;
        try {
            CheckResponse res = checkTimeout(cc);
            String body = res.getRes().getBody();
            checkResult = new CheckResult(res.getRes().getStatusCode(), LiveStatus.up, res.getDuration() / 1000000);

            LOG.info(cc.getName() + " " + res.getRes().getStatusCode() + " in " + (res.getDuration() / 1000000) + "ms");
            if(cc.getContent() != null && cc.getContent().isEmpty() == false) {
                if(body == null || body.contains(cc.getContent()) == false) {
                    checkResult.setBodyContentCheck(BodyContentCheck.ko);
                    checkResult.setCheckStatus(CheckStatus.ko);
                }
                else {
                    checkResult.setBodyContentCheck(BodyContentCheck.ok);
                    checkResult.setCheckStatus(CheckStatus.ok);
                }
            } else {
                checkResult.setBodyContentCheck(BodyContentCheck.not_required);
                checkResult.setCheckStatus(CheckStatus.ok);
            }
        } catch (CheckException e) {
            LOG.error(cc.getName() + " " + e.getMessage());
            checkResult = new CheckResult(HttpStatus.I_AM_A_TEAPOT, LiveStatus.down, -1);
            checkResult.setBodyContentCheck(BodyContentCheck.not_required);
            checkResult.setCheckStatus(CheckStatus.ko);
            checkResult.setException(e);
        }
        return checkResult;
    }

    class CheckResponse {
        private ResponseEntity<String> res;
        private long duration;

        public CheckResponse(ResponseEntity<String> res, long duration) {
            this.res = res;
            this.duration = duration;
        }

        public ResponseEntity<String> getRes() {
            return res;
        }

        public long getDuration() {
            return duration;
        }
    }

    void basicAuth(HttpHeaders h, String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        h.set("Authorization", authHeader);
    }

    void bearer(HttpHeaders h, String token) {
        String authHeader = "Bearer " + token;
        h.set("Authorization", authHeader);
    }

    private CheckResponse checkTimeout(CheckContext cc) throws CheckException {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory(cc.getTimeout()));

        long duration = 0;
        try {
            duration = System.nanoTime();
            ResponseEntity<String> res = null;

            HttpHeaders h = new HttpHeaders();

            String url = cc.getUrl() + (cc.getParams() == null ? "" : decryptCandidate(cc.getParams()));

            if(cc.getAuthType() == CheckContext.AuthType.basic) {

                basicAuth(h, decryptCandidate(cc.getUsername()), decryptCandidate(cc.getPassword()));
                HttpEntity e = new HttpEntity(h);

                res = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        e,
                        String.class);

            } else if (cc.getAuthType() == CheckContext.AuthType.bearer) {
                bearer(h, decryptCandidate(cc.getBearer()));
                HttpEntity e = new HttpEntity(h);

                res = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        e,
                        String.class);
            } else if (cc.getAuthType() == CheckContext.AuthType.none) {
                res = restTemplate.getForEntity(url, String.class);
            }
            duration = System.nanoTime() - duration;
            return new CheckResponse(res, duration);
        } catch (ResourceAccessException e) {
            duration = System.nanoTime() - duration;
            throw new CheckException("Read timeout after " + (duration / 1000000) + "ms", e);
        } catch (RestClientException e) {
            duration = System.nanoTime() - duration;
            throw new CheckException("Rest Exception after " + (duration / 1000000) + "ms", e);
        }
    }

    public void setCheckContextItem(CheckContext cc) {
        LOG.info(" - Create " + cc.toString());
        Event event = new Event(ZonedDateTime.now(), cc.getName(), "up");
        repositoryEvent.save(event);
        taskScheduler.scheduleWithFixedDelay(new RunnableTask(ctx, cc), cc.getDelay());
    }

}
