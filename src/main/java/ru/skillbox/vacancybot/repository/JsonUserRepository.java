package ru.skillbox.vacancybot.repository;

import ru.skillbox.vacancybot.exception.RepositoryException;
import ru.skillbox.vacancybot.model.BotUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Хранит пользователей в JSON-файлах: data/users/{userId}.json
 */
public class JsonUserRepository extends AbstractJsonRepository<BotUser> implements UserRepository {

    public JsonUserRepository(String storagePath) {
        super(storagePath, "users", BotUser.class);
    }

    @Override
    public Optional<BotUser> findById(Long id) {
        return read(id);
    }

    @Override
    public boolean exists(Long id) {
        return fileExists(id);
    }

    @Override
    public void save(BotUser user) {
        write(user.getId(), user);
    }

    @Override
    public void delete(Long id) {
        remove(id);
    }

    @Override
    public List<BotUser> findAll() {
        List<BotUser> users = new ArrayList<>();
        try (Stream<Path> files = Files.list(getDirectory())) {
            files.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> readFile(path).ifPresent(users::add));
        } catch (IOException e) {
            throw new RepositoryException("Не удалось прочитать каталог пользователей", e);
        }
        return users;
    }

    private Optional<BotUser> readFile(Path path) {
        try {
            return Optional.of(objectMapper.readValue(path.toFile(), BotUser.class));
        } catch (IOException e) {
            // Битый файл не должен ронять запуск приложения
            return Optional.empty();
        }
    }
}
