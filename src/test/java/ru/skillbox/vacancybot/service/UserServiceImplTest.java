package ru.skillbox.vacancybot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.skillbox.vacancybot.exception.UserNotFoundException;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.repository.UserRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Сервис пользователей")
class UserServiceImplTest {

    private static final Long USER_ID = 100L;
    private static final Long CHAT_ID = 200L;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(new InMemoryUserRepository());
    }

    @Test
    @DisplayName("новый пользователь регистрируется")
    void registersNewUser() {
        assertThat(userService.register(USER_ID, CHAT_ID)).isTrue();
        assertThat(userService.isRegistered(USER_ID)).isTrue();
    }

    @Test
    @DisplayName("повторная регистрация возвращает false")
    void doesNotRegisterTwice() {
        userService.register(USER_ID, CHAT_ID);

        assertThat(userService.register(USER_ID, CHAT_ID)).isFalse();
    }

    @Test
    @DisplayName("фильтры сохраняются")
    void savesFilters() {
        userService.register(USER_ID, CHAT_ID);

        userService.setRegion(USER_ID, 52);
        userService.setExperience(USER_ID, 2);
        userService.setSalary(USER_ID, 75000);
        userService.setKeyword(USER_ID, "бизнес-аналитик");

        BotUser user = userService.getUser(USER_ID);
        assertThat(user.getSearchFilter().getRegionCode()).isEqualTo(52);
        assertThat(user.getSearchFilter().getMinExperience()).isEqualTo(2);
        assertThat(user.getSearchFilter().getMinSalary()).isEqualTo(75000);
        assertThat(user.getSearchFilter().getKeyword()).isEqualTo("бизнес-аналитик");
    }

    @Test
    @DisplayName("время нотификации сохраняется")
    void savesNotificationTime() {
        userService.register(USER_ID, CHAT_ID);

        userService.setNotificationTime(USER_ID, LocalTime.of(7, 15));

        assertThat(userService.getUser(USER_ID).getNotificationSettings().getNotificationTime())
                .isEqualTo(LocalTime.of(7, 15));
    }

    @Test
    @DisplayName("запрос несуществующего пользователя бросает исключение")
    void throwsWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("удаление убирает пользователя")
    void deletesUser() {
        userService.register(USER_ID, CHAT_ID);

        userService.delete(USER_ID);

        assertThat(userService.isRegistered(USER_ID)).isFalse();
    }

    /**
     * Подставная реализация репозитория — тесты сервиса не должны
     * лезть в файловую систему.
     */
    private static class InMemoryUserRepository implements UserRepository {

        private final Map<Long, BotUser> storage = new HashMap<>();

        @Override
        public Optional<BotUser> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public boolean exists(Long id) {
            return storage.containsKey(id);
        }

        @Override
        public void save(BotUser user) {
            storage.put(user.getId(), user);
        }

        @Override
        public void delete(Long id) {
            storage.remove(id);
        }

        @Override
        public List<BotUser> findAll() {
            return new ArrayList<>(storage.values());
        }
    }
}
