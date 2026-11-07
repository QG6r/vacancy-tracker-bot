package ru.skillbox.vacancybot.service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrudvsemResponse {

    private String status;
    private ResultsDto results;
}