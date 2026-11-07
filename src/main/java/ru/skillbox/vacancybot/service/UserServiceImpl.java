package ru.skillbox.vacancybot.service;

import ru.skillbox.vacancybot.exception.UserNotFoundException;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.repository.UserRepository;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Consumer;
import java.time.LocalDateTime;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean register(Long userId, Long chatId) {
        if (userRepository.exists(userId)) {
            BotUser existing = getUser(userId);
            existing.setChatId(chatId);
            userRepository.save(existing);
            return false;
        }

        userRepository.save(BotUser.builder()
                .id(userId)
                .chatId(chatId)
                .build());
        return true;
    }

    @Override
    public BotUser getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public boolean isRegistered(Long userId) {
        return userRepository.exists(userId);
    }

    @Override
    public void setRegion(Long userId, Integer regionCode) {
        update(userId, user -> user.getSearchFilter().setRegionCode(regionCode));
    }

    @Override
    public void setExperience(Long userId, Integer years) {
        update(userId, user -> user.getSearchFilter().setMinExperience(years));
    }

    @Override
    public void setSalary(Long userId, Integer salary) {
        update(userId, user -> user.getSearchFilter().setMinSalary(salary));
    }

    @Override
    public void setKeyword(Long userId, String keyword) {
        update(userId, user -> user.getSearchFilter().setKeyword(keyword));
    }

    @Override
    public void setNotificationTime(Long userId, LocalTime time) {
        update(userId, user -> user.getNotificationSettings().setNotificationTime(time));
    }

    @Override
    public void setZoneOffset(Long userId, ZoneOffset offset) {
        update(userId, user -> user.getNotificationSettings().setZoneOffset(offset));
    }

    @Override
    public void setSearchActive(Long userId, boolean active) {
        update(userId, user -> user.setSearchActive(active));
    }

    @Override
    public void delete(Long userId) {
        userRepository.delete(userId);
    }

    @Override
    public List<BotUser> findAll() {
        return userRepository.findAll();
    }

    private void update(Long userId, Consumer<BotUser> change) {
        BotUser user = getUser(userId);
        change.accept(user);
        userRepository.save(user);
    }

    @Override
    public void setLastCheckedAt(Long userId, LocalDateTime moment) {
        update(userId, user -> user.setLastCheckedAt(moment));
    }
}
