FROM openjdk:14-jdk-alpine
COPY target/app.jar /
COPY src/main/resources/config.json /
ENV config.path /config.json
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]