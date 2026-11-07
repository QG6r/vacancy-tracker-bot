package ru.skillbox.vacancybot.service.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.VacancyService;
import ru.skillbox.vacancybot.service.notification.NotificationSender;
import ru.skillbox.vacancybot.service.notification.NotificationTask;
import ru.skillbox.vacancybot.service.scheduler.TaskScheduler;

import java.util.List;

public class ScheduledTaskRestorer {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskRestorer.class);

    private final UserService userService;
    private final VacancyService vacancyService;
    private final TaskScheduler taskScheduler;
    private final NotificationSender notificationSender;

    public ScheduledTaskRestorer(UserService userService,
                                 VacancyService vacancyService,
                                 TaskScheduler taskScheduler,
                                 NotificationSender notificationSender) {
        this.userService = userService;
        this.vacancyService = vacancyService;
        this.taskScheduler = taskScheduler;
        this.notificationSender = notificationSender;
    }

    public void restoreAll() {
        List<BotUser> users = userService.findAll();
        int restored = 0;

        for (BotUser user : users) {
            if (shouldRestore(user)) {
                restore(user);
                restored++;
            }
        }

        log.info("Восстановлено задач: {} из {} пользователей", restored, users.size());
    }

    private boolean shouldRestore(BotUser user) {
        return user.isSearchActive() && user.getNotificationSettings().isConfigured();
    }

    private void restore(BotUser user) {
        taskScheduler.schedule(
                user.getId(),
                user.getNotificationSettings().getNotificationTime(),
                user.getNotificationSettings().getZoneOffset(),
                new NotificationTask(user.getId(), userService, vacancyService, notificationSender));
    }
}