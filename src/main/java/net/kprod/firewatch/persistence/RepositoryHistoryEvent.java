package net.kprod.firewatch.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryHistoryEvent extends JpaRepository<EventHistory, Long> {
}
