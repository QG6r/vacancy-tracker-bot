package ru.skillbox.vacancybot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {

    private String id;
    private String jobName;
    private String companyName;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer experience;
    private String url;
    private String regionName;
}