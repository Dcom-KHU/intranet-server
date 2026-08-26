package com.dcom.intranet.photo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PhotoPostFileStorageServiceTest {

    private static final byte[] JPEG_BYTES = {
            (byte) 0xFF,
            (byte) 0xD8,
            (byte) 0xFF,
            (byte) 0xD9
    };

    @TempDir
    private Path uploadRoot;

    @Test
    @DisplayName("HEIC/HEIF 이미지는 원본 확장자와 무관하게 JPEG로 변환 저장한다")
    void storesHeifImageAsJpeg() throws Exception {
        Path converter = fakeHeifConverter();
        PhotoPostFileStorageService storageService = new PhotoPostFileStorageService(
                uploadRoot.toString(),
                converter.toString()
        );
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "iphone-photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                heifBytes()
        );

        PhotoPostFileStorageService.StoredFile storedFile = storageService.store(file);

        Path storedPath = uploadRoot.resolve(storedFile.getObjectKey());
        assertThat(storedFile.getOriginalFileName()).isEqualTo("iphone-photo.jpg");
        assertThat(storedFile.getStoredFileName()).endsWith(".jpg");
        assertThat(storedFile.getContentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
        assertThat(storedFile.getFileSize()).isEqualTo((long) JPEG_BYTES.length);
        assertThat(Files.readAllBytes(storedPath)).isEqualTo(JPEG_BYTES);
    }

    @Test
    @DisplayName("일반 이미지는 기존 확장자와 Content-Type으로 그대로 저장한다")
    void storesNormalImageWithoutConversion() throws Exception {
        PhotoPostFileStorageService storageService = new PhotoPostFileStorageService(
                uploadRoot.toString(),
                "unused"
        );
        byte[] pngBytes = {1, 2, 3, 4};
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "photo.png",
                MediaType.IMAGE_PNG_VALUE,
                pngBytes
        );

        PhotoPostFileStorageService.StoredFile storedFile = storageService.store(file);

        Path storedPath = uploadRoot.resolve(storedFile.getObjectKey());
        assertThat(storedFile.getStoredFileName()).endsWith(".png");
        assertThat(storedFile.getContentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);
        assertThat(storedFile.getFileSize()).isEqualTo((long) pngBytes.length);
        assertThat(Files.readAllBytes(storedPath)).isEqualTo(pngBytes);
    }

    private Path fakeHeifConverter() throws Exception {
        Path converter = uploadRoot.resolve("fake-heif-convert.sh");
        Files.writeString(
                converter,
                "#!/bin/sh\nprintf '\\377\\330\\377\\331' > \"$2\"\n"
        );
        assertThat(converter.toFile().setExecutable(true)).isTrue();
        return converter;
    }

    private byte[] heifBytes() {
        return new byte[]{
                0, 0, 0, 24,
                'f', 't', 'y', 'p',
                'h', 'e', 'i', 'c',
                0, 0, 0, 0,
                'm', 'i', 'f', '1',
                'h', 'e', 'i', 'c'
        };
    }
}
