package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.VacancyService;
import ru.skillbox.vacancybot.service.notification.NotificationSender;
import ru.skillbox.vacancybot.service.notification.NotificationTask;
import ru.skillbox.vacancybot.service.scheduler.TaskScheduler;

import java.util.ArrayList;
import java.util.List;

public class ReadyCommand implements BotCommand {

    public static final String NAME = "ready";

    private final UserService userService;
    private final VacancyService vacancyService;
    private final TaskScheduler taskScheduler;
    private final NotificationSender notificationSender;
    private final KeyboardFactory keyboardFactory;

    public ReadyCommand(UserService userService,
                        VacancyService vacancyService,
                        TaskScheduler taskScheduler,
                        NotificationSender notificationSender,
                        KeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.vacancyService = vacancyService;
        this.taskScheduler = taskScheduler;
        this.notificationSender = notificationSender;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        return NAME.equals(request.command());
    }

    @Override
    public SendMessage execute(UserRequest request) {
        BotUser user = userService.getUser(request.userId());

        if (!user.getNotificationSettings().isConfigured()) {
            return SendMessage.builder()
                    .chatId(request.chatId().toString())
                    .text("Не задано время нотификации! Задайте его, нажав кнопку ниже")
                    .replyMarkup(keyboardFactory.single(KeyboardFactory.Button.of(
                            "Настройки нотификации", NotifyCommand.NAME)))
                    .build();
        }

        userService.setSearchActive(request.userId(), true);

        taskScheduler.schedule(
                user.getId(),
                user.getNotificationSettings().getNotificationTime(),
                user.getNotificationSettings().getZoneOffset(),
                new NotificationTask(user.getId(), userService, vacancyService, notificationSender));

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text(buildSummary(user))
                .replyMarkup(keyboardFactory.single(
                        KeyboardFactory.Button.of("Menu", MenuCommand.NAME)))
                .build();
    }

    private String buildSummary(BotUser user) {
        StringBuilder summary = new StringBuilder()
                .append("Запланировано выполнение задачи нотификации о найденных вакансиях на ")
                .append(user.getNotificationSettings().getNotificationTime())
                .append(".\nБудут искаться новые вакансии по следующим параметрам:\n\n");

        SearchFilter filter = user.getSearchFilter();
        List<String> parts = new ArrayList<>();
        if (filter.getRegionCode() != null) {
            parts.add("Код региона: " + filter.getRegionCode());
        }
        if (filter.getMinSalary() != null) {
            parts.add("Минимальная зарплата: " + filter.getMinSalary());
        }
        if (filter.getMinExperience() != null) {
            parts.add("Минимальный опыт: " + filter.getMinExperience() + " лет");
        }
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            parts.add("Ключевые слова: " + filter.getKeyword());
        }

        if (parts.isEmpty()) {
            summary.append("Критерии не заданы — будут выбраны все вакансии во всех регионах России.");
        } else {
            summary.append(String.join("\n", parts));
        }

        return summary.toString();
    }
}