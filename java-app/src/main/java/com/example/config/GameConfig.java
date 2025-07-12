package com.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Управляет конфигурацией игры, загружая настройки из файла properties.
 */
public final class GameConfig {

    private final String apiBaseUrl;
    private final String apiToken;
    private final boolean fighterUseWorkerLogic;

    public GameConfig() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Не удалось найти application.properties");
                throw new IllegalStateException("Файл конфигурации не найден");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Ошибка при загрузке конфигурации", ex);
        }
        this.apiBaseUrl = properties.getProperty("api.base.url");
        this.apiToken = properties.getProperty("api.token");
        this.fighterUseWorkerLogic = Boolean.parseBoolean(properties.getProperty("strategy.fighter.use_worker_logic", "false"));
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public boolean isFighterUseWorkerLogic() {
        return fighterUseWorkerLogic;
    }
}
