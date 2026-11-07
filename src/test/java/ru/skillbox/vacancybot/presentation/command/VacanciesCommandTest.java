package ru.skillbox.vacancybot.presentation.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.formatter.VacancyFormatter;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.VacancyService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Команда /vacancies")
class VacanciesCommandTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private VacancyService vacancyService;
    private VacanciesCommand command;

    @BeforeEach
    void setUp() {
        vacancyService = mock(VacancyService.class);
        command = new VacanciesCommand(
                vacancyService, new VacancyFormatter(), new KeyboardFactory());
    }

    @Test
    @DisplayName("без сохранённых вакансий подсказывает настроить поиск")
    void suggestsSetupWhenEmpty() {
        when(vacancyService.getSaved(USER_ID)).thenReturn(List.of());

        assertThat(command.execute(request("")).getText())
                .contains("Пока нет найденных вакансий");
    }

    @Test
    @DisplayName("показывает первую страницу вакансий")
    void showsFirstPage() {
        when(vacancyService.getSaved(USER_ID)).thenReturn(vacancies(25));

        SendMessage result = command.execute(request(""));

        assertThat(result.getText()).contains("Показаны вакансии 1-10 из 25");
    }

    @Test
    @DisplayName("листает на следующую страницу")
    void showsRequestedPage() {
        when(vacancyService.getSaved(USER_ID)).thenReturn(vacancies(25));

        assertThat(command.execute(request("page:10")).getText())
                .contains("Показаны вакансии 11-20 из 25");
    }

    @Test
    @DisplayName("на первой странице только кнопка вперёд")
    void showsOnlyNextOnFirstPage() {
        when(vacancyService.getSaved(USER_ID)).thenReturn(vacancies(25));

        assertThat(buttonTexts(command.execute(request("")))).containsExactly("Следующие 10");
    }

    @Test
    @DisplayName("в середине списка есть обе кнопки")
    void showsBothNavigationButtons() {
        when(vacancyService.getSaved(USER_ID)).thenReturn(vacancies(25));

        assertThat(buttonTexts(command.execute(request("page:10"))))
                .containsExactly("Предыдущие 10", "Следующие 10");
    }

    @Test
    @DisplayName("если вакансий меньше страницы, навигации нет")
    void showsNoNavigationForShortList() {
        when(vacancyService.getSaved(USER_ID)).thenReturn(vacancies(5));

        assertThat(buttonTexts(command.execute(request("")))).containsExactly("Menu");
    }

    @Test
    @DisplayName("некорректное смещение трактуется как первая страница")
    void handlesBrokenOffset() {
        when(vacancyService.getSaved(USER_ID)).thenReturn(vacancies(25));

        assertThat(command.execute(request("page:abc")).getText())
                .contains("Показаны вакансии 1-10");
    }

    private List<String> buttonTexts(SendMessage message) {
        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) message.getReplyMarkup();
        return markup.getKeyboard().stream()
                .flatMap(List::stream)
                .map(button -> button.getText())
                .toList();
    }

    private List<Vacancy> vacancies(int count) {
        List<Vacancy> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            result.add(Vacancy.builder().id(String.valueOf(i)).companyName("Компания-" + i).build());
        }
        return result;
    }

    private UserRequest request(String argument) {
        return new UserRequest(USER_ID, CHAT_ID, VacanciesCommand.NAME, argument, 1, false);
    }
}