package ru.skillbox.vacancybot.service.notification;

import ru.skillbox.vacancybot.model.Vacancy;

import java.util.List;

public interface NotificationSender {

    void sendVacancies(Long chatId, List<Vacancy> vacancies);

    void sendText(Long chatId, String text);
}