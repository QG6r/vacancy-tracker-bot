package ru.skillbox.vacancybot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotUser {

    private Long id;

    private Long chatId;

    @Builder.Default
    private SearchFilter searchFilter = new SearchFilter();

    @Builder.Default
    private NotificationSettings notificationSettings = new NotificationSettings();

    private boolean searchActive;

    private LocalDateTime lastCheckedAt;
}
