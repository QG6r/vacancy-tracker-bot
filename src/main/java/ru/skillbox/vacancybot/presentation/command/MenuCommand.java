package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.NotificationSettings;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Команда /menu — главное меню с критериями поиска.
 * <p>
 * В подписи каждой кнопки показывается выбранное значение, например
 * "Регион [52]". Если значение не задано, показывается только название.
 */
public class MenuCommand implements BotCommand {

    public static final String NAME = "menu";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    public MenuCommand(UserService userService, KeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        return NAME.equals(request.command());
    }

    @Override
    public SendMessage execute(UserRequest request) {
        userService.register(request.userId(), request.chatId());
        BotUser user = userService.getUser(request.userId());

        SearchFilter filter = user.getSearchFilter();
        NotificationSettings notification = user.getNotificationSettings();

        String text = """
                Задайте критерии поиска и время нотификации.
                Затем нажмите "Готово", либо введите команду /ready.""";

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text(text)
                .replyMarkup(keyboardFactory.singleColumn(List.of(
                        KeyboardFactory.Button.of(
                                label("Регион", filter.getRegionCode()), RegionCommand.NAME),
                        KeyboardFactory.Button.of(
                                label("Минимальный опыт", filter.getMinExperience()),
                                ExperienceCommand.NAME),
                        KeyboardFactory.Button.of(
                                label("Минимальная зарплата", filter.getMinSalary()),
                                SalaryCommand.NAME),
                        KeyboardFactory.Button.of(
                                label("Слово для поиска", filter.getKeyword()),
                                KeywordCommand.NAME),
                        KeyboardFactory.Button.of(
                                label("Настройки нотификации", formatTime(notification)),
                                NotifyCommand.NAME),
                        KeyboardFactory.Button.of("Готово", ReadyCommand.NAME)
                )))
                .build();
    }

    /** "Регион" без значения, "Регион [52]" — со значением. */
    private String label(String title, Object value) {
        if (value == null || value.toString().isBlank()) {
            return title;
        }
        return title + " [" + value + "]";
    }

    private String formatTime(NotificationSettings settings) {
        return settings.isConfigured()
                ? settings.getNotificationTime().format(TIME_FORMAT)
                : null;
    }
}
