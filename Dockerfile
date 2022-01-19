FROM navikt/java:17

COPY build/libs/app.jar app.jar

ENV PORT=8080
EXPOSE $PORT