package ru.skillbox.vacancybot.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Конфигурация приложения")
class BotConfigTest {

    @Test
    @DisplayName("читает настройки из application.properties")
    void loadsFromProperties() {
        BotConfig config = BotConfig.load();

        assertThat(config.getBotName()).isNotBlank();
        assertThat(config.getBotToken()).isNotBlank();
        assertThat(config.getStoragePath()).isNotBlank();
    }
}