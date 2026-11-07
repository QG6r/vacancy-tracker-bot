package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.time.LocalTime;
import java.util.List;

/**
 * Команда /notify — время ежедневной нотификации.
 * <p>
 * Принимает время в двух видах: "/notify 7 15" (часы и минуты через пробел,
 * как в ТЗ) и "notify:07:15" — так его передают кнопки.
 */
public class NotifyCommand implements BotCommand {

    public static final String NAME = "notify";

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    public NotifyCommand(UserService userService, KeyboardFactory keyboardFactory) {
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

        LocalTime time = parseTime(request.argument());
        userService.setNotificationTime(request.userId(), time);

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Время нотификации: " + time)
                .replyMarkup(keyboardFactory.single(
                        KeyboardFactory.Button.of("Menu", MenuCommand.NAME)))
                .build();
    }

    private SendMessage showOptions(UserRequest request) {
        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Выбирайте время нотификации, либо введите команду, "
                        + "например /notify 15 30 (разделитель часов и минут - пробел)")
                .replyMarkup(keyboardFactory.singleColumn(List.of(
                        KeyboardFactory.Button.of("12:00", NAME, "12:00"),
                        KeyboardFactory.Button.of("15:00", NAME, "15:00"),
                        KeyboardFactory.Button.of("17:00", NAME, "17:00"),
                        KeyboardFactory.Button.of("19:00", NAME, "19:00"),
                        KeyboardFactory.Button.of("21:00", NAME, "21:00")
                )))
                .build();
    }

    /**
     * Разбирает "7 15" и "07:15" в одно и то же время.
     */
    private LocalTime parseTime(String value) {
        String[] parts = value.trim().split("[\\s:]+");

        if (parts.length != 2) {
            throw new InvalidInputException(
                    "Укажите часы и минуты. Пример: /notify 15 30");
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalTime.of(hour, minute);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(
                    "Часы и минуты должны быть числами. Пример: /notify 15 30");
        } catch (java.time.DateTimeException e) {
            throw new InvalidInputException(
                    "Некорректное время. Часы: 0-23, минуты: 0-59.");
        }
    }
}
