package com.koerber.inventory.service;

import com.koerber.inventory.dto.InventoryResponse;
import com.koerber.inventory.dto.InventoryUpdateRequest;
import com.koerber.inventory.dto.InventoryUpdateResponse;
import com.koerber.inventory.entity.InventoryBatch;
import com.koerber.inventory.factory.InventoryHandlerFactory;
import com.koerber.inventory.factory.FIFOInventoryHandler;
import com.koerber.inventory.repository.InventoryBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @Mock
    private InventoryHandlerFactory handlerFactory;

    @InjectMocks
    private InventoryService inventoryService;

    private List<InventoryBatch> testBatches;

    @BeforeEach
    void setUp() {
        testBatches = Arrays.asList(
                new InventoryBatch(1L, 1001L, "Laptop", 50, LocalDate.of(2026, 6, 25)),
                new InventoryBatch(2L, 1001L, "Laptop", 30, LocalDate.of(2026, 9, 15))
        );
    }

    @Test
    void testGetInventoryByProductId_Success() {
        // Arrange
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1001L))
                .thenReturn(testBatches);

        // Act
        InventoryResponse response = inventoryService.getInventoryByProductId(1001L);

        // Assert
        assertNotNull(response);
        assertEquals(1001L, response.getProductId());
        assertEquals("Laptop", response.getProductName());
        assertEquals(2, response.getBatches().size());
        verify(inventoryBatchRepository, times(1)).findByProductIdOrderByExpiryDateAsc(1001L);
    }

    @Test
    void testGetInventoryByProductId_ProductNotFound() {
        // Arrange
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(9999L))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.getInventoryByProductId(9999L);
        });
    }

    @Test
    void testReserveInventory_Success() {
        // Arrange
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1001L))
                .thenReturn(testBatches);
        when(handlerFactory.getDefaultHandler()).thenReturn(new FIFOInventoryHandler());
        when(inventoryBatchRepository.saveAll(any())).thenReturn(testBatches);

        // Act
        List<Long> reservedBatchIds = inventoryService.reserveInventory(1001L, 60);

        // Assert
        assertNotNull(reservedBatchIds);
        assertEquals(2, reservedBatchIds.size());
        assertTrue(reservedBatchIds.contains(1L));
        assertTrue(reservedBatchIds.contains(2L));
        verify(inventoryBatchRepository, times(1)).saveAll(any());
    }

    @Test
    void testReserveInventory_InsufficientStock() {
        // Arrange
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1001L))
                .thenReturn(testBatches);
        when(handlerFactory.getDefaultHandler()).thenReturn(new FIFOInventoryHandler());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.reserveInventory(1001L, 100);
        });
    }

    @Test
    void testUpdateInventory_Success() {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest(1001L, 20, null);
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1001L))
                .thenReturn(testBatches);
        when(handlerFactory.getDefaultHandler()).thenReturn(new FIFOInventoryHandler());
        when(inventoryBatchRepository.saveAll(any())).thenReturn(testBatches);

        // Act
        InventoryUpdateResponse response = inventoryService.updateInventory(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Inventory updated successfully", response.getMessage());
    }

    @Test
    void testUpdateInventory_Failure() {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest(1001L, 200, null);
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1001L))
                .thenReturn(testBatches);
        when(handlerFactory.getDefaultHandler()).thenReturn(new FIFOInventoryHandler());

        // Act
        InventoryUpdateResponse response = inventoryService.updateInventory(request);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Insufficient inventory"));
    }
}

