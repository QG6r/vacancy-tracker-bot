package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.UserService;
import ru.skillbox.vacancybot.service.VacancyService;
import ru.skillbox.vacancybot.service.scheduler.TaskScheduler;

import java.util.List;

public class StopCommand implements BotCommand {

    public static final String NAME = "stop";

    private static final String YES = "yes";
    private static final String NO = "no";

    private final UserService userService;
    private final VacancyService vacancyService;
    private final TaskScheduler taskScheduler;
    private final KeyboardFactory keyboardFactory;

    public StopCommand(UserService userService,
                       VacancyService vacancyService,
                       TaskScheduler taskScheduler,
                       KeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.vacancyService = vacancyService;
        this.taskScheduler = taskScheduler;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        return NAME.equals(request.command());
    }

    @Override
    public SendMessage execute(UserRequest request) {
        if (YES.equals(request.argument())) {
            taskScheduler.cancel(request.userId());
            vacancyService.delete(request.userId());
            userService.delete(request.userId());

            return text(request, "Вся информация, связанная с вашими данными, удалена!\n"
                    + "Благодарим за использование Vacancy Tracker Bot!");
        }

        if (NO.equals(request.argument())) {
            return text(request,
                    "Благодарим, что продолжаете пользоваться Vacancy Tracker Bot!");
        }

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Вся информация, связанная с вашими данными, будет удалена!\nВы уверены?")
                .replyMarkup(keyboardFactory.singleRow(List.of(
                        KeyboardFactory.Button.of("Да", NAME, YES),
                        KeyboardFactory.Button.of("Нет", NAME, NO)
                )))
                .build();
    }

    private SendMessage text(UserRequest request, String message) {
        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text(message)
                .build();
    }
}