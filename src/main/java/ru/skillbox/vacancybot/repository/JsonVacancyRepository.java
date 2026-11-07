package ru.skillbox.vacancybot.repository;

import ru.skillbox.vacancybot.model.UserVacancies;

import java.util.Optional;

public class JsonVacancyRepository extends AbstractJsonRepository<UserVacancies>
        implements VacancyRepository {

    public JsonVacancyRepository(String storagePath) {
        super(storagePath, "vacancies", UserVacancies.class);
    }

    @Override
    public Optional<UserVacancies> findByUserId(Long userId) {
        return read(userId);
    }

    @Override
    public void save(UserVacancies userVacancies) {
        write(userVacancies.getUserId(), userVacancies);
    }

    @Override
    public void delete(Long userId) {
        remove(userId);
    }
}