package ru.skillbox.vacancybot.service;

import ru.skillbox.vacancybot.model.BotUser;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Бизнес-логика работы с пользователями.
 * Слой представления обращается только сюда, а не в репозиторий напрямую.
 */
public interface UserService {

    /** Регистрирует пользователя, если его ещё нет. Возвращает true для нового. */
    boolean register(Long userId, Long chatId);

    BotUser getUser(Long userId);

    boolean isRegistered(Long userId);

    void setRegion(Long userId, Integer regionCode);

    void setExperience(Long userId, Integer years);

    void setSalary(Long userId, Integer salary);

    void setKeyword(Long userId, String keyword);

    void setNotificationTime(Long userId, LocalTime time);

    void setZoneOffset(Long userId, ZoneOffset offset);

    void setSearchActive(Long userId, boolean active);

    void delete(Long userId);

    List<BotUser> findAll();

    void setLastCheckedAt(Long userId, LocalDateTime moment);
}
