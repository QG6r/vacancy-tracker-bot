package ru.skillbox.vacancybot.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.VacancyService;

import java.util.List;

public class NotificationTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(NotificationTask.class);

    private final Long userId;
    private final UserService userService;
    private final VacancyService vacancyService;
    private final NotificationSender notificationSender;

    public NotificationTask(Long userId,
                            UserService userService,
                            VacancyService vacancyService,
                            NotificationSender notificationSender) {
        this.userId = userId;
        this.userService = userService;
        this.vacancyService = vacancyService;
        this.notificationSender = notificationSender;
    }

    @Override
    public void run() {
        log.info("Запуск задачи нотификации для пользователя {}", userId);

        if (!userService.isRegistered(userId)) {
            log.warn("Пользователь {} больше не зарегистрирован, задача пропущена", userId);
            return;
        }

        BotUser user = userService.getUser(userId);

        try {
            List<Vacancy> vacancies = vacancyService.searchAndSave(userId);
            notificationSender.sendVacancies(user.getChatId(), vacancies);

        } catch (Exception e) {
            log.error("Не удалось получить вакансии для пользователя {}", userId, e);
            notificationSender.sendText(user.getChatId(),
                    "Не удалось получить вакансии от агрегатора. Попробуем в следующий раз.");
        }
    }
}