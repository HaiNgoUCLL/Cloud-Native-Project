package com.foodplatform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class LocalFileStorageServiceTest {

    private LocalFileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    void uploadFile_validImage_returnsUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3, 4, 5});

        String url = fileStorageService.uploadFile(file);

        assertThat(url).startsWith("/uploads/");
        assertThat(url).endsWith(".jpg");
        // Verify file actually exists
        String filename = url.replace("/uploads/", "");
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    void uploadFile_pngImage_returnsUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", new byte[]{1, 2, 3});

        String url = fileStorageService.uploadFile(file);

        assertThat(url).startsWith("/uploads/");
        assertThat(url).endsWith(".png");
    }

    @Test
    void uploadFile_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.uploadFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("File is empty");
    }

    @Test
    void uploadFile_tooLarge_throws() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", largeContent);

        assertThatThrownBy(() -> fileStorageService.uploadFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("File size exceeds 5MB limit");
    }

    @Test
    void uploadFile_invalidType_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> fileStorageService.uploadFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only JPEG, PNG, WebP, and GIF images are allowed");
    }
}
