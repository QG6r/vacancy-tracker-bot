package ru.skillbox.vacancybot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Критерии поиска вакансий. Любое поле может быть null —
 * это значит "не учитывать при поиске".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilter {

    /** Код региона России, например 78 — Санкт-Петербург. */
    private Integer regionCode;

    /** Минимальный требуемый опыт в годах. */
    private Integer minExperience;

    /**
     * Минимальная зарплата в рублях. API агрегатора её не поддерживает,
     * фильтрация выполняется на нашей стороне.
     */
    private Integer minSalary;

    /** Ключевые слова для поиска. */
    private String keyword;

    /** @JsonIgnore обязателен: иначе Jackson примет это за поле "empty". */
    @JsonIgnore
    public boolean isEmpty() {
        return regionCode == null && minExperience == null
                && minSalary == null && (keyword == null || keyword.isBlank());
    }
}
