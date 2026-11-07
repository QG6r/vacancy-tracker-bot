package ru.skillbox.vacancybot.model;

/**
 * Регион России: код, используемый в API агрегатора, и название.
 *
 * @param code код региона (78 — Санкт-Петербург)
 * @param name название для отображения пользователю
 */
public record Region(int code, String name) {
}
