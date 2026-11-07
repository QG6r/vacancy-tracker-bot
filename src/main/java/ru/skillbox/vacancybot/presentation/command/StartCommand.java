package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * Команда /start — регистрация пользователя и приветствие.
 * Текст различается для нового и уже известного пользователя.
 */
public class StartCommand implements BotCommand {

    public static final String NAME = "start";

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    public StartCommand(UserService userService, KeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        return NAME.equals(request.command());
    }

    @Override
    public SendMessage execute(UserRequest request) {
        boolean isNew = userService.register(request.userId(), request.chatId());
        BotUser user = userService.getUser(request.userId());

        String text = isNew ? greetNewUser() : greetKnownUser(user);

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text(text)
                .replyMarkup(keyboardFactory.single(
                        KeyboardFactory.Button.of("Menu", MenuCommand.NAME)))
                .build();
    }

    private String greetNewUser() {
        return """
                Вы успешно зарегистрированы в Vacancy Tracker Bot!
                Будет использован часовой пояс UTC, для изменения введите часовой пояс в формате UTC+3.
                Используйте команду /menu для получения списка команд.""";
    }

    private String greetKnownUser(BotUser user) {
        return "Vacancy Tracker Bot приветствует вас!\n"
                + "Вы ранее регистрировались в нашем боте, можете приступать к его использованию.\n"
                + "Сейчас используется часовой пояс "
                + user.getNotificationSettings().getZoneOffset()
                + ", для изменения введите, например UTC+3:30\n\n"
                + "Ранее заданные фильтры:\n"
                + describeFilters(user.getSearchFilter())
                + "\n\nВведите команду /menu для получения списка команд.";
    }

    private String describeFilters(SearchFilter filter) {
        if (filter.isEmpty()) {
            return "Фильтры не заданы";
        }

        List<String> parts = new ArrayList<>();
        if (filter.getRegionCode() != null) {
            parts.add("Код региона: " + filter.getRegionCode());
        }
        if (filter.getMinExperience() != null) {
            parts.add("Минимальный опыт: " + filter.getMinExperience() + " лет");
        }
        if (filter.getMinSalary() != null) {
            parts.add("Минимальная зарплата: " + filter.getMinSalary());
        }
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            parts.add("Ключевые слова: " + filter.getKeyword());
        }
        return String.join("\n", parts);
    }
}
