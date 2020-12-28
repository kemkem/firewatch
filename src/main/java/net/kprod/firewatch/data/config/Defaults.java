
package net.kprod.firewatch.data.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timeout",
    "retry",
    "delay",
    "retry_delay",
    "slack_channel"
})
public class Defaults {

    @JsonProperty("timeout")
    private Integer timeout;
    @JsonProperty("retry")
    private Integer retry;
    @JsonProperty("delay")
    private Integer delay;
    @JsonProperty("retry_delay")
    private Integer retryDelay;
    @JsonProperty("slack_channel")
    private String slackChannel;

    @JsonProperty("timeout")
    public Integer getTimeout() {
        return timeout;
    }

    @JsonProperty("timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @JsonProperty("retry")
    public Integer getRetry() {
        return retry;
    }

    @JsonProperty("retry")
    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    @JsonProperty("delay")
    public Integer getDelay() {
        return delay;
    }

    @JsonProperty("delay")
    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    @JsonProperty("retry_delay")
    public Integer getRetryDelay() {
        return retryDelay;
    }

    @JsonProperty("retry_delay")
    public void setRetryDelay(Integer retryDelay) {
        this.retryDelay = retryDelay;
    }

    @JsonProperty("slack_channel")
    public String getSlackChannel() {
        return slackChannel;
    }

    @JsonProperty("slack_channel")
    public void setSlackChannel(String slackChannel) {
        this.slackChannel = slackChannel;
    }

}
