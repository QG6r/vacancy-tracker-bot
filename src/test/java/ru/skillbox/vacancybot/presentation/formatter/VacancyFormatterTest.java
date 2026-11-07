package ru.skillbox.vacancybot.presentation.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.model.Vacancy;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Форматирование вакансий")
class VacancyFormatterTest {

    private final VacancyFormatter formatter = new VacancyFormatter();

    @Test
    @DisplayName("выводит компанию, зарплату, опыт и ссылку")
    void formatsFullVacancy() {
        String text = formatter.format(Vacancy.builder()
                .companyName("ООО ИТМ")
                .jobName("Java-разработчик")
                .salaryMin(350000)
                .salaryMax(350000)
                .experience(5)
                .url("https://trudvsem.ru/vacancy/1")
                .build());

        assertThat(text).contains("Компания: \"ООО ИТМ\"");
        assertThat(text).contains("Зарплата: 350000-350000 руб.");
        assertThat(text).contains("Опыт: 5.");
        assertThat(text).contains("https://trudvsem.ru/vacancy/1");
    }

    @Test
    @DisplayName("вакансия без зарплаты помечается как по договорённости")
    void formatsMissingSalary() {
        String text = formatter.format(Vacancy.builder().companyName("Тест").build());

        assertThat(text).contains("Зарплата: по договорённости");
        assertThat(text).contains("Опыт: не указан");
    }

    @Test
    @DisplayName("указана только нижняя граница зарплаты")
    void formatsOnlyMinSalary() {
        String text = formatter.format(Vacancy.builder()
                .companyName("Тест")
                .salaryMin(50000)
                .build());

        assertThat(text).contains("Зарплата: от 50000 руб.");
    }

    @Test
    @DisplayName("символы разметки в названии компании экранируются")
    void escapesHtml() {
        String text = formatter.format(Vacancy.builder()
                .companyName("ООО <Рога & Копыта>")
                .build());

        assertThat(text).contains("&lt;Рога &amp; Копыта&gt;");
        assertThat(text).doesNotContain("<Рога");
    }

    @Test
    @DisplayName("пустой список даёт понятное сообщение")
    void formatsEmptyList() {
        assertThat(formatter.formatPage(List.of(), 0))
                .isEqualTo("По вашим критериям новых вакансий не найдено.");
    }

    @Test
    @DisplayName("на странице не больше 10 вакансий")
    void limitsPageSize() {
        String page = formatter.formatPage(vacancies(25), 0);

        assertThat(page).contains("Показаны вакансии 1-10 из 25");
        assertThat(page).contains("Компания-1");
        assertThat(page).contains("Компания-10");
        assertThat(page).doesNotContain("Компания-11");
    }

    @Test
    @DisplayName("смещение показывает нужную страницу")
    void respectsOffset() {
        String page = formatter.formatPage(vacancies(25), 10);

        assertThat(page).contains("Показаны вакансии 11-20 из 25");
        assertThat(page).contains("Компания-11");
    }

    @Test
    @DisplayName("последняя страница может быть неполной")
    void handlesLastPartialPage() {
        assertThat(formatter.formatPage(vacancies(25), 20))
                .contains("Показаны вакансии 21-25 из 25");
    }

    @Test
    @DisplayName("смещение за пределами списка не ломает вывод")
    void handlesOffsetBeyondEnd() {
        assertThat(formatter.formatPage(vacancies(5), 100)).contains("из 5");
    }

    private List<Vacancy> vacancies(int count) {
        List<Vacancy> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            result.add(Vacancy.builder()
                    .id(String.valueOf(i))
                    .companyName("Компания-" + i)
                    .build());
        }
        return result;
    }
}