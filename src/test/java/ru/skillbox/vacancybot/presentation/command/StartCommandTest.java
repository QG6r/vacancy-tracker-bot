package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Команда /start")
class StartCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private StartCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        command = new StartCommand(userService, new KeyboardFactory());
    }

    @Test
    @DisplayName("регистрирует пользователя")
    void registersUser() {
        givenUser(true, new SearchFilter());

        command.execute(request());

        verify(userService).register(USER_ID, CHAT_ID);
    }

    @Test
    @DisplayName("новому пользователю показывает приветствие о регистрации")
    void greetsNewUser() {
        givenUser(true, new SearchFilter());

        SendMessage result = command.execute(request());

        assertThat(result.getText()).contains("Вы успешно зарегистрированы");
    }

    @Test
    @DisplayName("известному пользователю показывает другое приветствие")
    void greetsKnownUser() {
        givenUser(false, new SearchFilter());

        SendMessage result = command.execute(request());

        assertThat(result.getText()).contains("Вы ранее регистрировались");
        assertThat(result.getText()).contains("Фильтры не заданы");
    }

    @Test
    @DisplayName("перечисляет ранее заданные фильтры")
    void listsExistingFilters() {
        givenUser(false, SearchFilter.builder()
                .regionCode(78)
                .minExperience(3)
                .minSalary(90000)
                .keyword("Java")
                .build());

        SendMessage result = command.execute(request());

        assertThat(result.getText()).contains("Код региона: 78");
        assertThat(result.getText()).contains("Минимальный опыт: 3");
        assertThat(result.getText()).contains("Минимальная зарплата: 90000");
        assertThat(result.getText()).contains("Ключевые слова: Java");
    }

    @Test
    @DisplayName("реагирует только на свою команду")
    void supportsOwnCommandOnly() {
        assertThat(command.supports(request())).isTrue();
        assertThat(command.supports(
                new UserRequest(USER_ID, CHAT_ID, "menu", "", 1, false))).isFalse();
    }

    private void givenUser(boolean isNew, SearchFilter filter) {
        when(userService.register(USER_ID, CHAT_ID)).thenReturn(isNew);
        when(userService.getUser(USER_ID)).thenReturn(BotUser.builder()
                .id(USER_ID)
                .chatId(CHAT_ID)
                .searchFilter(filter)
                .build());
    }

    private UserRequest request() {
        return new UserRequest(USER_ID, CHAT_ID, StartCommand.NAME, "", 1, false);
    }
}