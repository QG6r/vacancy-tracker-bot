package ru.skillbox.vacancybot.exception;

/**
 * Пользователь не найден в хранилище.
 */
public class UserNotFoundException extends BotException {

    public UserNotFoundException(Long userId) {
        super("Пользователь не найден: " + userId);
    }
}
