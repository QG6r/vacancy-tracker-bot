package ru.skillbox.vacancybot.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.skillbox.vacancybot.model.BotUser;
import ru.skillbox.vacancybot.model.NotificationSettings;
import ru.skillbox.vacancybot.model.SearchFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Файловое хранилище пользователей")
class JsonUserRepositoryTest {

    @TempDir
    Path tempDir;

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonUserRepository(tempDir.toString());
    }

    @Test
    @DisplayName("сохраняет и читает пользователя")
    void savesAndReads() {
        repository.save(user(100L));

        assertThat(repository.findById(100L))
                .isPresent()
                .get()
                .satisfies(found -> assertThat(found.getChatId()).isEqualTo(200L));
    }

    @Test
    @DisplayName("сохраняет вложенные фильтры и настройки")
    void savesNestedObjects() {
        BotUser user = user(100L);
        user.setSearchFilter(SearchFilter.builder()
                .regionCode(78)
                .minExperience(3)
                .minSalary(90000)
                .keyword("Java")
                .build());
        user.setNotificationSettings(NotificationSettings.builder()
                .notificationTime(LocalTime.of(7, 15))
                .zoneOffset(ZoneOffset.ofHours(3))
                .build());

        repository.save(user);
        BotUser loaded = repository.findById(100L).orElseThrow();

        assertThat(loaded.getSearchFilter().getRegionCode()).isEqualTo(78);
        assertThat(loaded.getSearchFilter().getKeyword()).isEqualTo("Java");
        assertThat(loaded.getNotificationSettings().getNotificationTime())
                .isEqualTo(LocalTime.of(7, 15));
        assertThat(loaded.getNotificationSettings().getZoneOffset())
                .isEqualTo(ZoneOffset.ofHours(3));
    }

    @Test
    @DisplayName("несуществующий пользователь не находится")
    void returnsEmptyForUnknownId() {
        assertThat(repository.findById(999L)).isEmpty();
        assertThat(repository.exists(999L)).isFalse();
    }

    @Test
    @DisplayName("повторное сохранение перезаписывает данные")
    void overwritesOnSecondSave() {
        repository.save(user(100L));

        BotUser updated = user(100L);
        updated.setChatId(300L);
        repository.save(updated);

        assertThat(repository.findById(100L).orElseThrow().getChatId()).isEqualTo(300L);
    }

    @Test
    @DisplayName("удаление убирает файл")
    void deletesFile() {
        repository.save(user(100L));

        repository.delete(100L);

        assertThat(repository.exists(100L)).isFalse();
    }

    @Test
    @DisplayName("удаление несуществующего не падает")
    void deleteUnknownDoesNotFail() {
        repository.delete(999L);

        assertThat(repository.exists(999L)).isFalse();
    }

    @Test
    @DisplayName("findAll возвращает всех пользователей")
    void findsAll() {
        repository.save(user(100L));
        repository.save(user(101L));
        repository.save(user(102L));

        assertThat(repository.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("битый файл пропускается, остальные читаются")
    void skipsBrokenFile() throws IOException {
        repository.save(user(100L));
        Files.writeString(tempDir.resolve("users").resolve("999.json"), "не json");

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("на пустом хранилище findAll возвращает пустой список")
    void findsAllOnEmptyStorage() {
        assertThat(repository.findAll()).isEmpty();
    }

    private BotUser user(Long id) {
        return BotUser.builder().id(id).chatId(200L).build();
    }
}