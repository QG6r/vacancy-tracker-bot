package ru.skillbox.vacancybot.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfig {

    private static final String CONFIG_FILE = "application.properties";

    private final String botName;
    private final String botToken;
    private final String storagePath;

    private BotConfig(String botName, String botToken, String storagePath) {
        this.botName = botName;
        this.botToken = botToken;
        this.storagePath = storagePath;
    }

    public static BotConfig load() {
        Properties properties = readProperties();

        String name = resolve("BOT_NAME", properties.getProperty("bot.name"));
        String token = resolve("BOT_TOKEN", properties.getProperty("bot.token"));
        String storage = resolve("STORAGE_PATH", properties.getProperty("storage.path", "data"));

        if (isBlank(name) || isBlank(token)) {
            throw new IllegalStateException(
                    "Не заданы bot.name и bot.token. Скопируйте application.properties.example "
                            + "в application.properties и подставьте значения от @BotFather.");
        }

        return new BotConfig(name, token, storage);
    }

    private static Properties readProperties() {
        Properties properties = new Properties();
        try (InputStream input = BotConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + CONFIG_FILE, e);
        }
        return properties;
    }

    private static String resolve(String envKey, String fromFile) {
        String fromEnv = System.getenv(envKey);
        return isBlank(fromEnv) ? fromFile : fromEnv;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String getBotName() {
        return botName;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getStoragePath() {
        return storagePath;
    }
}
