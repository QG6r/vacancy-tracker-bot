package ru.skillbox.vacancybot.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.skillbox.vacancybot.model.UserVacancies;
import ru.skillbox.vacancybot.model.Vacancy;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Файловое хранилище вакансий")
class JsonVacancyRepositoryTest {

    @TempDir
    Path tempDir;

    private VacancyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonVacancyRepository(tempDir.toString());
    }

    @Test
    @DisplayName("сохраняет и читает список вакансий")
    void savesAndReads() {
        repository.save(UserVacancies.builder()
                .userId(100L)
                .vacancies(List.of(
                        vacancy("1", "ООО Тест"),
                        vacancy("2", "АО Пример")))
                .build());

        UserVacancies loaded = repository.findByUserId(100L).orElseThrow();

        assertThat(loaded.getVacancies()).hasSize(2);
        assertThat(loaded.getVacancies().get(0).getCompanyName()).isEqualTo("ООО Тест");
    }

    @Test
    @DisplayName("пустой список сохраняется корректно")
    void savesEmptyList() {
        repository.save(UserVacancies.builder().userId(100L).vacancies(List.of()).build());

        assertThat(repository.findByUserId(100L).orElseThrow().getVacancies()).isEmpty();
    }

    @Test
    @DisplayName("для неизвестного пользователя ничего не находится")
    void returnsEmptyForUnknownUser() {
        assertThat(repository.findByUserId(999L)).isEmpty();
    }

    @Test
    @DisplayName("удаление убирает вакансии")
    void deletes() {
        repository.save(UserVacancies.builder().userId(100L).build());

        repository.delete(100L);

        assertThat(repository.findByUserId(100L)).isEmpty();
    }

    private Vacancy vacancy(String id, String company) {
        return Vacancy.builder()
                .id(id)
                .companyName(company)
                .jobName("Разработчик")
                .salaryMin(100000)
                .salaryMax(150000)
                .experience(3)
                .url("https://trudvsem.ru/vacancy/" + id)
                .build();
    }
}