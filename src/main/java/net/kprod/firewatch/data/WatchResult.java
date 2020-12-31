package net.kprod.firewatch.data;


import org.springframework.http.HttpStatus;

import java.util.Optional;

public class WatchResult {
    private HttpStatus status;
    private BodyContentCheck bodyContentCheck;
    private LiveStatus liveStatus;
    private WatchStatus watchStatus;
    private long reponseTime;
    private Optional<Exception> optException = Optional.empty();

    public HttpStatus getStatus() {
        return status;
    }

    public WatchResult setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public BodyContentCheck getBodyContentCheck() {
        return bodyContentCheck;
    }

    public WatchResult setBodyContentCheck(BodyContentCheck bodyContentCheck) {
        this.bodyContentCheck = bodyContentCheck;
        return this;
    }

    public LiveStatus getLiveStatus() {
        return liveStatus;
    }

    public WatchResult setLiveStatus(LiveStatus liveStatus) {
        this.liveStatus = liveStatus;
        return this;
    }

    public WatchStatus getWatchStatus() {
        return watchStatus;
    }

    public WatchResult setWatchStatus(WatchStatus watchStatus) {
        this.watchStatus = watchStatus;
        return this;
    }

    public long getReponseTime() {
        return reponseTime;
    }

    public WatchResult setReponseTime(long reponseTime) {
        this.reponseTime = reponseTime;
        return this;
    }

    public Optional<Exception> getOptException() {
        return optException;
    }

    public WatchResult setOptException(Optional<Exception> optException) {
        this.optException = optException;
        return this;
    }
}
