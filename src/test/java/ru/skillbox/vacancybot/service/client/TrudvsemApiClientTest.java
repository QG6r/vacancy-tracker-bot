package ru.skillbox.vacancybot.service.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.exception.ApiException;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.model.Vacancy;

import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Клиент API агрегатора")
class TrudvsemApiClientTest {

    private final TrudvsemApiClient client = new TrudvsemApiClient(
            HttpClient.newHttpClient(),
            new ObjectMapper()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

    @Test
    @DisplayName("без фильтров запрашивает все вакансии")
    void buildsUrlWithoutFilters() {
        String url = client.buildUrl(new SearchFilter(), null, 10);

        assertThat(url).isEqualTo(
                "https://opendata.trudvsem.ru/api/v1/vacancies?offset=0&limit=10");
    }

    @Test
    @DisplayName("регион уходит в путь запроса")
    void putsRegionIntoPath() {
        SearchFilter filter = SearchFilter.builder().regionCode(78).build();

        assertThat(client.buildUrl(filter, null, 10))
                .contains("/vacancies/region/78?");
    }

    @Test
    @DisplayName("опыт и ключевые слова уходят в параметры")
    void addsExperienceAndKeyword() {
        SearchFilter filter = SearchFilter.builder()
                .minExperience(2)
                .keyword("Java")
                .build();

        String url = client.buildUrl(filter, null, 10);

        assertThat(url).contains("experienceFrom=2");
        assertThat(url).contains("text=Java");
    }

    @Test
    @DisplayName("ключевые слова кодируются для URL")
    void encodesKeyword() {
        SearchFilter filter = SearchFilter.builder().keyword("программист Java").build();

        assertThat(client.buildUrl(filter, null, 10))
                .contains("text=%D0%BF")
                .doesNotContain("text=программист");
    }

    @Test
    @DisplayName("дата уходит в ISO-формате")
    void addsModifiedFrom() {
        assertThat(client.buildUrl(new SearchFilter(), LocalDateTime.of(2025, 1, 1, 1, 0), 10))
                .contains("modifiedFrom=2025-01-01T01:00:00Z");
    }

    @Test
    @DisplayName("лимит не превышает 100")
    void capsLimit() {
        assertThat(client.buildUrl(new SearchFilter(), null, 500)).contains("limit=100");
    }

    @Test
    @DisplayName("разбирает ответ агрегатора")
    void parsesResponse() {
        String json = """
                {
                  "status": "200",
                  "results": {
                    "vacancies": [
                      {
                        "vacancy": {
                          "id": "abc-123",
                          "job-name": "Java-разработчик",
                          "salary_min": 100000,
                          "salary_max": 150000,
                          "vac_url": "https://trudvsem.ru/vacancy/card/abc-123",
                          "company": { "name": "ООО Рога и Копыта" },
                          "region": { "region_code": "78", "name": "Санкт-Петербург" },
                          "requirement": { "experience": 3 }
                        }
                      }
                    ]
                  }
                }""";

        List<Vacancy> vacancies = client.parse(json);

        assertThat(vacancies).hasSize(1);
        Vacancy vacancy = vacancies.get(0);
        assertThat(vacancy.getId()).isEqualTo("abc-123");
        assertThat(vacancy.getJobName()).isEqualTo("Java-разработчик");
        assertThat(vacancy.getCompanyName()).isEqualTo("ООО Рога и Копыта");
        assertThat(vacancy.getSalaryMin()).isEqualTo(100000);
        assertThat(vacancy.getExperience()).isEqualTo(3);
        assertThat(vacancy.getRegionName()).isEqualTo("Санкт-Петербург");
    }

    @Test
    @DisplayName("пустой results не ломает разбор")
    void parsesEmptyResults() {
        assertThat(client.parse("{\"status\":\"200\",\"results\":\"\"}")).isEmpty();
    }

    @Test
    @DisplayName("вакансия без компании получает заглушку")
    void handlesMissingCompany() {
        String json = """
                {"results":{"vacancies":[{"vacancy":{"id":"1","job-name":"Тест"}}]}}""";

        assertThat(client.parse(json).get(0).getCompanyName()).isEqualTo("Не указана");
    }

    @Test
    @DisplayName("испорченный JSON вызывает ApiException")
    void throwsOnBrokenJson() {
        assertThatThrownBy(() -> client.parse("не json"))
                .isInstanceOf(ApiException.class);
    }
}