package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.util.List;

/**
 * Команда /keyword — ключевые слова для поиска вакансий.
 */
public class KeywordCommand implements BotCommand {

    public static final String NAME = "keyword";

    private static final String ANY = "any";
    private static final int MAX_LENGTH = 100;

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    public KeywordCommand(UserService userService, KeyboardFactory keyboardFactory) {
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
            userService.setKeyword(request.userId(), null);
            return confirm(request, "Ключевые слова не будут учитываться при поиске.");
        }

        String keyword = validate(request.argument());
        userService.setKeyword(request.userId(), keyword);
        return confirm(request, "Ключевые слова: " + keyword);
    }

    private SendMessage showOptions(UserRequest request) {
        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Выбирайте ключевое слово из предложенных вариантов "
                        + "или введите команду, например /keyword программист kotlin")
                .replyMarkup(keyboardFactory.singleColumn(List.of(
                        KeyboardFactory.Button.of("Не учитывать", NAME, ANY),
                        KeyboardFactory.Button.of("Java", NAME, "Java"),
                        KeyboardFactory.Button.of("Инженер-программист", NAME, "Инженер-программист"),
                        KeyboardFactory.Button.of("Стажировка Java", NAME, "Стажировка Java"),
                        KeyboardFactory.Button.of("Программист-стажер", NAME, "Программист-стажер"),
                        KeyboardFactory.Button.of("Developer", NAME, "Developer"),
                        KeyboardFactory.Button.of("Software engineer", NAME, "Software engineer")
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

    private String validate(String value) {
        String keyword = value.trim();
        if (keyword.length() > MAX_LENGTH) {
            throw new InvalidInputException(
                    "Слишком длинный запрос. Максимум " + MAX_LENGTH + " символов.");
        }
        return keyword;
    }
}
