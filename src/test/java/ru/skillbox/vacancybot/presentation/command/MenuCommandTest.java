package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.NotificationSettings;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Команда /menu")
class MenuCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private MenuCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        command = new MenuCommand(userService, new KeyboardFactory());
    }

    @Test
    @DisplayName("без заданных фильтров показывает кнопки без значений")
    void showsPlainLabels() {
        givenUser(new SearchFilter(), new NotificationSettings());

        assertThat(buttonTexts()).contains("Регион", "Минимальный опыт", "Готово");
    }

    @Test
    @DisplayName("показывает выбранные значения в подписях кнопок")
    void showsSelectedValues() {
        givenUser(SearchFilter.builder()
                        .regionCode(52)
                        .minExperience(2)
                        .minSalary(75000)
                        .keyword("бизнес-аналитик")
                        .build(),
                NotificationSettings.builder().notificationTime(LocalTime.of(7, 15)).build());

        assertThat(buttonTexts()).contains(
                "Регион [52]",
                "Минимальный опыт [2]",
                "Минимальная зарплата [75000]",
                "Слово для поиска [бизнес-аналитик]",
                "Настройки нотификации [07:15]");
    }

    @Test
    @DisplayName("меню содержит шесть кнопок")
    void hasSixButtons() {
        givenUser(new SearchFilter(), new NotificationSettings());

        assertThat(buttonTexts()).hasSize(6);
    }

    private List<String> buttonTexts() {
        InlineKeyboardMarkup markup =
                (InlineKeyboardMarkup) command.execute(request()).getReplyMarkup();
        return markup.getKeyboard().stream()
                .flatMap(List::stream)
                .map(button -> button.getText())
                .toList();
    }

    private void givenUser(SearchFilter filter, NotificationSettings settings) {
        when(userService.getUser(USER_ID)).thenReturn(BotUser.builder()
                .id(USER_ID)
                .chatId(CHAT_ID)
                .searchFilter(filter)
                .notificationSettings(settings)
                .build());
    }

    private UserRequest request() {
        return new UserRequest(USER_ID, CHAT_ID, MenuCommand.NAME, "", 1, false);
    }
}