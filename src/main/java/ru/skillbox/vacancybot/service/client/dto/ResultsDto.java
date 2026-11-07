package ru.skillbox.vacancybot.service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsDto {

    private List<VacancyWrapperDto> vacancies = new ArrayList<>();
}