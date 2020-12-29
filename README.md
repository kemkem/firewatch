# Firewatch

## Purpose

Firewatch is a light web monitoring service.

Firewatch is able to watch several urls, check content and track performances.

Alerts can be sent though mail system or a slack channel.

Feel free to contribute or fork.

## Alerts

Firewatch use slack to post events :

```
mywebsite (url https://myurl) is down. Exception CheckException with message Read timeout after 2321ms.
[... a few moment later ...]
mywebsite (url https://myurl) is up. Http response 200 OK response time 390ms.
```

The same king of messages are sent through email if needed.

Basic statistics are also posted hourly and daily :

```
mywebsite last hour avg response time : 154ms. last known status : up.
mywebsite last day avg response time : 182ms. last known status : up.
```

## API

Firewatch exposes a basic API with a swagger :

* `/data/events` : events (up/down) for a given `endpoint`, `count` and time `unit`
* `/data/events/all` : all events for a given `endpoint`
* `/data/history` : response time and status of a given `endpoint`
* `/data/history/all` : response time and status of all endpoints
* `/swagger-ui.html` : swagger ui 

## Configuring

### application.yml

Values to set :

`<SLACK_APP_TOKEN>` Your slack application token
`<SLACK_CHANNEL>` Your main Slack channel
`<SLACK_CHANNEL_STATS>` Slack channel for statistics (hourly, daily)
`<SMTP_USERNAME>` Your SMTP service username
`<SMTP_PASSWORD>` Your SMTP service password

Jasypt is used to set a secret encoder key. By default it's `mysecretkey`
`password: ${FIREWATCHKEY:mysecretkey}`

### config.json

This json file defines endpoints to watch, defaults, and mail recipients for alerts.

#### Defaults block

Defines default values for all endpoints.

Each endpoint is tested every `delay` ms.
When requested, endpoint may respond within `timeout` ms.
If `timeout` is reached, it may test another `retry` times, with a delay of `retry_delay` ms. 

Default channel is set to `slack_channel` value. 

```
  "defaults" : {
    "delay" : 120000,                   # default delay between two tests
    "timeout": 1000,                    # default request timeout (ms)
    "retry": 3,                         # default max retries
    "retry_delay" : 1000,               # default delay between two retries
    "slack_channel" : "firewatch"       # default channel
  }
```

#### Recipients block

Defines a list of named mail recipients.

```
  "recipients" :
    [{
        # first recipient is a single email address
        "name":"email1",
        "emails":["email1@domain.net"]
    },{
        # second recipient is a group of email address
        "name":"group1",
        "emails":["email1@domain.net","email2@domain.net"]
  }]
```

#### Endpoints

Defines a list of named endpoints

```
  "elements" : [{
    "name": "my website",                           # endpoint name
    "url": "https://www.mysite.net/",               # base url
    "enabled": false,                               # enabled (optional, default true)
    "delay" : 120000,                               # specific delay (optional, unless use default)
    "timeout": 1000,                                # specific timeout (optional, unless use default)
    "retry": 3,                                     # specific max retries (optional, unless use default)
    "retry_delay" : 1000,                           # specific retry delay (optional, unless use default)
    "params": "/up",                                # add params to url (path, credentials..) (optionnal)
    "content": "find this content",                 # find a content in response body (optionnal)
    "auth_type" : "basic",                          # define authentication (none, basic, bearer, default none)
    "username": "user",                             # define auth basic user (optionnal)
    "password": "pwd",                              # define auth basic password (optionnal)
    "bearer": "when auth_type bearer is used",      # define auth bearer (optionnal)
    "slack_channel" : "channel"                     # specific channel (optionnal)
  }]
```

### Secret encoding

Secrets can be encoded, to avoid a plain text exposure of them.

We use `Jasypt` and by default `PBEWithMD5AndDES` algorithm.

Encoding key is set through `jasypt.encryptor.password` property, which is by default initiated by `FIREWATCHKEY` env var.

Every value in `application*.yml` or `config.json` can be encoded, using `ENC()` :

```
{
    "name": "stackoverflow",
    "url": "https://stackoverflow.com",
    "params": "ENC(Xm75ipSHYyr1F5xP8RuS6g==)",      # encoded version of "/tags"
    "recipients": ["group_a","group_b"]
  }
```

To encode values, use jasypt scripts (http://www.jasypt.org/download.html) :

```
./encrypt.sh input="my-secret-value" password="my-jasypt-key"

----ENVIRONMENT-----------------
Runtime: Oracle Corporation OpenJDK 64-Bit Server VM 14.0.1+7 

----ARGUMENTS-------------------
input: my-secret-value
password: my-jasypt-key

----OUTPUT----------------------
KAZiUtn2BKuVPSydvVpJzqz+5h8ukrni        <<< THIS IS YOUR ENCODED VALUE
```

Remember to set your proper `FIREWATCHKEY` (see below)

You may encode all usernames and password, bearers,...

### Environment vars

`CONFIG_JSON_PATH` must point to the config.json file
`FIREWATCHKEY` must contain encoding key

### Firewatch basic auth

Firewatch API sets by default a basic auth for all API urls.

`firewatch.auth.user.admin.username` : admin username (`admin`, by default)
`firewatch.auth.user.admin.password` : admin password (`password`, by default)

To create a new auth basic password, use `/pwd` firewatch API.

### Local startup

Start the app locally running those commands :

(from project root directory)

```
export CONFIG_JSON_PATH=config/config.jsonexport CONFIG_JSON_PATH=config/config.json
java -jar target/app.jar
```

### Docker environment

First, you need to build Firewatch jar, and build firewatch container :

(from project root directory)

```
mvn install
sudo docker build . -t kprod/firewatch`
```

Edit `docker-compose.yml` and set a user and a password for postgres db :

```
  POSTGRES_USER: firewatch
  POSTGRES_PASSWORD: password
```

Edit `application-docker.yml`, configure it like `application.yml` and set postgres db credentials :

```
    username: <PG_USERNAME>
    password: <PG_PWD>
```

Create a `.env_file` besides `docker-compose.yml` to set env vars :
`FIREWATCHKEY=mysecretkey`

Then, Install docker-compose and run these commands :

```
sudo docker-compose stop
sudo docker build . -t kprod/firewatch
sudo docker-compose up -d
```

Firewatch API is by default available on port `4242`.

