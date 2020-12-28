package net.kprod.firewatch.controller;

import net.kprod.firewatch.persistence.Event;
import net.kprod.firewatch.persistence.EventHistory;
import net.kprod.firewatch.persistence.RepositoryEvent;
import net.kprod.firewatch.persistence.RepositoryHistoryEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class StatController {
    @Autowired
    private RepositoryEvent rh;

    @Autowired
    private RepositoryHistoryEvent rhe;

    @GetMapping("/data/history")
    public ResponseEntity<List<EventHistory>> history(@RequestParam String site) {

        List<EventHistory> list = rhe.findAll().stream()
                .filter(e -> e.getName().equals(site))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/data/events")
    public ResponseEntity<List<Event>> events(@RequestParam String site, @RequestParam ChronoUnit unit, @RequestParam int count) {

        ZonedDateTime zdt = ZonedDateTime.now()
                .minus(count, unit);

        List<Event> list = rh.findAll().stream()
                .filter(e -> e.getName().equals(site))
                .filter(e -> e.getEventDate().isAfter(zdt))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/data/history/all")
    public ResponseEntity<List<EventHistory>> history() {

        List<EventHistory> list = rhe.findAll().stream()
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/data/events/all")
    public ResponseEntity<List<Event>> events(@RequestParam ChronoUnit unit, @RequestParam int count) {

        ZonedDateTime zdt = ZonedDateTime.now()
                .minus(count, unit);

        List<Event> list = rh.findAll().stream()
                .filter(e -> e.getEventDate().isAfter(zdt))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }
}
