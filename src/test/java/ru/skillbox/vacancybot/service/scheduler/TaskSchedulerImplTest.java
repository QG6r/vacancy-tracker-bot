package ru.skillbox.vacancybot.service.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Планировщик задач")
class TaskSchedulerImplTest {

    private static final Long USER_ID = 100L;
    private static final Long OTHER_USER_ID = 200L;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> future;
    private TaskScheduler scheduler;

    @BeforeEach
    void setUp() {
        executor = mock(ScheduledExecutorService.class);
        future = mock(ScheduledFuture.class);

        when(executor.scheduleAtFixedRate(any(), anyLong(), anyLong(), any()))
                .thenAnswer(invocation -> future);

        NotificationTimeCalculator calculator = new NotificationTimeCalculator(
                Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC));

        scheduler = new TaskSchedulerImpl(executor, calculator);
    }

    @Test
    @DisplayName("ставит задачу с периодом в сутки")
    void schedulesWithDailyPeriod() {
        scheduler.schedule(USER_ID, LocalTime.of(15, 0), ZoneOffset.UTC, () -> { });

        verify(executor).scheduleAtFixedRate(
                any(),
                eq(TimeUnit.HOURS.toSeconds(5)),
                eq(TimeUnit.HOURS.toSeconds(24)),
                eq(TimeUnit.SECONDS));
        assertThat(scheduler.hasTasks(USER_ID)).isTrue();
    }

    @Test
    @DisplayName("повторная постановка отменяет старую задачу")
    void cancelsPreviousTaskOnReschedule() {
        scheduler.schedule(USER_ID, LocalTime.of(15, 0), ZoneOffset.UTC, () -> { });
        scheduler.schedule(USER_ID, LocalTime.of(18, 0), ZoneOffset.UTC, () -> { });

        verify(future).cancel(false);
        verify(executor, times(2)).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("отмена убирает задачи пользователя")
    void cancelsUserTasks() {
        scheduler.schedule(USER_ID, LocalTime.of(15, 0), ZoneOffset.UTC, () -> { });

        scheduler.cancel(USER_ID);

        verify(future).cancel(false);
        assertThat(scheduler.hasTasks(USER_ID)).isFalse();
    }

    @Test
    @DisplayName("отмена не трогает задачи других пользователей")
    void doesNotAffectOtherUsers() {
        scheduler.schedule(USER_ID, LocalTime.of(15, 0), ZoneOffset.UTC, () -> { });
        scheduler.schedule(OTHER_USER_ID, LocalTime.of(16, 0), ZoneOffset.UTC, () -> { });

        scheduler.cancel(USER_ID);

        assertThat(scheduler.hasTasks(USER_ID)).isFalse();
        assertThat(scheduler.hasTasks(OTHER_USER_ID)).isTrue();
    }

    @Test
    @DisplayName("отмена несуществующей задачи не падает")
    void cancelUnknownUserDoesNotFail() {
        scheduler.cancel(999L);

        assertThat(scheduler.hasTasks(999L)).isFalse();
    }

    @Test
    @DisplayName("исключение в задаче не отменяет расписание")
    void swallowsTaskException() {
        scheduler.schedule(USER_ID, LocalTime.of(15, 0), ZoneOffset.UTC, () -> {
            throw new IllegalStateException("сбой сети");
        });

        org.mockito.ArgumentCaptor<Runnable> captor =
                org.mockito.ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleAtFixedRate(captor.capture(), anyLong(), anyLong(), any());

        captor.getValue().run();
    }

    @Test
    @DisplayName("shutdown останавливает executor")
    void shutdownStopsExecutor() throws InterruptedException {
        when(executor.awaitTermination(anyLong(), any())).thenReturn(true);

        scheduler.shutdown();

        verify(executor).shutdown();
    }
}