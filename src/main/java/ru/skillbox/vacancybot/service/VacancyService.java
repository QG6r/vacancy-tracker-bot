package ru.skillbox.vacancybot.service;

import ru.skillbox.vacancybot.model.Vacancy;

import java.util.List;

public interface VacancyService {

    List<Vacancy> searchAndSave(Long userId);

    List<Vacancy> getSaved(Long userId);

    void delete(Long userId);
}