package ru.skillbox.vacancybot.service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionDto {

    @JsonProperty("region_code")
    private String regionCode;

    private String name;
}