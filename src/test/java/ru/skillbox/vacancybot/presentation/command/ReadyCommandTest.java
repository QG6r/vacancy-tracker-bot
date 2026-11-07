package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.NotificationSettings;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.VacancyService;
import ru.skillbox.vacancybot.service.notification.NotificationSender;
import ru.skillbox.vacancybot.service.scheduler.TaskScheduler;

import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Команда /ready")
class ReadyCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private TaskScheduler taskScheduler;
    private ReadyCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        taskScheduler = mock(TaskScheduler.class);
        command = new ReadyCommand(userService, mock(VacancyService.class), taskScheduler,
                mock(NotificationSender.class), new KeyboardFactory());
    }

    @Test
    @DisplayName("без времени нотификации задача не ставится")
    void requiresNotificationTime() {
        givenUser(null, new SearchFilter());

        SendMessage result = command.execute(request());

        assertThat(result.getText()).contains("Не задано время нотификации");
        verify(taskScheduler, never()).schedule(anyLong(), any(), any(), any());
        verify(userService, never()).setSearchActive(anyLong(), eq(true));
    }

    @Test
    @DisplayName("со временем нотификации ставит задачу в планировщик")
    void schedulesTask() {
        givenUser(LocalTime.of(7, 15), new SearchFilter());

        command.execute(request());

        verify(taskScheduler).schedule(
                eq(USER_ID), eq(LocalTime.of(7, 15)), eq(ZoneOffset.UTC), any(Runnable.class));
        verify(userService).setSearchActive(USER_ID, true);
    }

    @Test
    @DisplayName("выводит сводку заданных параметров")
    void showsFilterSummary() {
        givenUser(LocalTime.of(7, 15), SearchFilter.builder()
                .regionCode(52)
                .minSalary(75000)
                .minExperience(2)
                .keyword("бизнес-аналитик")
                .build());

        SendMessage result = command.execute(request());

        assertThat(result.getText()).contains("Код региона: 52");
        assertThat(result.getText()).contains("Минимальная зарплата: 75000");
        assertThat(result.getText()).contains("Минимальный опыт: 2");
        assertThat(result.getText()).contains("Ключевые слова: бизнес-аналитик");
    }

    @Test
    @DisplayName("без критериев сообщает, что искать будет по всей России")
    void reportsEmptyFilters() {
        givenUser(LocalTime.of(7, 15), new SearchFilter());

        assertThat(command.execute(request()).getText())
                .contains("все вакансии во всех регионах России");
    }

    private void givenUser(LocalTime notificationTime, SearchFilter filter) {
        when(userService.getUser(USER_ID)).thenReturn(BotUser.builder()
                .id(USER_ID)
                .chatId(CHAT_ID)
                .searchFilter(filter)
                .notificationSettings(NotificationSettings.builder()
                        .notificationTime(notificationTime)
                        .zoneOffset(ZoneOffset.UTC)
                        .build())
                .build());
    }

    private UserRequest request() {
        return new UserRequest(USER_ID, CHAT_ID, ReadyCommand.NAME, "", 1, false);
    }
}