package ru.skillbox.vacancybot.presentation.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.presentation.command.VacanciesCommand;
import ru.skillbox.vacancybot.presentation.formatter.VacancyFormatter;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.notification.NotificationSender;

import java.util.ArrayList;
import java.util.List;

public class TelegramNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationSender.class);

    private final TelegramClient telegramClient;
    private final VacancyFormatter formatter;
    private final KeyboardFactory keyboardFactory;

    public TelegramNotificationSender(TelegramClient telegramClient,
                                      VacancyFormatter formatter,
                                      KeyboardFactory keyboardFactory) {
        this.telegramClient = telegramClient;
        this.formatter = formatter;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public void sendVacancies(Long chatId, List<Vacancy> vacancies) {
        SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                .chatId(chatId.toString())
                .parseMode("HTML")
                .text(formatter.formatPage(vacancies, 0));

        if (vacancies.size() > VacancyFormatter.PAGE_SIZE) {
            List<KeyboardFactory.Button> navigation = new ArrayList<>();
            navigation.add(KeyboardFactory.Button.of("Следующие 10",
                    VacanciesCommand.NAME, "page:" + VacancyFormatter.PAGE_SIZE));
            builder.replyMarkup(keyboardFactory.singleRow(navigation));
        }

        send(builder.build());
    }

    @Override
    public void sendText(Long chatId, String text) {
        send(SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build());
    }

    private void send(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Не удалось отправить уведомление в чат {}", message.getChatId(), e);
        }
    }
}