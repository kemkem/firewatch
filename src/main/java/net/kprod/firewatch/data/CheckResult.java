package net.kprod.firewatch.data;


import org.springframework.http.HttpStatus;

import java.util.Optional;

public class CheckResult {
    private HttpStatus status;
    private BodyContentCheck bodyContentCheck;
    private LiveStatus liveStatus;
    private CheckStatus checkStatus;
    private long reponseTime;
    private Optional<Exception> optException;

    public CheckResult(HttpStatus status, LiveStatus liveStatus, long reponseTime) {
        this.status = status;
        this.liveStatus = liveStatus;
        this.reponseTime = reponseTime;
        this.optException = Optional.empty();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public BodyContentCheck getBodyContentCheck() {
        return bodyContentCheck;
    }

    public LiveStatus getLiveStatus() {
        return liveStatus;
    }

    public CheckStatus getCheckStatus() {
        return checkStatus;
    }

    public long getReponseTime() {
        return reponseTime;
    }

    public Optional<Exception> getOptException() {
        return optException;
    }

    public void setException(Exception exception) {
        this.optException = Optional.of(exception);
    }

    public void setBodyContentCheck(BodyContentCheck bodyContentCheck) {
        this.bodyContentCheck = bodyContentCheck;
    }

    public void setCheckStatus(CheckStatus checkStatus) {
        this.checkStatus = checkStatus;
    }
}
