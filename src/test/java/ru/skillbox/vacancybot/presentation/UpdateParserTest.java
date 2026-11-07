package ru.skillbox.vacancybot.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Разбор обновлений от Telegram")
class UpdateParserTest {

    private final UpdateParser parser = new UpdateParser();

    @Test
    @DisplayName("команда без аргумента")
    void parsesCommandWithoutArgument() {
        Optional<UserRequest> result = parser.parse(messageUpdate("/start"));

        assertThat(result).isPresent();
        assertThat(result.get().command()).isEqualTo("start");
        assertThat(result.get().hasArgument()).isFalse();
        assertThat(result.get().callback()).isFalse();
    }

    @Test
    @DisplayName("команда с аргументом")
    void parsesCommandWithArgument() {
        UserRequest request = parser.parse(messageUpdate("/region 52")).orElseThrow();

        assertThat(request.command()).isEqualTo("region");
        assertThat(request.argument()).isEqualTo("52");
    }

    @Test
    @DisplayName("аргумент из нескольких слов сохраняется целиком")
    void parsesMultiWordArgument() {
        UserRequest request = parser.parse(messageUpdate("/keyword бизнес аналитик")).orElseThrow();

        assertThat(request.command()).isEqualTo("keyword");
        assertThat(request.argument()).isEqualTo("бизнес аналитик");
    }

    @Test
    @DisplayName("нажатие кнопки разбирается как команда с аргументом")
    void parsesCallback() {
        UserRequest request = parser.parse(callbackUpdate("region:52")).orElseThrow();

        assertThat(request.command()).isEqualTo("region");
        assertThat(request.argument()).isEqualTo("52");
        assertThat(request.callback()).isTrue();
    }

    @Test
    @DisplayName("нажатие кнопки без аргумента")
    void parsesCallbackWithoutArgument() {
        UserRequest request = parser.parse(callbackUpdate("menu")).orElseThrow();

        assertThat(request.command()).isEqualTo("menu");
        assertThat(request.hasArgument()).isFalse();
    }

    @Test
    @DisplayName("обновление без текста игнорируется")
    void ignoresEmptyUpdate() {
        assertThat(parser.parse(new Update())).isEmpty();
    }

    private Update messageUpdate(String text) {
        Message message = new Message();
        message.setText(text);
        message.setFrom(testUser());
        message.setMessageId(1);
        message.setChat(testChat());

        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    private Update callbackUpdate(String data) {
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(testChat());

        CallbackQuery query = new CallbackQuery();
        query.setData(data);
        query.setFrom(testUser());
        query.setMessage(message);

        Update update = new Update();
        update.setCallbackQuery(query);
        return update;
    }

    private User testUser() {
        return User.builder()
                .id(100L)
                .isBot(false)
                .firstName("Test")
                .build();
    }

    private Chat testChat() {
        return Chat.builder()
                .id(200L)
                .type("private")
                .build();
    }
}
