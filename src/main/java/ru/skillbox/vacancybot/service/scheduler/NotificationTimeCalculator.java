package ru.skillbox.vacancybot.service.scheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class NotificationTimeCalculator {

    private final Clock clock;

    public NotificationTimeCalculator() {
        this(Clock.systemUTC());
    }

    public NotificationTimeCalculator(Clock clock) {
        this.clock = clock;
    }

    public Duration delayUntilNext(LocalTime notificationTime, ZoneOffset offset) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(offset);
        ZonedDateTime next = now.with(notificationTime);

        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }

        return Duration.between(now, next);
    }
}