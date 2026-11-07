package ru.skillbox.vacancybot.exception;

/**
 * Базовое исключение приложения. От него наследуются все остальные —
 * это позволяет ловить любую бизнес-ошибку одним catch.
 */
public class BotException extends RuntimeException {

    public BotException(String message) {
        super(message);
    }

    public BotException(String message, Throwable cause) {
        super(message, cause);
    }
}
