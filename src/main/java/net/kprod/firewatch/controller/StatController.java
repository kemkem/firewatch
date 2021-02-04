package net.kprod.firewatch.controller;

import net.kprod.firewatch.data.TimeUnit;
import net.kprod.firewatch.data.WatchedElement;
import net.kprod.firewatch.persistence.Event;
import net.kprod.firewatch.persistence.EventHistory;
import net.kprod.firewatch.persistence.RepositoryEvent;
import net.kprod.firewatch.persistence.RepositoryHistoryEvent;
import net.kprod.firewatch.service.Configure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class StatController {
    @Autowired
    private RepositoryEvent rh;

    @Autowired
    private RepositoryHistoryEvent rhe;

    @Autowired
    private Configure configure;

    @GetMapping("/data/sites/names")
    List<String> sites() {
        return configure.getListCC().stream()
                .map(WatchedElement::getName)
                .collect(Collectors.toList());
    }

    @GetMapping("/data/performance/{site}/last")
    public ResponseEntity<EventHistory> performanceLast(@PathVariable String site) {

        EventHistory eh = rhe.findAll().stream()
                .filter(e -> e.getName().equals(site))
                .sorted(Comparator.comparing(EventHistory::getId).reversed())
                .findFirst().get();

        return ResponseEntity.ok(eh);
    }

    @GetMapping("/data/performance/{site}/all")
    public ResponseEntity<List<EventHistory>> performance(@PathVariable String site, @RequestParam TimeUnit timeUnitFilter) {

        List<EventHistory> list = rhe.findAll().stream()
                .filter(e -> e.getName().equals(site))
                .filter(e -> e.getTimeUnit().equals(timeUnitFilter))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/data/events/{site}/last")
    public ResponseEntity<Event> eventsLast(@PathVariable String site) {

        Event ev = rh.findAll().stream()
                .filter(e -> e.getName().equals(site))
                .sorted(Comparator.comparing(Event::getId).reversed())
                .findFirst().get();

        return ResponseEntity.ok(ev);
    }

    @GetMapping("/data/events/{site}")
    public ResponseEntity<List<Event>> events(@PathVariable String site, @RequestParam String strUnit, @RequestParam int count) {

        ChronoUnit unit = ChronoUnit.valueOf(strUnit);

        ZonedDateTime zdt = ZonedDateTime.now()
                .minus(count, unit);

        List<Event> list = rh.findAll().stream()
                .filter(e -> e.getName().equals(site))
                .filter(e -> e.getEventDate().isAfter(zdt))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/data/events")
    public ResponseEntity<List<Event>> events(@RequestParam String strUnit, @RequestParam int count) {

        ChronoUnit unit = ChronoUnit.valueOf(strUnit);

        ZonedDateTime zdt = ZonedDateTime.now()
                .minus(count, unit);

        List<Event> list = rh.findAll().stream()
                .filter(e -> e.getEventDate().isAfter(zdt))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }
}
