package ru.skillbox.vacancybot.presentation.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Сборка экранных клавиатур.
 * <p>
 * Вынесено отдельно, чтобы команды не занимались вёрсткой кнопок,
 * а разметка не дублировалась между ними.
 * <p>
 * В callbackData кладём строку "команда:аргумент" — UpdateParser разберёт
 * её обратно, и нажатие кнопки попадёт в ту же команду, что и ввод текста.
 */
public class KeyboardFactory {

    /** Клавиатура из одной кнопки в строку. */
    public InlineKeyboardMarkup singleColumn(List<Button> buttons) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (Button button : buttons) {
            rows.add(new InlineKeyboardRow(toInline(button)));
        }
        return new InlineKeyboardMarkup(rows);
    }

    /** Одна кнопка — удобно для коротких ответов вроде "Menu". */
    public InlineKeyboardMarkup single(Button button) {
        return singleColumn(List.of(button));
    }

    /**
     * Строка из нескольких кнопок рядом — например "Да" / "Нет"
     * или "Предыдущие 10" / "Следующие 10".
     */
    public InlineKeyboardMarkup singleRow(List<Button> buttons) {
        InlineKeyboardRow row = new InlineKeyboardRow();
        for (Button button : buttons) {
            row.add(toInline(button));
        }
        return new InlineKeyboardMarkup(List.of(row));
    }

    /**
     * Список кнопок в столбик плюс отдельная строка навигации внизу.
     * Используется для постраничного списка регионов.
     *
     * @param items      кнопки основного списка
     * @param navigation кнопки навигации (могут быть пустыми)
     */
    public InlineKeyboardMarkup withNavigation(List<Button> items, List<Button> navigation) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (Button button : items) {
            rows.add(new InlineKeyboardRow(toInline(button)));
        }
        if (!navigation.isEmpty()) {
            InlineKeyboardRow navigationRow = new InlineKeyboardRow();
            for (Button button : navigation) {
                navigationRow.add(toInline(button));
            }
            rows.add(navigationRow);
        }
        return new InlineKeyboardMarkup(rows);
    }

    private InlineKeyboardButton toInline(Button button) {
        return InlineKeyboardButton.builder()
                .text(button.text())
                .callbackData(button.callbackData())
                .build();
    }

    /** Описание одной кнопки: что написано и что отправится при нажатии. */
    public record Button(String text, String callbackData) {

        public static Button of(String text, String command) {
            return new Button(text, command);
        }

        public static Button of(String text, String command, String argument) {
            return new Button(text, command + ":" + argument);
        }
    }
}
