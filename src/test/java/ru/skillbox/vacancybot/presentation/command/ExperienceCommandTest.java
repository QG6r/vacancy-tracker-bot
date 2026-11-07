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

@DisplayName("Команда /experience")
class ExperienceCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private ExperienceCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        command = new ExperienceCommand(userService, new KeyboardFactory());
    }

    @Test
    @DisplayName("сохраняет опыт")
    void savesExperience() {
        command.execute(request("3"));

        verify(userService).setExperience(USER_ID, 3);
    }

    @Test
    @DisplayName("принимает нулевой опыт")
    void acceptsZero() {
        command.execute(request("0"));

        verify(userService).setExperience(USER_ID, 0);
    }

    @Test
    @DisplayName("сбрасывает фильтр по значению any")
    void resetsExperience() {
        command.execute(request("any"));

        verify(userService).setExperience(USER_ID, null);
    }

    @Test
    @DisplayName("отвергает нечисловое значение")
    void rejectsNonNumeric() {
        assertThatThrownBy(() -> command.execute(request("много")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("отвергает значение вне допустимого диапазона")
    void rejectsOutOfRange() {
        assertThatThrownBy(() -> command.execute(request("100")))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> command.execute(request("-1")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("без аргумента показывает варианты")
    void showsOptions() {
        assertThat(command.execute(request("")).getReplyMarkup()).isNotNull();
    }

    private UserRequest request(String argument) {
        return new UserRequest(USER_ID, CHAT_ID, ExperienceCommand.NAME, argument, 1, false);
    }
}