package ru.skillbox.vacancybot.service.scheduler;

import java.time.LocalTime;
import java.time.ZoneOffset;

public interface TaskScheduler {

    void schedule(Long userId, LocalTime notificationTime, ZoneOffset offset, Runnable task);

    void cancel(Long userId);

    boolean hasTasks(Long userId);

    void shutdown();
}