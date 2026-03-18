package com.foodplatform.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_withMessage_setsFields() {
        ApiResponse<String> response = ApiResponse.success("data", "OK");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("data");
        assertThat(response.getMessage()).isEqualTo("OK");
    }

    @Test
    void success_withoutMessage_defaultsMessage() {
        ApiResponse<String> response = ApiResponse.success("data");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Success");
    }

    @Test
    void error_setsFields() {
        ApiResponse<String> response = ApiResponse.error("Failed");
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("Failed");
    }
}
