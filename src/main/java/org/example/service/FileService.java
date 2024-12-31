package org.example.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/* ---------------------------------------------------
 * FileService — для сохранения данных в файл
 * --------------------------------------------------- */
public class FileService {
    private static final Logger logger = Logger.getLogger(FileService.class.getName());

    public void saveToFile(String content, String filePath) {
        try {
            Files.writeString(Path.of(filePath), content);
            logger.info("[FileService] Данные успешно сохранены в файл: " + filePath);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[FileService] Ошибка при сохранении в файл: " + e.getMessage(), e);
        }
    }
}
