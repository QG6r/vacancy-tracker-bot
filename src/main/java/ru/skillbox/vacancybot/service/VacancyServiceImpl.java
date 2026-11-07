package ru.skillbox.vacancybot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.UserVacancies;
import ru.skillbox.vacancybot.model.Vacancy;
import ru.skillbox.vacancybot.repository.VacancyRepository;
import ru.skillbox.vacancybot.service.client.VacancyApiClient;

import java.time.LocalDateTime;
import java.util.List;

public class VacancyServiceImpl implements VacancyService {

    private static final Logger log = LoggerFactory.getLogger(VacancyServiceImpl.class);

    private static final int SEARCH_LIMIT = 100;

    private final VacancyApiClient apiClient;
    private final VacancyRepository vacancyRepository;
    private final UserService userService;

    public VacancyServiceImpl(VacancyApiClient apiClient,
                              VacancyRepository vacancyRepository,
                              UserService userService) {
        this.apiClient = apiClient;
        this.vacancyRepository = vacancyRepository;
        this.userService = userService;
    }

    @Override
    public List<Vacancy> searchAndSave(Long userId) {
        BotUser user = userService.getUser(userId);

        List<Vacancy> found = apiClient.search(
                user.getSearchFilter(), user.getLastCheckedAt(), SEARCH_LIMIT);

        // API не умеет фильтровать по зарплате — делаем это сами
        List<Vacancy> filtered = found.stream()
                .filter(vacancy -> matchesSalary(vacancy, user.getSearchFilter().getMinSalary()))
                .toList();

        log.info("Пользователь {}: получено {} вакансий, после фильтра по зарплате {}",
                userId, found.size(), filtered.size());

        vacancyRepository.save(UserVacancies.builder()
                .userId(userId)
                .vacancies(filtered)
                .build());

        userService.setLastCheckedAt(userId, LocalDateTime.now());

        return filtered;
    }

    @Override
    public List<Vacancy> getSaved(Long userId) {
        return vacancyRepository.findByUserId(userId)
                .map(UserVacancies::getVacancies)
                .orElse(List.of());
    }

    @Override
    public void delete(Long userId) {
        vacancyRepository.delete(userId);
    }

    private boolean matchesSalary(Vacancy vacancy, Integer minSalary) {
        if (minSalary == null) {
            return true;
        }
        Integer max = vacancy.getSalaryMax() != null ? vacancy.getSalaryMax() : vacancy.getSalaryMin();
        return max == null || max >= minSalary;
    }
}