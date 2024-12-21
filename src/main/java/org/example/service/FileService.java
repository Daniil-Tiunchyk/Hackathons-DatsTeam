package org.example.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileService {
    public void saveToFile(String content, String path) throws IOException {
        Files.createDirectories(Paths.get(path).getParent());
        Files.write(Paths.get(path), content.getBytes());
        System.out.println("[INFO] JSON сохранён в " + path);
    }
}
