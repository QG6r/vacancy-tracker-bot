package ru.skillbox.vacancybot.presentation;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;

import java.util.Optional;

/**
 * Превращает Update от Telegram в наш DTO.
 * <p>
 * Пользователь может обратиться к боту двумя способами — ввести команду
 * текстом или нажать кнопку. Оба случая приводятся здесь к единому виду,
 * чтобы командам было всё равно, откуда пришёл запрос.
 */
public class UpdateParser {

    public Optional<UserRequest> parse(Update update) {
        if (update.hasCallbackQuery()) {
            return Optional.of(fromCallback(update.getCallbackQuery()));
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            return Optional.of(fromMessage(update.getMessage()));
        }
        return Optional.empty();
    }

    /**
     * Нажатие кнопки. В callbackData мы сами кладём строку вида
     * "region:52" — до двоеточия команда, после аргумент.
     */
    private UserRequest fromCallback(CallbackQuery query) {
        String data = query.getData();
        int separator = data.indexOf(':');

        String command = separator < 0 ? data : data.substring(0, separator);
        String argument = separator < 0 ? "" : data.substring(separator + 1);

        return new UserRequest(
                query.getFrom().getId(),
                query.getMessage().getChatId(),
                command,
                argument,
                query.getMessage().getMessageId(),
                true
        );
    }

    /**
     * Введённое сообщение. Команда — первое слово без слэша,
     * аргумент — всё остальное ("/salary 75000" → command=salary, argument=75000).
     */
    private UserRequest fromMessage(Message message) {
        String text = message.getText().trim();

        String command;
        String argument;

        int space = text.indexOf(' ');
        if (space < 0) {
            command = text;
            argument = "";
        } else {
            command = text.substring(0, space);
            argument = text.substring(space + 1).trim();
        }

        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        return new UserRequest(
                message.getFrom().getId(),
                message.getChatId(),
                command.toLowerCase(),
                argument,
                message.getMessageId(),
                false
        );
    }
}
