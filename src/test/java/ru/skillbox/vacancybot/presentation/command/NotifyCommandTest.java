package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Команда /notify")
class NotifyCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private NotifyCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        command = new NotifyCommand(userService, new KeyboardFactory());
    }

    @Test
    @DisplayName("принимает время через пробел")
    void parsesSpaceSeparatedTime() {
        command.execute(request("7 15"));

        verify(userService).setNotificationTime(USER_ID, LocalTime.of(7, 15));
    }

    @Test
    @DisplayName("принимает время через двоеточие")
    void parsesColonSeparatedTime() {
        command.execute(request("12:00"));

        verify(userService).setNotificationTime(USER_ID, LocalTime.of(12, 0));
    }

    @Test
    @DisplayName("отвергает неполное время")
    void rejectsIncompleteTime() {
        assertThatThrownBy(() -> command.execute(request("15")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("отвергает нечисловое время")
    void rejectsNonNumericTime() {
        assertThatThrownBy(() -> command.execute(request("утром рано")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("отвергает несуществующее время")
    void rejectsInvalidTime() {
        assertThatThrownBy(() -> command.execute(request("25 00")))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> command.execute(request("12 99")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("без аргумента показывает варианты времени")
    void showsOptions() {
        assertThat(command.execute(request("")).getReplyMarkup()).isNotNull();
    }

    private UserRequest request(String argument) {
        return new UserRequest(USER_ID, CHAT_ID, NotifyCommand.NAME, argument, 1, false);
    }
}