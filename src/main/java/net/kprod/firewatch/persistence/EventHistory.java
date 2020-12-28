package net.kprod.firewatch.persistence;

import net.kprod.firewatch.data.LiveStatus;
import net.kprod.firewatch.data.TimeUnit;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
public class EventHistory {
    private long id;
    private ZonedDateTime eventDate;
    private String name;
    private TimeUnit timeUnit;
    private LiveStatus liveStatus;
    private long response;

    public EventHistory() {
    }

    public EventHistory(String name, TimeUnit timeUnit, LiveStatus liveStatus, long response) {
        this.eventDate = ZonedDateTime.now();
        this.name = name;
        this.timeUnit = timeUnit;
        this.liveStatus = liveStatus;
        this.response = response;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Enumerated(EnumType.STRING)
    public LiveStatus getLiveStatus() {
        return liveStatus;
    }

    public void setLiveStatus(LiveStatus liveStatus) {
        this.liveStatus = liveStatus;
    }

    public long getResponse() {
        return response;
    }

    public void setResponse(long response) {
        this.response = response;
    }

    @Enumerated(EnumType.STRING)
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public String toString() {
        return "EventHistory{" +
                "id=" + id +
                ", eventDate=" + eventDate +
                ", name='" + name + '\'' +
                ", timeUnit=" + timeUnit +
                ", liveStatus=" + liveStatus +
                ", response=" + response +
                '}';
    }
}
