# Firewatch

## Purpose

Firewatch is a light web monitoring service.

Firewatch is able to watch several urls, check content and track performances.

Alerts can be sent though mail system or a slack channel.

Feel free to contribute or fork.

## Startup

### Local

From project root :

```
export CONFIG_JSON_PATH=config/config.json
java -jar target/app.jar
```

### Docker environment

Install docker-compose and run these commands :

```
sudo docker-compose stop
sudo docker build . -t kprod/firewatch
sudo docker-compose up -d
```

## Configure

### application.yml

### config.json