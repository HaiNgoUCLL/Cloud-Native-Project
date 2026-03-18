package com.foodplatform.controller;

import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = FileController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class FileControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private FileStorageService fileStorageService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "owner1")
    void uploadFile_validImage_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3, 4, 5});
        when(fileStorageService.uploadFile(any())).thenReturn("/uploads/uuid-test.jpg");

        mockMvc.perform(multipart("/api/files/upload").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("/uploads/uuid-test.jpg"));
    }

    @Test
    @WithMockUser(username = "owner1")
    void uploadFile_invalidType_returnsError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});
        when(fileStorageService.uploadFile(any()))
                .thenThrow(new RuntimeException("Only JPEG, PNG, WebP, and GIF images are allowed"));

        mockMvc.perform(multipart("/api/files/upload").file(file).with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
