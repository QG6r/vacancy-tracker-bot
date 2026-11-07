package ru.skillbox.vacancybot.presentation.dto;

/**
 * Разобранный запрос пользователя.
 * <p>
 * Объект Update из библиотеки Telegram имеет громоздкую структуру, и работать
 * с ним напрямую в командах неудобно. Поэтому сразу после получения он
 * превращается в этот простой DTO, содержащий только то, что нам нужно.
 *
 * @param userId    ID пользователя в Telegram (уникален, используется как ключ хранилища)
 * @param chatId    ID чата — нужен, чтобы отправить ответ
 * @param command   команда без слэша: "start", "menu", "region". Для нажатия
 *                  кнопки сюда попадает её callback-имя
 * @param argument  всё, что шло после команды ("/region 52" → "52"). Пустая
 *                  строка, если аргумента не было
 * @param messageId ID сообщения — понадобится, чтобы редактировать клавиатуру
 * @param callback  true, если это нажатие на кнопку, а не введённая команда
 */
public record UserRequest(
        Long userId,
        Long chatId,
        String command,
        String argument,
        Integer messageId,
        boolean callback
) {

    public boolean hasArgument() {
        return argument != null && !argument.isBlank();
    }
}
