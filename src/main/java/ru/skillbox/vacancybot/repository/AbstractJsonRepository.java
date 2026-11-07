package ru.skillbox.vacancybot.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.skillbox.vacancybot.exception.RepositoryException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Общая логика хранения объектов в JSON-файлах.
 * <p>
 * Вынесена в абстрактный класс, чтобы не дублировать её в каждом
 * репозитории. Наследнику остаётся указать тип объекта и подкаталог.
 *
 * @param <T> тип хранимого объекта
 */
public abstract class AbstractJsonRepository<T> {

    protected final ObjectMapper objectMapper;
    private final Path directory;
    private final Class<T> type;

    protected AbstractJsonRepository(String storagePath, String subdirectory, Class<T> type) {
        this.type = type;
        this.directory = Path.of(storagePath, subdirectory);
        this.objectMapper = createObjectMapper();
        createDirectory();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Без этого модуля Jackson не умеет LocalTime и ZoneOffset
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Незнакомое поле в файле (например, оставшееся от старой версии
        // модели) не должно ронять чтение
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    private void createDirectory() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RepositoryException("Не удалось создать каталог хранилища: " + directory, e);
        }
    }

    protected Optional<T> read(Long id) {
        Path file = fileOf(id);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(file.toFile(), type));
        } catch (IOException e) {
            throw new RepositoryException(
                    "Не удалось прочитать файл: " + file + ". Причина: " + e.getMessage(), e);
        }
    }

    protected void write(Long id, T object) {
        Path file = fileOf(id);
        try {
            objectMapper.writeValue(file.toFile(), object);
        } catch (IOException e) {
            throw new RepositoryException("Не удалось записать файл: " + file, e);
        }
    }

    protected void remove(Long id) {
        try {
            Files.deleteIfExists(fileOf(id));
        } catch (IOException e) {
            throw new RepositoryException("Не удалось удалить файл: " + fileOf(id), e);
        }
    }

    protected boolean fileExists(Long id) {
        return Files.exists(fileOf(id));
    }

    protected Path getDirectory() {
        return directory;
    }

    private Path fileOf(Long id) {
        return directory.resolve(id + ".json");
    }
}
