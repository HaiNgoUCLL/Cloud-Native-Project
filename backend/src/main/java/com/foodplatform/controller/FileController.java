package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.uploadFile(file);
        return ResponseEntity.ok(ApiResponse.success(url, "File uploaded successfully"));
    }
}
