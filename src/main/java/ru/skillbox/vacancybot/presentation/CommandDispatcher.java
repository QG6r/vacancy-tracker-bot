package ru.skillbox.vacancybot.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.exception.BotException;
import ru.skillbox.vacancybot.presentation.command.BotCommand;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;

import java.util.List;

/**
 * Выбирает команду, которая обработает запрос, и выполняет её.
 * <p>
 * Здесь же перехватываются бизнес-исключения: их сообщение написано
 * для пользователя, поэтому отправляется ему как есть. Так каждой
 * команде не нужно самой заботиться об обработке ошибок ввода.
 */
public class CommandDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final List<BotCommand> commands;
    private final BotCommand defaultCommand;

    public CommandDispatcher(List<BotCommand> commands, BotCommand defaultCommand) {
        this.commands = List.copyOf(commands);
        this.defaultCommand = defaultCommand;
    }

    public SendMessage dispatch(UserRequest request) {
        BotCommand command = commands.stream()
                .filter(candidate -> candidate.supports(request))
                .findFirst()
                .orElse(defaultCommand);

        try {
            return command.execute(request);
        } catch (BotException e) {
            log.warn("Ошибка при выполнении команды {}: {}", request.command(), e.getMessage());
            return SendMessage.builder()
                    .chatId(request.chatId().toString())
                    .text(e.getMessage())
                    .build();
        }
    }
}
