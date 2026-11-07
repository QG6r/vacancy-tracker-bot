package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.RegionCatalog;
import ru.skillbox.vacancybot.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Команда выбора региона")
class RegionCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private RegionCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        command = new RegionCommand(userService, new RegionCatalog(), new KeyboardFactory());
    }

    @Test
    @DisplayName("сохраняет выбранный регион")
    void savesRegion() {
        SendMessage result = command.execute(request("78"));

        verify(userService).setRegion(USER_ID, 78);
        assertThat(result.getText()).contains("Санкт-Петербург");
    }

    @Test
    @DisplayName("отвергает несуществующий код региона")
    void rejectsUnknownRegion() {
        assertThatThrownBy(() -> command.execute(request("999")))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("отвергает нечисловой код")
    void rejectsNonNumericCode() {
        assertThatThrownBy(() -> command.execute(request("Питер")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("без аргумента показывает список регионов")
    void showsListWithoutArgument() {
        SendMessage result = command.execute(request(""));

        assertThat(result.getText()).isEqualTo("Выбирайте регион:");
        assertThat(result.getReplyMarkup()).isNotNull();
    }

    @Test
    @DisplayName("реагирует на свою команду и игнорирует чужие")
    void supportsOwnCommandOnly() {
        assertThat(command.supports(request(""))).isTrue();
        assertThat(command.supports(
                new UserRequest(USER_ID, CHAT_ID, "salary", "", 1, false))).isFalse();
    }

    private UserRequest request(String argument) {
        return new UserRequest(USER_ID, CHAT_ID, RegionCommand.NAME, argument, 1, false);
    }
}
