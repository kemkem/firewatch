package net.kprod.firewatch.controller;

import net.kprod.firewatch.persistence.Event;
import net.kprod.firewatch.persistence.EventHistory;
import net.kprod.firewatch.persistence.RepositoryEvent;
import net.kprod.firewatch.persistence.RepositoryHistoryEvent;
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

    @GetMapping("/data/performance/last")
    public ResponseEntity<EventHistory> performanceLast() {

        EventHistory eh = rhe.findAll().stream()
                .sorted(Comparator.comparing(EventHistory::getId).reversed())
                .findFirst().get();

        return ResponseEntity.ok(eh);
    }

    @GetMapping("/data/performance")
    public ResponseEntity<List<EventHistory>> performance() {

        List<EventHistory> list = rhe.findAll().stream()
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/data/performance/{site}")
    public ResponseEntity<List<EventHistory>> performance(@PathVariable String site) {

        List<EventHistory> list = rhe.findAll().stream()
                .filter(e -> e.getName().equals(site))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/data/events/last")
    public ResponseEntity<Event> eventsLast() {

        Event e = rh.findAll().stream()
                .sorted(Comparator.comparing(Event::getId).reversed())
                .findFirst().get();

        return ResponseEntity.ok(e);
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
