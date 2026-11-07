package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Команда /keyword")
class KeywordCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private KeywordCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        command = new KeywordCommand(userService, new KeywordFactoryStub());
    }

    @Test
    @DisplayName("сохраняет ключевое слово")
    void savesKeyword() {
        command.execute(request("Java-разработчик"));

        verify(userService).setKeyword(USER_ID, "Java-разработчик");
    }

    @Test
    @DisplayName("обрезает лишние пробелы")
    void trimsKeyword() {
        command.execute(request("  Java  "));

        verify(userService).setKeyword(USER_ID, "Java");
    }

    @Test
    @DisplayName("сбрасывает фильтр по значению any")
    void resetsKeyword() {
        command.execute(request("any"));

        verify(userService).setKeyword(USER_ID, null);
    }

    @Test
    @DisplayName("отвергает слишком длинный запрос")
    void rejectsTooLong() {
        assertThatThrownBy(() -> command.execute(request("я".repeat(200))))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("без аргумента показывает варианты")
    void showsOptions() {
        assertThat(command.execute(request("")).getReplyMarkup()).isNotNull();
    }

    private UserRequest request(String argument) {
        return new UserRequest(USER_ID, CHAT_ID, KeywordCommand.NAME, argument, 1, false);
    }

    /** Обычная фабрика — отдельный класс только чтобы не плодить импортов. */
    private static class KeywordFactoryStub extends KeyboardFactory {
    }
}