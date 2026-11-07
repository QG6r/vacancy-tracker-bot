package ru.skillbox.vacancybot.service.client;

import ru.skillbox.vacancybot.model.SearchFilter;
import ru.skillbox.vacancybot.model.Vacancy;

import java.time.LocalDateTime;
import java.util.List;

public interface VacancyApiClient {

    List<Vacancy> search(SearchFilter filter, LocalDateTime modifiedFrom, int limit);
}