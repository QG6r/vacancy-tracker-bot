package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.formatter.VacancyFormatter;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.VacancyService;

import java.util.ArrayList;
import java.util.List;

public class VacanciesCommand implements BotCommand {

    public static final String NAME = "vacancies";

    private static final String PAGE_PREFIX = "page:";

    private final VacancyService vacancyService;
    private final VacancyFormatter formatter;
    private final KeyboardFactory keyboardFactory;

    public VacanciesCommand(VacancyService vacancyService,
                            VacancyFormatter formatter,
                            KeyboardFactory keyboardFactory) {
        this.vacancyService = vacancyService;
        this.formatter = formatter;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        return NAME.equals(request.command());
    }

    @Override
    public SendMessage execute(UserRequest request) {
        List<Vacancy> vacancies = vacancyService.getSaved(request.userId());

        if (vacancies.isEmpty()) {
            return SendMessage.builder()
                    .chatId(request.chatId().toString())
                    .text("Пока нет найденных вакансий. "
                            + "Задайте критерии в /menu и нажмите \"Готово\".")
                    .replyMarkup(keyboardFactory.single(
                            KeyboardFactory.Button.of("Menu", MenuCommand.NAME)))
                    .build();
        }

        int offset = parseOffset(request.argument());

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .parseMode("HTML")
                .text(formatter.formatPage(vacancies, offset))
                .replyMarkup(buildNavigation(vacancies.size(), offset))
                .build();
    }

    private org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
    buildNavigation(int total, int offset) {
        List<KeyboardFactory.Button> navigation = new ArrayList<>();

        if (offset > 0) {
            navigation.add(KeyboardFactory.Button.of("Предыдущие 10", NAME,
                    PAGE_PREFIX + Math.max(0, offset - VacancyFormatter.PAGE_SIZE)));
        }
        if (offset + VacancyFormatter.PAGE_SIZE < total) {
            navigation.add(KeyboardFactory.Button.of("Следующие 10", NAME,
                    PAGE_PREFIX + (offset + VacancyFormatter.PAGE_SIZE)));
        }
        if (navigation.isEmpty()) {
            navigation.add(KeyboardFactory.Button.of("Menu", MenuCommand.NAME));
        }

        return keyboardFactory.singleRow(navigation);
    }

    private int parseOffset(String argument) {
        if (argument == null || !argument.startsWith(PAGE_PREFIX)) {
            return 0;
        }
        try {
            return Integer.parseInt(argument.substring(PAGE_PREFIX.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}