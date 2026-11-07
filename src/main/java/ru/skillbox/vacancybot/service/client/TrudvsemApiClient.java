package ru.skillbox.vacancybot.service.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillbox.vacancybot.exception.ApiException;
import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.service.client.dto.TrudvsemResponse;
import ru.skillbox.vacancybot.service.client.dto.VacancyDto;
import ru.skillbox.vacancybot.service.client.dto.VacancyWrapperDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TrudvsemApiClient implements VacancyApiClient {

    private static final Logger log = LoggerFactory.getLogger(TrudvsemApiClient.class);

    private static final String BASE_URL = "https://opendata.trudvsem.ru/api/v1/vacancies";
    private static final int MAX_LIMIT = 100;
    private static final int TIMEOUT_SECONDS = 30;

    private static final DateTimeFormatter ISO_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TrudvsemApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // Когда ничего не найдено, API возвращает "results": ""
                // вместо объекта — без этой настройки разбор упал бы
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

    TrudvsemApiClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Vacancy> search(SearchFilter filter, LocalDateTime modifiedFrom, int limit) {
        String url = buildUrl(filter, modifiedFrom, limit);
        log.info("Запрос к API агрегатора: {}", url);

        String body = sendRequest(url);
        return parse(body);
    }

    String buildUrl(SearchFilter filter, LocalDateTime modifiedFrom, int limit) {
        StringBuilder url = new StringBuilder(BASE_URL);

        if (filter.getRegionCode() != null) {
            url.append("/region/").append(filter.getRegionCode());
        }

        url.append("?offset=0&limit=").append(Math.min(limit, MAX_LIMIT));

        if (filter.getMinExperience() != null) {
            url.append("&experienceFrom=").append(filter.getMinExperience());
        }
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            url.append("&text=")
                    .append(URLEncoder.encode(filter.getKeyword(), StandardCharsets.UTF_8));
        }
        if (modifiedFrom != null) {
            url.append("&modifiedFrom=")
                    .append(modifiedFrom.atOffset(ZoneOffset.UTC).format(ISO_FORMAT));
        }

        return url.toString();
    }

    private String sendRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ApiException(
                        "Агрегатор вернул код " + response.statusCode() + " для запроса " + url);
            }
            return response.body();

        } catch (IOException e) {
            throw new ApiException("Не удалось получить ответ от агрегатора вакансий", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Запрос к агрегатору прерван", e);
        }
    }

    List<Vacancy> parse(String body) {
        TrudvsemResponse response;
        try {
            response = objectMapper.readValue(body, TrudvsemResponse.class);
        } catch (IOException e) {
            throw new ApiException("Не удалось разобрать ответ агрегатора", e);
        }

        if (response.getResults() == null || response.getResults().getVacancies() == null) {
            return List.of();
        }

        List<Vacancy> vacancies = new ArrayList<>();
        for (VacancyWrapperDto wrapper : response.getResults().getVacancies()) {
            if (wrapper.getVacancy() != null) {
                vacancies.add(toVacancy(wrapper.getVacancy()));
            }
        }
        return vacancies;
    }

    private Vacancy toVacancy(VacancyDto dto) {
        return Vacancy.builder()
                .id(dto.getId())
                .jobName(dto.getJobName())
                .companyName(dto.getCompany() == null ? "Не указана" : dto.getCompany().getName())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .experience(dto.getRequirement() == null ? null : dto.getRequirement().getExperience())
                .url(dto.getVacUrl())
                .regionName(dto.getRegion() == null ? null : dto.getRegion().getName())
                .build();
    }
}