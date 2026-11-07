package ru.skillbox.vacancybot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * Настройки нотификации: во сколько присылать вакансии
 * и в каком часовом поясе задано это время.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {

    /** Время нотификации в часовом поясе пользователя. */
    private LocalTime notificationTime;

    /** Смещение от UTC. По умолчанию UTC, пока пользователь не задал своё. */
    @Builder.Default
    private ZoneOffset zoneOffset = ZoneOffset.UTC;

    @JsonIgnore
    public boolean isConfigured() {
        return notificationTime != null;
    }
}
