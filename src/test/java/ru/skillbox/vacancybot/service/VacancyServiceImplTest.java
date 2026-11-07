package ru.skillbox.vacancybot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.model.UserVacancies;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.repository.VacancyRepository;
import ru.skillbox.vacancybot.service.client.VacancyApiClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Сервис вакансий")
class VacancyServiceImplTest {

    private static final Long USER_ID = 100L;

    private VacancyApiClient apiClient;
    private VacancyRepository vacancyRepository;
    private UserService userService;
    private VacancyService vacancyService;

    @BeforeEach
    void setUp() {
        apiClient = mock(VacancyApiClient.class);
        vacancyRepository = mock(VacancyRepository.class);
        userService = mock(UserService.class);
        vacancyService = new VacancyServiceImpl(apiClient, vacancyRepository, userService);
    }

    @Test
    @DisplayName("отбрасывает вакансии с зарплатой ниже желаемой")
    void filtersBySalary() {
        givenUserWithMinSalary(75000);
        givenApiReturns(
                vacancy("1", 50000, 60000),
                vacancy("2", 80000, 120000));

        List<Vacancy> result = vacancyService.searchAndSave(USER_ID);

        assertThat(result).extracting(Vacancy::getId).containsExactly("2");
    }

    @Test
    @DisplayName("оставляет вакансии без указанной зарплаты")
    void keepsVacanciesWithoutSalary() {
        givenUserWithMinSalary(75000);
        givenApiReturns(vacancy("1", null, null));

        assertThat(vacancyService.searchAndSave(USER_ID)).hasSize(1);
    }

    @Test
    @DisplayName("без фильтра по зарплате возвращает всё")
    void returnsAllWhenNoSalaryFilter() {
        givenUserWithMinSalary(null);
        givenApiReturns(vacancy("1", 10000, 20000), vacancy("2", 90000, 90000));

        assertThat(vacancyService.searchAndSave(USER_ID)).hasSize(2);
    }

    @Test
    @DisplayName("сохраняет результат и отмечает время проверки")
    void savesResultAndUpdatesTimestamp() {
        givenUserWithMinSalary(null);
        givenApiReturns(vacancy("1", 50000, 50000));

        vacancyService.searchAndSave(USER_ID);

        verify(vacancyRepository).save(any(UserVacancies.class));
        verify(userService).setLastCheckedAt(eq(USER_ID), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("если ничего не сохранено, возвращает пустой список")
    void returnsEmptyWhenNothingSaved() {
        when(vacancyRepository.findByUserId(USER_ID)).thenReturn(java.util.Optional.empty());

        assertThat(vacancyService.getSaved(USER_ID)).isEmpty();
    }

    private void givenUserWithMinSalary(Integer minSalary) {
        BotUser user = BotUser.builder()
                .id(USER_ID)
                .chatId(USER_ID)
                .searchFilter(SearchFilter.builder().minSalary(minSalary).build())
                .build();
        when(userService.getUser(USER_ID)).thenReturn(user);
    }

    private void givenApiReturns(Vacancy... vacancies) {
        when(apiClient.search(any(SearchFilter.class), any(), anyInt()))
                .thenReturn(List.of(vacancies));
    }

    private Vacancy vacancy(String id, Integer salaryMin, Integer salaryMax) {
        return Vacancy.builder()
                .id(id)
                .jobName("Вакансия " + id)
                .salaryMin(salaryMin)
                .salaryMax(salaryMax)
                .build();
    }
}