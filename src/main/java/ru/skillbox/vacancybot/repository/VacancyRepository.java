package ru.skillbox.vacancybot.repository;

import ru.skillbox.vacancybot.model.UserVacancies;

import java.util.Optional;

public interface VacancyRepository {

    Optional<UserVacancies> findByUserId(Long userId);

    void save(UserVacancies userVacancies);

    void delete(Long userId);
}