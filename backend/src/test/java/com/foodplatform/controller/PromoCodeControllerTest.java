package com.foodplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodplatform.dto.PromoCodeRequest;
import com.foodplatform.dto.PromoValidationResponse;
import com.foodplatform.model.PromoCode;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.PromoCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PromoCodeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class PromoCodeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private PromoCodeService promoCodeService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "admin1")
    void createPromoCode_returns200() throws Exception {
        PromoCodeRequest request = new PromoCodeRequest("NEWCODE", "PERCENTAGE", 15, 20, 50, null, null);
        PromoCode promo = new PromoCode("p1", "NEWCODE", PromoCode.DiscountType.PERCENTAGE, 15, 20, 50, 0,
                LocalDateTime.now().plusMonths(1), true, null, "admin1", LocalDateTime.now());

        when(promoCodeService.createPromoCode(any(PromoCodeRequest.class), eq("admin1"))).thenReturn(promo);

        mockMvc.perform(post("/api/promo-codes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("NEWCODE"));
    }

    @Test
    @WithMockUser(username = "cust1")
    void validatePromoCode_returns200() throws Exception {
        PromoValidationResponse response = new PromoValidationResponse(true, 10.0, "Promo applied!");
        when(promoCodeService.validatePromoCode(eq("WELCOME10"), eq(100.0), eq("rest1")))
                .thenReturn(response);

        Map<String, Object> body = Map.of("code", "WELCOME10", "orderAmount", 100.0, "restaurantId", "rest1");

        mockMvc.perform(post("/api/promo-codes/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.discountAmount").value(10.0));
    }

    @Test
    @WithMockUser(username = "admin1")
    void getPromoCodes_returns200() throws Exception {
        PromoCode promo = new PromoCode("p1", "WELCOME10", PromoCode.DiscountType.PERCENTAGE, 10, 15, 100, 0,
                LocalDateTime.now().plusMonths(3), true, null, "admin1", LocalDateTime.now());
        when(promoCodeService.getPromoCodesByCreator("admin1")).thenReturn(List.of(promo));

        mockMvc.perform(get("/api/promo-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("WELCOME10"));
    }

    @Test
    @WithMockUser(username = "admin1")
    void deactivatePromoCode_returns200() throws Exception {
        mockMvc.perform(delete("/api/promo-codes/p1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
