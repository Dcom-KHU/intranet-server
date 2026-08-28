package com.dcom.intranet.photo.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

@Service
public class PhotoPostFileStorageService {

    private static final String PUBLIC_UPLOAD_PREFIX = "/uploads/photo/";
    private static final String JPEG_EXTENSION = ".jpg";
    private static final int HEIF_HEADER_READ_LIMIT = 64;
    private static final byte[] FTYP_SIGNATURE = {'f', 't', 'y', 'p'};
    private static final byte[][] HEIF_BRANDS = {
            {'h', 'e', 'i', 'c'},
            {'h', 'e', 'i', 'x'},
            {'h', 'e', 'v', 'c'},
            {'h', 'e', 'v', 'x'},
            {'h', 'e', 'i', 'm'},
            {'h', 'e', 'i', 's'},
            {'h', 'e', 'v', 'm'},
            {'h', 'e', 'v', 's'},
            {'m', 'i', 'f', '1'},
            {'m', 's', 'f', '1'}
    };

    private final Path uploadRoot;
    private final String heicConverterCommand;

    public PhotoPostFileStorageService(
            @Value("${file.photo-upload-dir:./uploads/photo}") String uploadDir,
            @Value("${file.heic-converter-command:heif-convert}") String heicConverterCommand
    ) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        this.heicConverterCommand = normalizeConverterCommand(heicConverterCommand);
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "업로드할 파일이 비어 있습니다."
            );
        }

        String originalFileName = file.getOriginalFilename();
        boolean heifImage = isHeifImage(file);
        String extension = heifImage ? JPEG_EXTENSION : extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + extension;

        LocalDate now = LocalDate.now();
        Path directory = uploadRoot
                .resolve(String.valueOf(now.getYear()))
                .resolve(String.format("%02d", now.getMonthValue()));

        try {
            Files.createDirectories(directory);

            Path targetPath = directory.resolve(storedFileName);
            if (heifImage) {
                convertHeifToJpeg(file, directory, targetPath);
            } else {
                Files.copy(
                        file.getInputStream(),
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            String objectKey = toRelativePath(targetPath);
            long fileSize = Files.size(targetPath);

            return new StoredFile(
                    originalFileName,
                    storedFileName,
                    objectKey,
                    PUBLIC_UPLOAD_PREFIX + objectKey,
                    fileSize,
                    heifImage ? MediaType.IMAGE_JPEG_VALUE : file.getContentType()
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

    private String normalizeConverterCommand(String command) {
        if (command == null || command.isBlank()) {
            return "heif-convert";
        }

        return command.trim();
    }

    private boolean isHeifImage(MultipartFile file) {
        if (hasHeifExtension(file.getOriginalFilename()) || hasHeifContentType(file.getContentType())) {
            return true;
        }

        try {
            try (InputStream inputStream = file.getInputStream()) {
                return hasHeifSignature(inputStream.readNBytes(HEIF_HEADER_READ_LIMIT));
            }
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "사진 파일 확인 중 오류가 발생했습니다."
            );
        }
    }

    private boolean hasHeifExtension(String originalFileName) {
        String extension = extractExtension(originalFileName);
        return ".heic".equalsIgnoreCase(extension) || ".heif".equalsIgnoreCase(extension);
    }

    private boolean hasHeifContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }

        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        return normalizedContentType.equals("image/heic")
                || normalizedContentType.equals("image/heif")
                || normalizedContentType.equals("image/heic-sequence")
                || normalizedContentType.equals("image/heif-sequence");
    }

    private boolean hasHeifSignature(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }

        int limit = Math.min(bytes.length, HEIF_HEADER_READ_LIMIT);
        if (!matches(bytes, 4, FTYP_SIGNATURE)) {
            return false;
        }

        for (int index = 8; index <= limit - 4; index += 4) {
            for (byte[] brand : HEIF_BRANDS) {
                if (matches(bytes, index, brand)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matches(byte[] source, int offset, byte[] expected) {
        if (source.length < offset + expected.length) {
            return false;
        }

        return Arrays.equals(
                Arrays.copyOfRange(source, offset, offset + expected.length),
                expected
        );
    }

    private void convertHeifToJpeg(MultipartFile file, Path directory, Path targetPath) {
        Path sourcePath = null;

        try {
            sourcePath = Files.createTempFile(directory, "heif-upload-", extractExtension(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), sourcePath, StandardCopyOption.REPLACE_EXISTING);

            Process process = new ProcessBuilder(
                    heicConverterCommand,
                    sourcePath.toString(),
                    targetPath.toString()
            )
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            int exitCode = process.waitFor();

            if (exitCode != 0 || !Files.exists(targetPath)) {
                Files.deleteIfExists(targetPath);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "HEIC/HEIF 이미지 변환 중 오류가 발생했습니다."
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "HEIC/HEIF 이미지 변환이 중단되었습니다."
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "HEIC/HEIF 이미지 변환 도구를 실행할 수 없습니다."
            );
        } finally {
            if (sourcePath != null) {
                try {
                    Files.deleteIfExists(sourcePath);
                } catch (IOException ignored) {
                    // 임시 파일 삭제 실패는 업로드 결과에 영향을 주지 않는다.
                }
            }
        }
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolvePath(fileUrl));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "사진 파일 삭제 중 오류가 발생했습니다."
            );
        }
    }

    public Resource loadAsResource(String fileUrl) {
        try {
            Resource resource = new UrlResource(resolvePath(fileUrl).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 읽을 수 없습니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 경로가 올바르지 않습니다.");
        }
    }

    private String toRelativePath(Path targetPath) {
        return uploadRoot.relativize(targetPath)
                .toString()
                .replace('\\', '/');
    }

    private Path resolvePath(String fileUrl) {
        if (fileUrl.startsWith(PUBLIC_UPLOAD_PREFIX)) {
            Path resolvedPath = uploadRoot
                    .resolve(fileUrl.substring(PUBLIC_UPLOAD_PREFIX.length()))
                    .normalize();

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
