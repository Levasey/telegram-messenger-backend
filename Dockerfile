# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app
EXPOSE 9090

ENV JAVA_OPTS=""

# Прод: задайте SPRING_DATASOURCE_*, TELEGRAM_BOT_TOKEN, при необходимости TELEGRAM_WEBHOOK_SECRET, SERVER_PORT.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
