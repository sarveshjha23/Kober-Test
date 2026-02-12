package com.koerber.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koerber.order.dto.OrderRequest;
import com.koerber.order.dto.OrderResponse;
import com.koerber.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void testPlaceOrder_Success() throws Exception {
        // Arrange
        OrderRequest request = new OrderRequest(1001L, 20);
        OrderResponse response = new OrderResponse(
                100L,
                1001L,
                "Laptop",
                20,
                "PLACED",
                Collections.singletonList(1L),
                "Order placed. Inventory reserved."
        );

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.productId").value(1001))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(20))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.message").value("Order placed. Inventory reserved."));
    }

    @Test
    void testPlaceOrder_InsufficientInventory() throws Exception {
        // Arrange
        OrderRequest request = new OrderRequest(1001L, 200);

        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Insufficient inventory. Available: 80, Requested: 200"));

        // Act & Assert
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient inventory. Available: 80, Requested: 200"));
    }

    @Test
    void testPlaceOrder_ServiceError() throws Exception {
        // Arrange
        OrderRequest request = new OrderRequest(1001L, 20);

        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new RuntimeException("Failed to communicate with Inventory Service"));

        // Act & Assert
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }
}

