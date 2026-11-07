package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Смена часового пояса. В отличие от остальных команд, вводится не через
 * слэш, а просто текстом: "UTC+3", "UTC2:00", "UTC+3:30".
 * <p>
 * Поэтому supports() проверяет не имя команды, а совпадение с шаблоном.
 */
public class TimezoneCommand implements BotCommand {

    private static final Pattern PATTERN =
            Pattern.compile("^UTC([+-]?)(\\d{1,2})(?::(\\d{2}))?$", Pattern.CASE_INSENSITIVE);

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    public TimezoneCommand(UserService userService, KeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        // Собираем обратно исходный текст: "UTC+3 30" маловероятно,
        // но "UTC+3" придёт как команда без аргумента
        return !request.callback() && PATTERN.matcher(fullText(request)).matches();
    }

    @Override
    public SendMessage execute(UserRequest request) {
        ZoneOffset offset = parseOffset(fullText(request));
        userService.setZoneOffset(request.userId(), offset);

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Для нотификации будет использован часовой пояс UTC"
                        + (offset.equals(ZoneOffset.UTC) ? "+00:00" : offset.getId()))
                .replyMarkup(keyboardFactory.single(
                        KeyboardFactory.Button.of("Menu", MenuCommand.NAME)))
                .build();
    }

    private String fullText(UserRequest request) {
        return request.hasArgument()
                ? request.command() + request.argument()
                : request.command();
    }

    private ZoneOffset parseOffset(String value) {
        Matcher matcher = PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new InvalidInputException("Неверный формат. Пример: UTC+3 или UTC+3:30");
        }

        int sign = "-".equals(matcher.group(1)) ? -1 : 1;
        int hours = Integer.parseInt(matcher.group(2));
        int minutes = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));

        try {
            return ZoneOffset.ofHoursMinutes(sign * hours, sign * minutes);
        } catch (java.time.DateTimeException e) {
            throw new InvalidInputException(
                    "Часовой пояс должен быть в диапазоне от UTC-18 до UTC+18.");
        }
    }
}
