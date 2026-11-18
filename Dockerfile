# Build stage: compile the bot and create a runnable distribution (scripts + jars)
FROM gradle:8.14-jdk21 AS build
WORKDIR /home/gradle/project
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle installDist --no-daemon -x test

# Runtime stage: JRE only
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/build/install/vacancy-tracker-bot /app/bot

# User data (settings and found vacancies) lives in a volume, logs go to /app/logs
ENV STORAGE_PATH=/app/data
VOLUME ["/app/data"]

CMD ["/app/bot/bin/vacancy-tracker-bot"]
