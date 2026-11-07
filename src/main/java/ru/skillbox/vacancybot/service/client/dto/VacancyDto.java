package ru.skillbox.vacancybot.service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VacancyDto {

    private String id;

    @JsonProperty("job-name")
    private String jobName;

    @JsonProperty("salary_min")
    private Integer salaryMin;

    @JsonProperty("salary_max")
    private Integer salaryMax;

    @JsonProperty("vac_url")
    private String vacUrl;

    private CompanyDto company;

    private RegionDto region;

    private RequirementsDto requirement;
}