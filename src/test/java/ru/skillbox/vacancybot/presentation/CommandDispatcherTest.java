package ru.skillbox.vacancybot.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.presentation.command.BotCommand;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Диспетчер команд")
class CommandDispatcherTest {

    private final UserRequest request =
            new UserRequest(1L, 1L, "menu", "", 1, false);

    @Test
    @DisplayName("вызывает команду, которая согласилась обработать запрос")
    void callsSupportingCommand() {
        BotCommand supporting = mock(BotCommand.class);
        SendMessage expected = SendMessage.builder().chatId("1").text("ok").build();
        when(supporting.supports(request)).thenReturn(true);
        when(supporting.execute(request)).thenReturn(expected);

        BotCommand fallback = mock(BotCommand.class);
        CommandDispatcher dispatcher = new CommandDispatcher(List.of(supporting), fallback);

        SendMessage result = dispatcher.dispatch(request);

        assertThat(result).isEqualTo(expected);
        verify(fallback, never()).execute(any());
    }

    @Test
    @DisplayName("вызывает обработчик по умолчанию, если подходящей команды нет")
    void callsDefaultWhenNoneSupports() {
        BotCommand notSupporting = mock(BotCommand.class);
        when(notSupporting.supports(request)).thenReturn(false);

        SendMessage expected = SendMessage.builder().chatId("1").text("unknown").build();
        BotCommand fallback = mock(BotCommand.class);
        when(fallback.execute(request)).thenReturn(expected);

        CommandDispatcher dispatcher = new CommandDispatcher(List.of(notSupporting), fallback);

        assertThat(dispatcher.dispatch(request)).isEqualTo(expected);
    }

    @Test
    @DisplayName("берёт первую подходящую команду, если их несколько")
    void callsFirstSupportingCommand() {
        SendMessage first = SendMessage.builder().chatId("1").text("first").build();
        BotCommand firstCommand = mock(BotCommand.class);
        when(firstCommand.supports(request)).thenReturn(true);
        when(firstCommand.execute(request)).thenReturn(first);

        BotCommand secondCommand = mock(BotCommand.class);

        CommandDispatcher dispatcher =
                new CommandDispatcher(List.of(firstCommand, secondCommand), mock(BotCommand.class));

        assertThat(dispatcher.dispatch(request)).isEqualTo(first);
        verify(secondCommand, never()).execute(any());
    }
}
