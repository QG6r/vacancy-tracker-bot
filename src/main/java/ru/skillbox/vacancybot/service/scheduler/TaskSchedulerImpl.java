package ru.skillbox.vacancybot.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TaskSchedulerImpl implements TaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerImpl.class);

    private static final long PERIOD_HOURS = 24;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final ScheduledExecutorService executor;
    private final NotificationTimeCalculator timeCalculator;

    private final Map<Long, List<ScheduledFuture<?>>> tasks = new ConcurrentHashMap<>();

    public TaskSchedulerImpl(int poolSize, NotificationTimeCalculator timeCalculator) {
        this.executor = Executors.newScheduledThreadPool(poolSize);
        this.timeCalculator = timeCalculator;
    }

    TaskSchedulerImpl(ScheduledExecutorService executor, NotificationTimeCalculator timeCalculator) {
        this.executor = executor;
        this.timeCalculator = timeCalculator;
    }

    @Override
    public void schedule(Long userId, LocalTime notificationTime, ZoneOffset offset, Runnable task) {
        cancel(userId);

        Duration delay = timeCalculator.delayUntilNext(notificationTime, offset);

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(
                wrap(userId, task),
                delay.toSeconds(),
                TimeUnit.HOURS.toSeconds(PERIOD_HOURS),
                TimeUnit.SECONDS);

        tasks.computeIfAbsent(userId, key -> new ArrayList<>()).add(future);

        log.info("Пользователь {}: задача запланирована на {} ({}), первый запуск через {}",
                userId, notificationTime, offset, delay);
    }

    @Override
    public void cancel(Long userId) {
        List<ScheduledFuture<?>> userTasks = tasks.remove(userId);
        if (userTasks == null) {
            return;
        }

        for (ScheduledFuture<?> future : userTasks) {
            future.cancel(false);
        }
        log.info("Пользователь {}: отменено задач — {}", userId, userTasks.size());
    }

    @Override
    public boolean hasTasks(Long userId) {
        List<ScheduledFuture<?>> userTasks = tasks.get(userId);
        return userTasks != null && !userTasks.isEmpty();
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        tasks.clear();
        log.info("Планировщик остановлен");
    }

    private Runnable wrap(Long userId, Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Ошибка при выполнении задачи пользователя {}", userId, e);
            }
        };
    }
}