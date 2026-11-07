package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.util.List;

/**
 * Команда /experience — минимальный требуемый опыт в годах.
 * Без аргумента показывает варианты, с аргументом сохраняет значение.
 */
public class ExperienceCommand implements BotCommand {

    public static final String NAME = "experience";

    /** Значение "не учитывать" — сбрасывает фильтр в null. */
    private static final String ANY = "any";

    private static final int MAX_YEARS = 50;

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    public ExperienceCommand(UserService userService, KeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        return NAME.equals(request.command());
    }

    @Override
    public SendMessage execute(UserRequest request) {
        if (!request.hasArgument()) {
            return showOptions(request);
        }

        if (ANY.equals(request.argument())) {
            userService.setExperience(request.userId(), null);
            return confirm(request, "Опыт работы не будет учитываться при поиске.");
        }

        int years = parseYears(request.argument());
        userService.setExperience(request.userId(), years);
        return confirm(request, "Минимальный опыт: " + years + " лет");
    }

    private SendMessage showOptions(UserRequest request) {
        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Выбирайте минимальный опыт:")
                .replyMarkup(keyboardFactory.singleColumn(List.of(
                        KeyboardFactory.Button.of("Не учитывать", NAME, ANY),
                        KeyboardFactory.Button.of("0 Без опыта", NAME, "0"),
                        KeyboardFactory.Button.of("1 От года", NAME, "1"),
                        KeyboardFactory.Button.of("3 От трех лет", NAME, "3"),
                        KeyboardFactory.Button.of("5 От пяти лет", NAME, "5")
                )))
                .build();
    }

    private SendMessage confirm(UserRequest request, String text) {
        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text(text)
                .replyMarkup(keyboardFactory.single(
                        KeyboardFactory.Button.of("Menu", MenuCommand.NAME)))
                .build();
    }

    private int parseYears(String value) {
        int years;
        try {
            years = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(
                    "Опыт должен быть числом лет. Пример: /experience 2");
        }
        if (years < 0 || years > MAX_YEARS) {
            throw new InvalidInputException(
                    "Опыт должен быть от 0 до " + MAX_YEARS + " лет.");
        }
        return years;
    }
}
