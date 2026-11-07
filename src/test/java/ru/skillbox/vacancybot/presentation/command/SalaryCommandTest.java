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

@DisplayName("Команда минимальной зарплаты")
class SalaryCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private SalaryCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        command = new SalaryCommand(userService, new KeyboardFactory());
    }

    @Test
    @DisplayName("сохраняет корректную зарплату")
    void savesSalary() {
        command.execute(request("75000"));

        verify(userService).setSalary(USER_ID, 75000);
    }

    @Test
    @DisplayName("сбрасывает фильтр по значению any")
    void resetsSalary() {
        command.execute(request("any"));

        verify(userService).setSalary(USER_ID, null);
    }

    @Test
    @DisplayName("отвергает нечисловое значение")
    void rejectsNonNumeric() {
        assertThatThrownBy(() -> command.execute(request("много")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("отвергает отрицательное значение")
    void rejectsNegative() {
        assertThatThrownBy(() -> command.execute(request("-100")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("без аргумента показывает варианты")
    void showsOptions() {
        assertThat(command.execute(request("")).getReplyMarkup()).isNotNull();
    }

    private UserRequest request(String argument) {
        return new UserRequest(USER_ID, CHAT_ID, SalaryCommand.NAME, argument, 1, false);
    }
}
