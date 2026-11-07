package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;

/**
 * Общий интерфейс всех команд бота.
 * <p>
 * Каждая команда сама сообщает, на какие сообщения она реагирует
 * (метод supports), и сама формирует ответ (метод execute). Благодаря
 * этому добавление новой команды не требует правок в диспетчере —
 * достаточно написать новый класс и зарегистрировать его.
 */
public interface BotCommand {

    /**
     * Отвечает ли эта команда за данный запрос.
     */
    boolean supports(UserRequest request);

    /**
     * Обрабатывает запрос и возвращает сообщение для пользователя.
     */
    SendMessage execute(UserRequest request);
}
