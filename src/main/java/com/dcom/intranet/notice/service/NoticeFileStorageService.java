package com.dcom.intranet.notice.service;

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
public class NoticeFileStorageService {

    private final Path uploadRoot;

    public NoticeFileStorageService(
            @Value("${file.notice-upload-dir:./uploads/notice}") String uploadDir
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

            return new StoredFile(
                    originalFileName,
                    targetPath.toString()
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "공지사항 첨부파일 저장 중 오류가 발생했습니다."
            );
        }
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
                    "공지사항 첨부파일 삭제 중 오류가 발생했습니다."
            );
        }
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }

        return originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    @Getter
    public static class StoredFile {

        private final String fileName;
        private final String fileUrl;

        public StoredFile(String fileName, String fileUrl) {
            this.fileName = fileName;
            this.fileUrl = fileUrl;
        }
    }
}
