
package net.kprod.firewatch.data.config;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "url",
    "recipients",
    "timeout",
    "retry",
    "params",
    "enabled",
    "delay",
    "retry_delay",
    "content",
    "auth_type",
    "username",
    "password",
    "bearer",
    "slack_channel"
})
public class Element {

    @JsonProperty("name")
    private String name;
    @JsonProperty("url")
    private String url;
    @JsonProperty("recipients")
    private List<String> recipients = null;
    @JsonProperty("timeout")
    private Integer timeout;
    @JsonProperty("retry")
    private Integer retry;
    @JsonProperty("params")
    private String params;
    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("delay")
    private Integer delay;
    @JsonProperty("retry_delay")
    private Integer retryDelay;
    @JsonProperty("content")
    private String content;
    @JsonProperty("auth_type")
    private String authType;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("bearer")
    private String bearer;
    @JsonProperty("slack_channel")
    private String slackChannel;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("recipients")
    public List<String> getRecipients() {
        return recipients;
    }

    @JsonProperty("recipients")
    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

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

    @JsonProperty("params")
    public String getParams() {
        return params;
    }

    @JsonProperty("params")
    public void setParams(String params) {
        this.params = params;
    }

    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    @JsonProperty("auth_type")
    public String getAuthType() {
        return authType;
    }

    @JsonProperty("auth_type")
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty("bearer")
    public String getBearer() {
        return bearer;
    }

    @JsonProperty("bearer")
    public void setBearer(String bearer) {
        this.bearer = bearer;
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
