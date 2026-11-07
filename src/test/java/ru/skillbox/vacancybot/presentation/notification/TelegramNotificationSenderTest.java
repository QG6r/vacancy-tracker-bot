package ru.skillbox.vacancybot.presentation.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.presentation.formatter.VacancyFormatter;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.notification.NotificationSender;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Отправка уведомлений в Telegram")
class TelegramNotificationSenderTest {

    private static final Long CHAT_ID = 200L;

    private TelegramClient telegramClient;
    private NotificationSender sender;

    @BeforeEach
    void setUp() {
        telegramClient = mock(TelegramClient.class);
        sender = new TelegramNotificationSender(
                telegramClient, new VacancyFormatter(), new KeyboardFactory());
    }

    @Test
    @DisplayName("отправляет вакансии в нужный чат")
    void sendsVacancies() throws Exception {
        sender.sendVacancies(CHAT_ID, vacancies(3));

        SendMessage sent = captureSent();
        assertThat(sent.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(sent.getText()).contains("Компания-1");
        assertThat(sent.getParseMode()).isEqualTo("HTML");
    }

    @Test
    @DisplayName("при коротком списке кнопки листания нет")
    void noNavigationForShortList() throws Exception {
        sender.sendVacancies(CHAT_ID, vacancies(5));

        assertThat(captureSent().getReplyMarkup()).isNull();
    }

    @Test
    @DisplayName("при длинном списке добавляет кнопку листания")
    void addsNavigationForLongList() throws Exception {
        sender.sendVacancies(CHAT_ID, vacancies(25));

        assertThat(captureSent().getReplyMarkup()).isNotNull();
    }

    @Test
    @DisplayName("отправляет произвольный текст")
    void sendsText() throws Exception {
        sender.sendText(CHAT_ID, "Агрегатор недоступен");

        assertThat(captureSent().getText()).isEqualTo("Агрегатор недоступен");
    }

    @Test
    @DisplayName("сбой отправки не выбрасывает исключение наружу")
    void swallowsSendFailure() throws Exception {
        when(telegramClient.execute(any(SendMessage.class)))
                .thenThrow(new RuntimeException("сеть недоступна"));

        assertThatCode(() -> sender.sendText(CHAT_ID, "текст")).doesNotThrowAnyException();
    }

    private SendMessage captureSent() throws Exception {
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());
        return captor.getValue();
    }

    private List<Vacancy> vacancies(int count) {
        List<Vacancy> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            result.add(Vacancy.builder().id(String.valueOf(i)).companyName("Компания-" + i).build());
        }
        return result;
    }
}