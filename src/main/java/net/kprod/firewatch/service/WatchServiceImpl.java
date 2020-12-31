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
import org.springframework.web.client.HttpClientErrorException;
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
public class WatchServiceImpl implements WatchService {
    private Logger LOG = LoggerFactory.getLogger(WatchService.class);

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
    public void checkUrl(WatchedElement we, boolean boot) {

        if(we.isActive() == false) {
            return;
        }

        boolean state = false;
        //if boot mode, do not retry
        int count = boot == false ? we.getRetry() : 0;

        WatchResult wr = check(we);

        //TODO : when last status is down, and new status is not up or down, there's no change (eg : down -> content_fails)

        //TODO need to be optimized
        List<Event> events = repositoryEvent.findEventByNameOrderByEventDateDesc(we.getName());
        Optional<Event> optLastEvent = events.stream().findFirst();

        if (wr.getWatchStatus().equals(WatchStatus.ok)) {
            state = true;
        } else {

            //do not retry if last event was down
            if(optLastEvent.isPresent() && optLastEvent.get().getStatus().equals("down")) {
                count = 0;
                LOG.warn(we.getName() + " still fails");
            } else {
                LOG.warn(we.getName() + " failed " + (we.getRetry() > 0 ? "(retries left " + we.getRetry() + ")" : ""));
            }

            while(state == false && count > 0) {
                sleep(we.getRetry_delay());
                wr = check(we);
                state = wr.getWatchStatus().equals(WatchStatus.ok);
                count--;
                if(count > 0) {
                    LOG.warn(we.getName() + " retry #" + (count + 1) + " failed (retries left " + (we.getRetry() - count) + ")");
                } else {
                    LOG.warn(we.getName() + " retry #" + (count + 1) + " failed");
                }
            }
        }
        if(state == false) {
            if(optLastEvent.isPresent()) {
                Event lastEvent = optLastEvent.get();
                if(lastEvent.getStatus().equals("down")) {
                    LOG.error(we.getName() + " is still down");
                } else {
                    LOG.error(we.getName() + " is down");
                    Event event = new Event(ZonedDateTime.now(), we.getName(), "down");
                    repositoryEvent.save(event);
                    alertService.sendCheckAlert(we, wr);
                }
            } else {
                LOG.error(we.getName() + " is down");
                Event event = new Event(ZonedDateTime.now(), we.getName(), "down");
                repositoryEvent.save(event);
                alertService.sendCheckAlert(we, wr);
            }
        } else {
            if(optLastEvent.isPresent()) {
                Event lastEvent = optLastEvent.get();
                if(lastEvent.getStatus().equals("down")) {
                    LOG.info(we.getName() + " is up again");
                    Event event = new Event(ZonedDateTime.now(), we.getName(), "up");
                    repositoryEvent.save(event);
                    alertService.sendCheckAlert(we, wr);
                } else {
                    LOG.info(we.getName() + " is up");
                }
            } else {
                LOG.info(we.getName() + " is up");
                Event event = new Event(ZonedDateTime.now(), we.getName(), "up");
                repositoryEvent.save(event);
                alertService.sendCheckAlert(we, wr);
            }
        }
        EventHistory eH = new EventHistory(we.getName(), TimeUnit.event, wr.getLiveStatus(), wr.getReponseTime());
        repositoryHistoryEvent.save(eH);
    }

    private void sleep(int interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
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

    private WatchResult check(WatchedElement cc) {
        WatchResult watchResult;
        try {
            CheckResponse res = checkTimeout(cc);
            String body = res.getRes().getBody();

            watchResult = new WatchResult()
                    .setStatus(res.getRes().getStatusCode())
                    .setWatchStatus(WatchStatus.ko)
                    .setBodyContentCheck(BodyContentCheck.not_required)
                    .setLiveStatus(LiveStatus.up)
                    .setReponseTime(res.getDuration() / 1000000);

            LOG.info(cc.getName() + " " + res.getRes().getStatusCode() + " in " + (res.getDuration() / 1000000) + "ms");
            if(cc.getContent() != null && cc.getContent().isEmpty() == false) {
                if(body == null || body.contains(cc.getContent()) == false) {
                    watchResult.setBodyContentCheck(BodyContentCheck.ko);
                    watchResult.setWatchStatus(WatchStatus.ko);
                }
                else {
                    watchResult.setBodyContentCheck(BodyContentCheck.ok);
                    watchResult.setWatchStatus(WatchStatus.ok);
                }
            } else {
                watchResult.setBodyContentCheck(BodyContentCheck.not_required);
                watchResult.setWatchStatus(WatchStatus.ok);
            }
        } catch (CheckException e) {
            LOG.error(cc.getName() + " " + e.getMessage());

            watchResult = new WatchResult()
                    .setWatchStatus(WatchStatus.ko)
                    .setBodyContentCheck(BodyContentCheck.not_required)
                    .setLiveStatus(LiveStatus.down)
                    .setReponseTime(-1);

            if(e.getCause() != null && e.getCause() instanceof Exception) {
                watchResult.setOptException(Optional.of((Exception) e.getCause()));
            } else {
                watchResult.setOptException(Optional.of(e));
            }

            if(e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException httpClientErrorException = (HttpClientErrorException) e.getCause();
                watchResult.setStatus(httpClientErrorException.getStatusCode());
            }
        }
        return watchResult;
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

    private CheckResponse checkTimeout(WatchedElement cc) throws CheckException {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory(cc.getTimeout()));

        long duration = 0;
        try {
            duration = System.nanoTime();
            ResponseEntity<String> res = null;

            HttpHeaders h = new HttpHeaders();

            String url = cc.getUrl() + (cc.getParams() == null ? "" : decryptCandidate(cc.getParams()));

            if(cc.getAuthType() == WatchedElement.AuthType.basic) {

                basicAuth(h, decryptCandidate(cc.getUsername()), decryptCandidate(cc.getPassword()));
                HttpEntity e = new HttpEntity(h);

                res = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        e,
                        String.class);

            } else if (cc.getAuthType() == WatchedElement.AuthType.bearer) {
                bearer(h, decryptCandidate(cc.getBearer()));
                HttpEntity e = new HttpEntity(h);

                res = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        e,
                        String.class);
            } else if (cc.getAuthType() == WatchedElement.AuthType.none) {
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

    public void watchElement(WatchedElement cc) {
        LOG.info(" - Added " + cc.toString());
        Event event = new Event(ZonedDateTime.now(), cc.getName(), "up");
        repositoryEvent.save(event);
        taskScheduler.scheduleWithFixedDelay(new RunnableTask(ctx, cc), cc.getDelay());
    }

}
