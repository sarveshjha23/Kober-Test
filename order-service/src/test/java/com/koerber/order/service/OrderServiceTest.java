package com.koerber.order.service;

import com.koerber.order.client.InventoryClient;
import com.koerber.order.dto.*;
import com.koerber.order.entity.Order;
import com.koerber.order.entity.OrderStatus;
import com.koerber.order.repository.OrderRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    private InventoryResponse inventoryResponse;
    private InventoryUpdateResponse inventoryUpdateResponse;

    @BeforeEach
    void setUp() {
        List<BatchDTO> batches = Arrays.asList(
                new BatchDTO(1L, 50, LocalDate.of(2026, 6, 25)),
                new BatchDTO(2L, 30, LocalDate.of(2026, 9, 15))
        );

        inventoryResponse = new InventoryResponse(1001L, "Laptop", batches);

        inventoryUpdateResponse = new InventoryUpdateResponse();
        inventoryUpdateResponse.setSuccess(true);
        inventoryUpdateResponse.setMessage("Inventory updated successfully");
        inventoryUpdateResponse.setReservedFromBatchIds(Collections.singletonList(1L));
    }

    @Test
    void testPlaceOrder_Success() {
        // Arrange
        OrderRequest request = new OrderRequest(1001L, 20);

        when(inventoryClient.checkInventory(1001L)).thenReturn(inventoryResponse);
        when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class)))
                .thenReturn(inventoryUpdateResponse);

        Order savedOrder = new Order();
        savedOrder.setOrderId(100L);
        savedOrder.setProductId(1001L);
        savedOrder.setProductName("Laptop");
        savedOrder.setQuantity(20);
        savedOrder.setStatus(OrderStatus.PLACED);
        savedOrder.setOrderDate(LocalDate.now());
        savedOrder.setReservedFromBatchIds("1");

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());
        assertEquals(1001L, response.getProductId());
        assertEquals("Laptop", response.getProductName());
        assertEquals(20, response.getQuantity());
        assertEquals("PLACED", response.getStatus());
        assertEquals("Order placed. Inventory reserved.", response.getMessage());

        verify(inventoryClient, times(1)).checkInventory(1001L);
        verify(inventoryClient, times(1)).updateInventory(any(InventoryUpdateRequest.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_InsufficientInventory() {
        // Arrange
        OrderRequest request = new OrderRequest(1001L, 100);

        when(inventoryClient.checkInventory(1001L)).thenReturn(inventoryResponse);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.placeOrder(request);
        });

        verify(inventoryClient, times(1)).checkInventory(1001L);
        verify(inventoryClient, never()).updateInventory(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testPlaceOrder_ProductNotFound() {
        // Arrange
        OrderRequest request = new OrderRequest(9999L, 10);

        when(inventoryClient.checkInventory(9999L))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(request);
        });

        verify(inventoryClient, times(1)).checkInventory(9999L);
        verify(inventoryClient, never()).updateInventory(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testPlaceOrder_InventoryUpdateFails() {
        // Arrange
        OrderRequest request = new OrderRequest(1001L, 20);

        InventoryUpdateResponse failedResponse = new InventoryUpdateResponse();
        failedResponse.setSuccess(false);
        failedResponse.setMessage("Failed to reserve inventory");

        when(inventoryClient.checkInventory(1001L)).thenReturn(inventoryResponse);
        when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class)))
                .thenReturn(failedResponse);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(request);
        });

        verify(inventoryClient, times(1)).checkInventory(1001L);
        verify(inventoryClient, times(1)).updateInventory(any());
        verify(orderRepository, never()).save(any());
    }
}

