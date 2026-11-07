package ru.skillbox.vacancybot.exception;

public class ApiException extends BotException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}