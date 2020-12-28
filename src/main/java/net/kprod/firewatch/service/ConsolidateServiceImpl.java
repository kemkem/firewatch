package net.kprod.firewatch.service;

import net.kprod.firewatch.data.*;
import net.kprod.firewatch.exc.CheckException;
import net.kprod.firewatch.persistence.Event;
import net.kprod.firewatch.persistence.EventHistory;
import net.kprod.firewatch.persistence.RepositoryEvent;
import net.kprod.firewatch.persistence.RepositoryHistoryEvent;
import net.kprod.firewatch.sched.RunnableTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConsolidateServiceImpl implements ConsolidateService {
    private Logger LOG = LoggerFactory.getLogger(CheckService.class);

    @Autowired
    private Configure configure;

    @Autowired
    private RepositoryHistoryEvent repositoryHistoryEvent;

    @Autowired
    private AlertService alertService;

    //test
//    @Scheduled(cron = "0 0/5 * * * ?")
//    public void consolidateTest() {
//        consolidate(TimeUnit.event, TimeUnit.hour, ChronoUnit.HOURS, 1, ChronoUnit.HOURS, 1);
//    }

    //every hour
    @Scheduled(cron = "0 0 * * * ?")
    public void consolidateHourly() {
        consolidate(TimeUnit.event, TimeUnit.hour, ChronoUnit.HOURS, 1, ChronoUnit.HOURS, 1);
    }

    //every day at 00:05
    @Scheduled(cron = "0 5 0 * * ?")
    public void consolidateDaily() {
        consolidate(TimeUnit.hour, TimeUnit.day, ChronoUnit.DAYS, 1, ChronoUnit.DAYS, 1);
    }

    private void consolidate(TimeUnit consolidateTimeUnit, TimeUnit reportTimeUnit, ChronoUnit unitToConsolidate, long countToConsolidate, ChronoUnit unitToCleanAfter, long countToCleanAfter) {
        List<EventHistory> list = repositoryHistoryEvent.findAll();

        ZonedDateTime zdConsolidate = ZonedDateTime.now().minus(countToConsolidate, unitToConsolidate);
        ZonedDateTime zdClean = ZonedDateTime.now().minus(countToCleanAfter, unitToCleanAfter);

        List<EventHistory> listToConsolidate = list.stream()
                .filter(e -> e.getTimeUnit().equals(consolidateTimeUnit))
                .filter(e -> e.getEventDate().isAfter(zdConsolidate))
                .collect(Collectors.toList());
        List<EventHistory> listToCLean = list.stream()
                .filter(e -> e.getTimeUnit().equals(consolidateTimeUnit))
                .filter(e -> e.getEventDate().isBefore(zdClean))
                .collect(Collectors.toList());

        LOG.info("-- ----------- --");
        LOG.info("-- CONSOLIDATE --");
        LOG.info(" consolidate {} after {} {} - {}", consolidateTimeUnit, countToConsolidate, unitToConsolidate, zdConsolidate);
        LOG.info(" consolidate events total {}", listToConsolidate.size());
        LOG.info(" clean {} before {} {} - {}", countToCleanAfter, countToCleanAfter, unitToCleanAfter, zdConsolidate);
        LOG.info(" clean events total {}", listToCLean.size());

        for (CheckContext cc : configure.getListCC()) {
            LOG.info("  Consolidate {}", cc.getName());
            List<EventHistory> consolidateByContext = listToConsolidate.stream()
                    .filter(e -> e.getName().equals(cc.getName()))
                    .collect(Collectors.toList());

            Optional<Long> optAvg =  consolidateByContext.stream()
                    .map(e -> e.getResponse())
                    .reduce((aLong, aLong2) -> aLong + aLong2);

            if(optAvg.isPresent()) {
                long avgResponse = optAvg.get() / consolidateByContext.size();
                EventHistory lastEvent = consolidateByContext.stream()
                        .sorted(Comparator.comparing(EventHistory::getId).reversed())
                        .findFirst()
                        .get();

                LOG.info("   Last event {}", lastEvent);

                LOG.info("   {} avg response time {}", cc.getName(), avgResponse);
                EventHistory eH = new EventHistory(cc.getName(), reportTimeUnit, lastEvent.getLiveStatus(), avgResponse);
                repositoryHistoryEvent.save(eH);

                StringBuilder sbSlack = new StringBuilder();
                sbSlack.append("`").append(cc.getName()).append("`")
                        .append(" last " ).append(reportTimeUnit.equals(TimeUnit.hour) ? "hour" : "day")
                        .append(" avg response time : `").append(avgResponse).append("`ms.")
                        .append(" last known status : `").append(lastEvent.getLiveStatus()).append("`.");

                alertService.sendStatMessage(sbSlack.toString());

            } else {
                LOG.warn("   No {} to consolidate", consolidateTimeUnit);
            }
            repositoryHistoryEvent.deleteAll(listToCLean);


        }
        LOG.info("-- ----------- --");



    }

}
