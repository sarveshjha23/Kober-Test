package com.koerber.inventory.controller;

import com.koerber.inventory.dto.BatchDTO;
import com.koerber.inventory.dto.InventoryResponse;
import com.koerber.inventory.dto.InventoryUpdateRequest;
import com.koerber.inventory.dto.InventoryUpdateResponse;
import com.koerber.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void testGetInventory_Success() throws Exception {
        // Arrange
        InventoryResponse response = new InventoryResponse(
                1001L,
                "Laptop",
                Arrays.asList(
                        new BatchDTO(1L, 50, LocalDate.of(2026, 6, 25)),
                        new BatchDTO(2L, 30, LocalDate.of(2026, 9, 15))
                )
        );
        when(inventoryService.getInventoryByProductId(1001L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/inventory/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1001))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.batches.length()").value(2));
    }

    @Test
    void testGetInventory_NotFound() throws Exception {
        // Arrange
        when(inventoryService.getInventoryByProductId(9999L))
                .thenThrow(new IllegalArgumentException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/inventory/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateInventory_Success() throws Exception {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest(1001L, 20, null);
        InventoryUpdateResponse response = new InventoryUpdateResponse(true, "Inventory updated successfully");
        when(inventoryService.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inventory updated successfully"));
    }

    @Test
    void testUpdateInventory_Failure() throws Exception {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest(1001L, 200, null);
        InventoryUpdateResponse response = new InventoryUpdateResponse(false, "Insufficient inventory");
        when(inventoryService.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

