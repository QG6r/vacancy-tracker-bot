package ru.skillbox.vacancybot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.skillbox.vacancybot.config.BotConfig;
import ru.skillbox.vacancybot.presentation.CommandDispatcher;
import ru.skillbox.vacancybot.presentation.UpdateParser;
import ru.skillbox.vacancybot.presentation.VacancyBot;
import ru.skillbox.vacancybot.presentation.command.BotCommand;
import ru.skillbox.vacancybot.presentation.command.ExperienceCommand;
import ru.skillbox.vacancybot.presentation.command.KeywordCommand;
import ru.skillbox.vacancybot.presentation.command.MenuCommand;
import ru.skillbox.vacancybot.presentation.command.NotifyCommand;
import ru.skillbox.vacancybot.presentation.command.ReadyCommand;
import ru.skillbox.vacancybot.presentation.command.RegionCommand;
import ru.skillbox.vacancybot.presentation.command.SalaryCommand;
import ru.skillbox.vacancybot.presentation.command.StartCommand;
import ru.skillbox.vacancybot.presentation.command.StopCommand;
import ru.skillbox.vacancybot.presentation.command.TimezoneCommand;
import ru.skillbox.vacancybot.presentation.command.UnknownCommand;
import ru.skillbox.vacancybot.presentation.command.VacanciesCommand;
import ru.skillbox.vacancybot.presentation.formatter.VacancyFormatter;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.presentation.notification.TelegramNotificationSender;
import ru.skillbox.vacancybot.repository.JsonUserRepository;
import ru.skillbox.vacancybot.repository.JsonVacancyRepository;
import ru.skillbox.vacancybot.repository.UserRepository;
import ru.skillbox.vacancybot.repository.VacancyRepository;
import ru.skillbox.vacancybot.service.RegionCatalog;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.UserServiceImpl;
import ru.skillbox.vacancybot.service.VacancyService;
import ru.skillbox.vacancybot.service.VacancyServiceImpl;
import ru.skillbox.vacancybot.service.client.TrudvsemApiClient;
import ru.skillbox.vacancybot.service.client.VacancyApiClient;
import ru.skillbox.vacancybot.service.notification.NotificationSender;
import ru.skillbox.vacancybot.service.scheduler.NotificationTimeCalculator;
import ru.skillbox.vacancybot.service.scheduler.TaskScheduler;
import ru.skillbox.vacancybot.service.scheduler.TaskSchedulerImpl;
import ru.skillbox.vacancybot.service.startup.ScheduledTaskRestorer;

import java.util.List;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final int SCHEDULER_POOL_SIZE = 4;

    public static void main(String[] args) {
        try {
            run();
        } catch (Exception e) {
            log.error("Не удалось запустить приложение", e);
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        BotConfig config = BotConfig.load();

        TelegramClient telegramClient = new OkHttpTelegramClient(config.getBotToken());

        UserRepository userRepository = new JsonUserRepository(config.getStoragePath());
        VacancyRepository vacancyRepository = new JsonVacancyRepository(config.getStoragePath());

        UserService userService = new UserServiceImpl(userRepository);
        RegionCatalog regionCatalog = new RegionCatalog();
        VacancyApiClient apiClient = new TrudvsemApiClient();
        VacancyService vacancyService =
                new VacancyServiceImpl(apiClient, vacancyRepository, userService);
        TaskScheduler taskScheduler =
                new TaskSchedulerImpl(SCHEDULER_POOL_SIZE, new NotificationTimeCalculator());

        KeyboardFactory keyboardFactory = new KeyboardFactory();
        VacancyFormatter vacancyFormatter = new VacancyFormatter();
        UpdateParser updateParser = new UpdateParser();
        NotificationSender notificationSender =
                new TelegramNotificationSender(telegramClient, vacancyFormatter, keyboardFactory);

        List<BotCommand> commands = List.of(
                new StartCommand(userService, keyboardFactory),
                new MenuCommand(userService, keyboardFactory),
                new RegionCommand(userService, regionCatalog, keyboardFactory),
                new ExperienceCommand(userService, keyboardFactory),
                new SalaryCommand(userService, keyboardFactory),
                new KeywordCommand(userService, keyboardFactory),
                new NotifyCommand(userService, keyboardFactory),
                new ReadyCommand(userService, vacancyService, taskScheduler,
                        notificationSender, keyboardFactory),
                new VacanciesCommand(vacancyService, vacancyFormatter, keyboardFactory),
                new StopCommand(userService, vacancyService, taskScheduler, keyboardFactory),
                new TimezoneCommand(userService, keyboardFactory)
        );
        CommandDispatcher dispatcher =
                new CommandDispatcher(commands, new UnknownCommand(keyboardFactory));

        VacancyBot bot = new VacancyBot(telegramClient, updateParser, dispatcher);

        new ScheduledTaskRestorer(userService, vacancyService, taskScheduler, notificationSender)
                .restoreAll();

        TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(config.getBotToken(), bot);

        log.info("Бот {} запущен", config.getBotName());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Остановка бота...");
            try {
                taskScheduler.shutdown();
                botsApplication.stop();
            } catch (Exception e) {
                log.error("Ошибка при остановке", e);
            }
        }));

        Thread.currentThread().join();
    }
}