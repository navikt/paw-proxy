FROM ghcr.io/navikt/baseimages/temurin:17

COPY /target/app-jar-with-dependencies.jar app.jar

ENV PORT=8080
EXPOSE $PORT