package ru.skillbox.vacancybot.service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequirementsDto {

    private Integer experience;
    private String education;
}