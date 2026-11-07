package ru.skillbox.vacancybot.service.startup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.NotificationSettings;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.VacancyService;
import ru.skillbox.vacancybot.service.notification.NotificationSender;
import ru.skillbox.vacancybot.service.scheduler.TaskScheduler;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Восстановление задач при старте")
class ScheduledTaskRestorerTest {

    private UserService userService;
    private TaskScheduler taskScheduler;
    private ScheduledTaskRestorer restorer;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        taskScheduler = mock(TaskScheduler.class);
        restorer = new ScheduledTaskRestorer(
                userService, mock(VacancyService.class), taskScheduler,
                mock(NotificationSender.class));
    }

    @Test
    @DisplayName("восстанавливает задачу активного пользователя")
    void restoresActiveUser() {
        when(userService.findAll()).thenReturn(List.of(user(100L, true, LocalTime.of(9, 0))));

        restorer.restoreAll();

        verify(taskScheduler).schedule(
                eq(100L), eq(LocalTime.of(9, 0)), eq(ZoneOffset.UTC), any(Runnable.class));
    }

    @Test
    @DisplayName("не трогает пользователя без активного поиска")
    void skipsInactiveUser() {
        when(userService.findAll()).thenReturn(List.of(user(100L, false, LocalTime.of(9, 0))));

        restorer.restoreAll();

        verify(taskScheduler, never()).schedule(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("не трогает пользователя без времени нотификации")
    void skipsUserWithoutTime() {
        when(userService.findAll()).thenReturn(List.of(user(100L, true, null)));

        restorer.restoreAll();

        verify(taskScheduler, never()).schedule(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("пустое хранилище не вызывает ошибок")
    void handlesEmptyStorage() {
        when(userService.findAll()).thenReturn(List.of());

        restorer.restoreAll();

        verify(taskScheduler, never()).schedule(anyLong(), any(), any(), any());
    }

    private BotUser user(Long id, boolean searchActive, LocalTime time) {
        return BotUser.builder()
                .id(id)
                .chatId(id)
                .searchActive(searchActive)
                .notificationSettings(NotificationSettings.builder()
                        .notificationTime(time)
                        .zoneOffset(ZoneOffset.UTC)
                        .build())
                .build();
    }
}