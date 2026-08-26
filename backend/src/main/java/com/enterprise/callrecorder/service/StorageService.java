package com.enterprise.callrecorder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Service pour le stockage des fichiers
 */
@Service
@RequiredArgsConstructor
public class StorageService {

    private static final String STORAGE_PATH = "./recordings";

    /**
     * Sauvegarder un fichier
     */
    public String saveFile(byte[] fileContent, String fileName) throws Exception {
        File directory = new File(STORAGE_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filePath = STORAGE_PATH + File.separator + fileName;
        Files.write(Paths.get(filePath), fileContent);

        return fileName;
    }

    /**
     * Récupérer un fichier
     */
    public byte[] getFile(String storageKey) throws Exception {
        String filePath = STORAGE_PATH + File.separator + storageKey;
        return Files.readAllBytes(Paths.get(filePath));
    }

    /**
     * Supprimer un fichier
     */
    public boolean deleteFile(String storageKey) {
        try {
            String filePath = STORAGE_PATH + File.separator + storageKey;
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (Exception e) {
            return false;
        }
    }
}
