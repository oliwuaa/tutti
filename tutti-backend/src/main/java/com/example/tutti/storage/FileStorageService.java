package com.example.tutti.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootDir = Paths.get("./uploads").toAbsolutePath().normalize();

    public String saveFile(MultipartFile file, String subDirectory) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Plik jest pusty.");

        Path uploadPath = rootDir.resolve(subDirectory).normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/files/" + subDirectory + "/" + uniqueFileName;
    }

    public byte[] loadFile(String httpPath) throws IOException {
        Path filePath = resolveHttpPathToStaticPath(httpPath);
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("Plik nie istnieje: " + httpPath);
        }
        return Files.readAllBytes(filePath);
    }

    public void deleteFile(String httpPath) {
        if (httpPath == null || !httpPath.startsWith("/files/")) return;
        try {
            Path fileToDelete = resolveHttpPathToStaticPath(httpPath);
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.err.println("Nie udało się usunąć pliku: " + httpPath);
        }
    }

    private Path resolveHttpPathToStaticPath(String httpPath) {
        String relativePath = httpPath.replace("/files/", "");
        return rootDir.resolve(relativePath).normalize();
    }
}