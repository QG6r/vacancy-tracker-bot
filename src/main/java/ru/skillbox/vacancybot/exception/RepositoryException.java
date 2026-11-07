package ru.skillbox.vacancybot.exception;

/**
 * Ошибка при чтении или записи данных в хранилище.
 */
public class RepositoryException extends BotException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
