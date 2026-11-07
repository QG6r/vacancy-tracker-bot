package ru.skillbox.vacancybot.service.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.exception.ApiException;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.VacancyService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Задача нотификации")
class NotificationTaskTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private VacancyService vacancyService;
    private NotificationSender notificationSender;
    private NotificationTask task;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        vacancyService = mock(VacancyService.class);
        notificationSender = mock(NotificationSender.class);

        when(userService.isRegistered(USER_ID)).thenReturn(true);
        when(userService.getUser(USER_ID)).thenReturn(
                BotUser.builder().id(USER_ID).chatId(CHAT_ID).build());

        task = new NotificationTask(USER_ID, userService, vacancyService, notificationSender);
    }

    @Test
    @DisplayName("ищет вакансии и отправляет их пользователю")
    void searchesAndSends() {
        List<Vacancy> vacancies = List.of(Vacancy.builder().id("1").build());
        when(vacancyService.searchAndSave(USER_ID)).thenReturn(vacancies);

        task.run();

        verify(vacancyService).searchAndSave(USER_ID);
        verify(notificationSender).sendVacancies(CHAT_ID, vacancies);
    }

    @Test
    @DisplayName("пустой результат тоже отправляется")
    void sendsEmptyResult() {
        when(vacancyService.searchAndSave(USER_ID)).thenReturn(List.of());

        task.run();

        verify(notificationSender).sendVacancies(CHAT_ID, List.of());
    }

    @Test
    @DisplayName("удалённому пользователю ничего не отправляется")
    void skipsDeletedUser() {
        when(userService.isRegistered(USER_ID)).thenReturn(false);

        task.run();

        verify(vacancyService, never()).searchAndSave(anyLong());
        verify(notificationSender, never()).sendVacancies(anyLong(), any());
    }

    @Test
    @DisplayName("при ошибке API пользователь получает предупреждение")
    void notifiesOnApiFailure() {
        when(vacancyService.searchAndSave(USER_ID))
                .thenThrow(new ApiException("агрегатор недоступен"));

        task.run();

        verify(notificationSender).sendText(eq(CHAT_ID), anyString());
        verify(notificationSender, never()).sendVacancies(anyLong(), any());
    }
}