package ru.skillbox.vacancybot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVacancies {

    private Long userId;

    @Builder.Default
    private List<Vacancy> vacancies = new ArrayList<>();
}