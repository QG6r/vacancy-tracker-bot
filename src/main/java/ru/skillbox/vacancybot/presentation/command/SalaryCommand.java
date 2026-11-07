package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.util.List;

/**
 * Команда /salary — минимальная зарплата.
 * <p>
 * API агрегатора не умеет фильтровать по зарплате, поэтому значение
 * сохраняется у нас и применяется при обработке полученных вакансий.
 */
public class SalaryCommand implements BotCommand {

    public static final String NAME = "salary";

    private static final String ANY = "any";
    private static final int MAX_SALARY = 10_000_000;

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    public SalaryCommand(UserService userService, KeyboardFactory keyboardFactory) {
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
            userService.setSalary(request.userId(), null);
            return confirm(request, "Зарплата не будет учитываться при поиске.");
        }

        int salary = parseSalary(request.argument());
        userService.setSalary(request.userId(), salary);
        return confirm(request, "Минимальная зарплата: " + salary + " руб.");
    }

    private SendMessage showOptions(UserRequest request) {
        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Выбирайте желаемую минимальную зарплату из предложенных вариантов "
                        + "или введите команду, например /salary 10000")
                .replyMarkup(keyboardFactory.singleColumn(List.of(
                        KeyboardFactory.Button.of("Не учитывать", NAME, ANY),
                        KeyboardFactory.Button.of("25000 От 25 тыс. руб", NAME, "25000"),
                        KeyboardFactory.Button.of("50000 От 50 тыс. руб", NAME, "50000"),
                        KeyboardFactory.Button.of("100000 От 100 тыс. руб.", NAME, "100000"),
                        KeyboardFactory.Button.of("200000 От 200 тыс. руб.", NAME, "200000")
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

    private int parseSalary(String value) {
        int salary;
        try {
            salary = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(
                    "Зарплата должна быть числом. Пример: /salary 75000");
        }
        if (salary < 0 || salary > MAX_SALARY) {
            throw new InvalidInputException(
                    "Зарплата должна быть от 0 до " + MAX_SALARY + " руб.");
        }
        return salary;
    }
}
