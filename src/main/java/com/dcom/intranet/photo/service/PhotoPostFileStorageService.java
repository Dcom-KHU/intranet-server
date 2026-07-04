package com.dcom.intranet.photo.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PhotoPostFileStorageService {

    private final Path uploadRoot;

    public PhotoPostFileStorageService(
            @Value("${file.photo-upload-dir:./uploads/photo}") String uploadDir
    ) {
        this.uploadRoot = Path.of(uploadDir);
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "업로드할 파일이 비어 있습니다."
            );
        }

        String originalFileName = file.getOriginalFilename();
        String extension = extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + extension;

        LocalDate now = LocalDate.now();
        Path directory = uploadRoot
                .resolve(String.valueOf(now.getYear()))
                .resolve(String.format("%02d", now.getMonthValue()));

        try {
            Files.createDirectories(directory);

            Path targetPath = directory.resolve(storedFileName);
            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            String fileUrl = targetPath.toString();

            return new StoredFile(
                    originalFileName,
                    storedFileName,
                    uploadRoot.relativize(targetPath).toString(),
                    fileUrl,
                    file.getSize(),
                    file.getContentType()
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "사진 파일 저장 중 오류가 발생했습니다."
            );
        }
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }

        return originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(Path.of(fileUrl));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "사진 파일 삭제 중 오류가 발생했습니다."
            );
        }
    }

    @Getter
    public static class StoredFile {

        private final String originalFileName;
        private final String storedFileName;
        private final String objectKey;
        private final String fileUrl;
        private final Long fileSize;
        private final String contentType;

        public StoredFile(
                String originalFileName,
                String storedFileName,
                String objectKey,
                String fileUrl,
                Long fileSize,
                String contentType
        ) {
            this.originalFileName = originalFileName;
            this.storedFileName = storedFileName;
            this.objectKey = objectKey;
            this.fileUrl = fileUrl;
            this.fileSize = fileSize;
            this.contentType = contentType;
        }
    }
}
