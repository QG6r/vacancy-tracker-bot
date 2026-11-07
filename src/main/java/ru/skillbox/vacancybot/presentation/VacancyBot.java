package ru.skillbox.vacancybot.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;

import java.util.Optional;

/**
 * Точка входа обновлений от Telegram.
 * <p>
 * Сам ничего не решает: разбирает Update в DTO, отдаёт диспетчеру,
 * отправляет полученный ответ. Вся логика — в классах команд.
 */
public class VacancyBot implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(VacancyBot.class);

    private final TelegramClient telegramClient;
    private final UpdateParser updateParser;
    private final CommandDispatcher dispatcher;

    public VacancyBot(TelegramClient telegramClient,
                      UpdateParser updateParser,
                      CommandDispatcher dispatcher) {
        this.telegramClient = telegramClient;
        this.updateParser = updateParser;
        this.dispatcher = dispatcher;
    }

    @Override
    public void consume(Update update) {
        try {
            handle(update);
        } catch (Exception e) {
            // Ошибка на одном сообщении не должна ломать обработку следующих
            log.error("Ошибка при обработке обновления", e);
        }
    }

    private void handle(Update update) {
        Optional<UserRequest> parsed = updateParser.parse(update);
        if (parsed.isEmpty()) {
            return;
        }

        UserRequest request = parsed.get();
        log.info("Запрос от {}: команда={}, аргумент={}",
                request.userId(), request.command(), request.argument());

        // Убираем "часики" на нажатой кнопке, иначе она будет мигать
        if (request.callback()) {
            acknowledgeCallback(update);
        }

        SendMessage response = dispatcher.dispatch(request);
        send(response);
    }

    private void acknowledgeCallback(Update update) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .build());
        } catch (Exception e) {
            log.warn("Не удалось подтвердить нажатие кнопки", e);
        }
    }

    private void send(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Не удалось отправить сообщение в чат {}", message.getChatId(), e);
        }
    }
}
