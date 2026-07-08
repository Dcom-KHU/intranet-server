package com.dcom.intranet.archive.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ArchiveFileStorageService {

    private static final String PUBLIC_UPLOAD_PREFIX = "/uploads/archive/";

    private final Path uploadRoot;

    public ArchiveFileStorageService(
            @Value("${file.archive-upload-dir:./uploads/archive}") String uploadDir
    ) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
        }

        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + extension;

            LocalDate now = LocalDate.now();

            String objectKey = "archive/"
                    + now.getYear() + "/"
                    + String.format("%02d", now.getMonthValue()) + "/"
                    + storedFileName;

            Path directoryPath = uploadRoot
                    .resolve(String.valueOf(now.getYear()))
                    .resolve(String.format("%02d", now.getMonthValue()));

            Files.createDirectories(directoryPath);

            Path filePath = directoryPath.resolve(storedFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(
                    originalFileName,
                    storedFileName,
                    objectKey,
                    "/uploads/" + objectKey,
                    file.getSize(),
                    file.getContentType()
            );

        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    private String getExtension(String originalFileName) {
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }
        return originalFileName.substring(dotIndex);
    }

    @Getter
    public static class StoredFile {
        private final String originalFileName;
        private final String storedFileName;
        private final String objectKey;
        private final String fileUrl;
        private final Long fileSize;
        private final String contentType;

        public StoredFile(String originalFileName, String storedFileName, String objectKey,
                          String fileUrl, Long fileSize, String contentType) {
            this.originalFileName = originalFileName;
            this.storedFileName = storedFileName;
            this.objectKey = objectKey;
            this.fileUrl = fileUrl;
            this.fileSize = fileSize;
            this.contentType = contentType;
        }
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolvePath(fileUrl));
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }

    public Resource loadAsResource(String fileUrl) {
        try {
            Path filePath = resolvePath(fileUrl);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "파일을 읽을 수 없습니다."
                );
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "파일 경로가 올바르지 않습니다."
            );
        }
    }

    private Path resolvePath(String fileUrl) {
        if (fileUrl.startsWith(PUBLIC_UPLOAD_PREFIX)) {
            String relativePath = fileUrl.substring(PUBLIC_UPLOAD_PREFIX.length());
            Path resolvedPath = uploadRoot.resolve(relativePath).normalize();

            if (!resolvedPath.startsWith(uploadRoot)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "파일 경로가 올바르지 않습니다."
                );
            }

            return resolvedPath;
        }

        return Path.of(fileUrl).normalize();
    }
}
