package net.kprod.firewatch.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryEvent extends JpaRepository<Event, Long> {
    List<Event> findEventByNameOrderByEventDateDesc(String name);
}
