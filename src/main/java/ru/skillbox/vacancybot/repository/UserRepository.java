package ru.skillbox.vacancybot.repository;

import ru.skillbox.vacancybot.model.BotUser;

import java.util.List;
import java.util.Optional;

/**
 * Доступ к данным пользователей.
 * <p>
 * Сервисы работают именно с этим интерфейсом, а не с конкретной
 * реализацией. Если хранилище сменится на базу данных, достаточно
 * будет написать новую реализацию и поменять одну строку в Application.
 */
public interface UserRepository {

    Optional<BotUser> findById(Long id);

    boolean exists(Long id);

    void save(BotUser user);

    void delete(Long id);

    /** Все пользователи — нужны при старте для восстановления задач. */
    List<BotUser> findAll();
}
