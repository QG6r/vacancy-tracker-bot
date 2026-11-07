package ru.skillbox.vacancybot.exception;

/**
 * Пользователь ввёл некорректные данные: не число там, где ожидалось
 * число, несуществующий код региона, неверный формат времени и т.п.
 * <p>
 * Сообщение этого исключения показывается пользователю, поэтому
 * оно должно быть понятным человеку, а не разработчику.
 */
public class InvalidInputException extends BotException {

    public InvalidInputException(String message) {
        super(message);
    }
}
