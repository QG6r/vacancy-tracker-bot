package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;

import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Смена часового пояса")
class TimezoneCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private TimezoneCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        command = new TimezoneCommand(userService, new KeyboardFactory());
    }

    @Test
    @DisplayName("распознаёт UTC+3")
    void parsesPositiveOffset() {
        command.execute(request("UTC+3", ""));

        verify(userService).setZoneOffset(USER_ID, ZoneOffset.ofHours(3));
    }

    @Test
    @DisplayName("распознаёт UTC-5")
    void parsesNegativeOffset() {
        command.execute(request("UTC-5", ""));

        verify(userService).setZoneOffset(USER_ID, ZoneOffset.ofHours(-5));
    }

    @Test
    @DisplayName("распознаёт смещение с минутами")
    void parsesOffsetWithMinutes() {
        command.execute(request("UTC+3:30", ""));

        verify(userService).setZoneOffset(USER_ID, ZoneOffset.ofHoursMinutes(3, 30));
    }

    @Test
    @DisplayName("распознаёт запись без знака")
    void parsesOffsetWithoutSign() {
        command.execute(request("UTC2:00", ""));

        verify(userService).setZoneOffset(USER_ID, ZoneOffset.ofHours(2));
    }

    @Test
    @DisplayName("реагирует на текст, похожий на часовой пояс")
    void supportsTimezonePattern() {
        assertThat(command.supports(request("UTC+3", ""))).isTrue();
        assertThat(command.supports(request("UTC-11", ""))).isTrue();
    }

    @Test
    @DisplayName("не реагирует на обычные команды")
    void doesNotSupportRegularCommands() {
        assertThat(command.supports(request("menu", ""))).isFalse();
        assertThat(command.supports(request("region", "52"))).isFalse();
        assertThat(command.supports(request("UTCabc", ""))).isFalse();
    }

    @Test
    @DisplayName("не реагирует на нажатия кнопок")
    void doesNotSupportCallbacks() {
        assertThat(command.supports(
                new UserRequest(USER_ID, CHAT_ID, "UTC+3", "", 1, true))).isFalse();
    }

    private UserRequest request(String command, String argument) {
        return new UserRequest(USER_ID, CHAT_ID, command, argument, 1, false);
    }
}