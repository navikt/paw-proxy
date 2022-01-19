FROM navikt/java:17

COPY build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE $PORT