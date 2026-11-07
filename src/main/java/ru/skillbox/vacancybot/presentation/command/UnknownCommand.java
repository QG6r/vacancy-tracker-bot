package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;

/**
 * Обработчик по умолчанию: срабатывает, когда ни одна команда
 * не взялась за запрос.
 * <p>
 * supports() всегда возвращает false — эта команда не участвует
 * в обычном подборе, диспетчер вызывает её напрямую как запасной вариант.
 */
public class UnknownCommand implements BotCommand {

    private final KeyboardFactory keyboardFactory;

    public UnknownCommand(KeyboardFactory keyboardFactory) {
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        return false;
    }

    @Override
    public SendMessage execute(UserRequest request) {
        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Неизвестная команда. Используйте /menu для списка доступных действий.")
                .replyMarkup(keyboardFactory.single(
                        KeyboardFactory.Button.of("Menu", MenuCommand.NAME)))
                .build();
    }
}
