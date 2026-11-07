package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.VacancyService;
import ru.skillbox.vacancybot.service.scheduler.TaskScheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("Команда /stop")
class StopCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;
    private VacancyService vacancyService;
    private TaskScheduler taskScheduler;
    private StopCommand command;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        vacancyService = mock(VacancyService.class);
        taskScheduler = mock(TaskScheduler.class);
        command = new StopCommand(userService, vacancyService, taskScheduler, new KeyboardFactory());
    }

    @Test
    @DisplayName("без ответа спрашивает подтверждение")
    void asksConfirmation() {
        SendMessage result = command.execute(request(""));

        assertThat(result.getText()).contains("Вы уверены?");
        verify(userService, never()).delete(anyLong());
    }

    @Test
    @DisplayName("при отказе ничего не удаляется")
    void keepsDataOnNo() {
        SendMessage result = command.execute(request("no"));

        assertThat(result.getText()).contains("продолжаете пользоваться");
        verify(userService, never()).delete(anyLong());
        verify(taskScheduler, never()).cancel(anyLong());
    }

    @Test
    @DisplayName("при подтверждении удаляет задачи, вакансии и пользователя")
    void deletesEverythingOnYes() {
        SendMessage result = command.execute(request("yes"));

        assertThat(result.getText()).contains("удалена");
        verify(taskScheduler).cancel(USER_ID);
        verify(vacancyService).delete(USER_ID);
        verify(userService).delete(USER_ID);
    }

    @Test
    @DisplayName("задачи снимаются до удаления данных")
    void cancelsTasksBeforeDeletingData() {
        command.execute(request("yes"));

        InOrder inOrder = inOrder(taskScheduler, vacancyService, userService);
        inOrder.verify(taskScheduler).cancel(USER_ID);
        inOrder.verify(vacancyService).delete(USER_ID);
        inOrder.verify(userService).delete(USER_ID);
    }

    private UserRequest request(String argument) {
        return new UserRequest(USER_ID, CHAT_ID, StopCommand.NAME, argument, 1, false);
    }
}