package net.kprod.firewatch.data;

import net.kprod.firewatch.exc.ConfigException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CheckContext {
    private String name;
    private String url;
    private String params;
    private int timeout;
    private int retry;
    private int delay;
    private int retry_delay;
    private String content;
    private List<String> listGroups;
    private String slackChannel;
    private boolean active;
    private String username;
    private String password;
    private String bearer;
    private AuthType authType = AuthType.none;

    public enum AuthType {
        basic,
        bearer,
        none;
    }

    public static class Builder {
        private String name;
        private String url;
        private String params;
        private int timeout;
        private int retry;
        private int delay;
        private int retry_delay;
        private String content;
        private List<String> listGroups;
        private String slackChannel;
        private boolean active;
        private String username;
        private String password;
        private String bearer;
        private AuthType authType = AuthType.none;


        public CheckContext build() throws ConfigException {
            CheckContext cc = new CheckContext();

            if(authType == AuthType.bearer && bearer == null) {
                throw new ConfigException(MessageFormat.format("Element [{}] error : bearer value requirer", name));
            }

            if(authType == AuthType.basic && (username == null || password == null)) {
                throw new ConfigException(MessageFormat.format("Element [{}] error : username and password required", name));
            }

            cc.name = name;
            cc.url = url;
            cc.params = params;
            cc.timeout = timeout;
            cc.retry = retry;
            cc.delay = delay;
            cc.retry_delay = retry_delay;
            cc.content = content;
            cc.listGroups = listGroups;
            cc.slackChannel = slackChannel;
            cc.active = active;
            cc.username = username;
            cc.password = password;
            cc.bearer = bearer;
            cc.authType = authType;

            return cc;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setParams(String params) {
            this.params = params;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setRetry(int retry) {
            this.retry = retry;
            return this;
        }

        public Builder setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public Builder setRetry_delay(int retry_delay) {
            this.retry_delay = retry_delay;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setListGroups(List<String> listGroups) {
            this.listGroups = listGroups;
            return this;
        }

        public Builder setSlackChannel(String slackChannel) {
            this.slackChannel = slackChannel;
            return this;
        }

        public Builder setActive(boolean active) {
            this.active = active;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setBearer(String bearer) {
            this.bearer = bearer;
            return this;
        }

        public Builder setAuthType(AuthType authType) {
            this.authType = authType;
            return this;
        }
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getParams() {
        return params;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRetry() {
        return retry;
    }

    public int getDelay() {
        return delay;
    }

    public int getRetry_delay() {
        return retry_delay;
    }

    public String getContent() {
        return content;
    }

    public List<String> getListGroups() {
        return listGroups;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public boolean isActive() {
        return active;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBearer() {
        return bearer;
    }

    public AuthType getAuthType() {
        return authType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(name).append(" :");
        sb.append(" url [").append(url).append("]");
        sb.append(" t, r, d, r, d [")
                .append(timeout).append(", ")
                .append(retry).append(", ")
                .append(delay).append(", ")
                .append(retry_delay).append("]");

        if (content != null) {
            sb.append(" content [").append(content).append("]");
        }

        sb.append(" auth [").append(authType).append("]");

        if(listGroups != null) {
            sb.append(" recipients [").append(listGroups.stream().collect(Collectors.joining(", "))).append("]");
        }
        sb.append(" channel [").append(slackChannel).append("]");
        if (active == false) {
            sb.append(" [disabled]");
        }
        return sb.toString();
    }

    public String toSlack() {
        StringBuilder sb = new StringBuilder();

        sb.append("`").append(name).append("` :");
        sb.append(" url `").append(url).append("`");
        if(params != null) {
            sb.append(" `+params`");
        }
        sb.append(" t/r/d/rd ")
                .append("`").append(timeout).append("`/")
                .append("`").append(retry).append("`/")
                .append("`").append(delay).append("`/")
                .append("`").append(retry_delay).append("`");

        if (content != null) {
            sb.append(" content `").append(content).append("`");
        }

        sb.append(" auth `").append(authType).append("`");

        if(listGroups != null) {
            sb.append(" recipients ")
                .append(listGroups.stream()
                    .map(g -> "`" + g + "`")
                    .collect(Collectors.joining(", ")));

        }
        sb.append(" channel `").append(slackChannel).append("`");
        if (active == false) {
            sb.append(" `disabled`");
        }
        return sb.toString();
    }
}
