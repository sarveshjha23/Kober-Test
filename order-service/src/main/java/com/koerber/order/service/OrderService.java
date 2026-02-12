package com.koerber.order.service;

import com.koerber.order.client.InventoryClient;
import com.koerber.order.dto.*;
import com.koerber.order.entity.Order;
import com.koerber.order.entity.OrderStatus;
import com.koerber.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    /**
     * Place a new order
     * @param request Order request
     * @return Order response
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Placing order for product ID: {} with quantity: {}",
                request.getProductId(), request.getQuantity());

        // Step 1: Check inventory availability
        InventoryResponse inventoryResponse;
        try {
            inventoryResponse = inventoryClient.checkInventory(request.getProductId());
        } catch (Exception e) {
            log.error("Failed to check inventory: {}", e.getMessage());
            throw new RuntimeException("Product not found or inventory service unavailable");
        }

        // Step 2: Calculate total available quantity
        int totalAvailable = inventoryResponse.getBatches().stream()
                .mapToInt(BatchDTO::getQuantity)
                .sum();

        if (totalAvailable < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient inventory. Available: " + totalAvailable +
                    ", Requested: " + request.getQuantity());
        }

        // Step 3: Update inventory (reserve stock)
        InventoryUpdateRequest updateRequest = new InventoryUpdateRequest(
                request.getProductId(),
                request.getQuantity(),
                null
        );

        InventoryUpdateResponse updateResponse;
        try {
            updateResponse = inventoryClient.updateInventory(updateRequest);
        } catch (Exception e) {
            log.error("Failed to update inventory: {}", e.getMessage());
            throw new RuntimeException("Failed to reserve inventory");
        }

        if (!updateResponse.isSuccess()) {
            throw new RuntimeException("Failed to reserve inventory: " + updateResponse.getMessage());
        }

        // Step 4: Create order
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setProductName(inventoryResponse.getProductName());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.PLACED);
        order.setOrderDate(LocalDate.now());

        // Store batch IDs as comma-separated string
        if (updateResponse.getReservedFromBatchIds() != null) {
            String batchIds = updateResponse.getReservedFromBatchIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            order.setReservedFromBatchIds(batchIds);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getOrderId());

        // Step 5: Return response
        return new OrderResponse(
                savedOrder.getOrderId(),
                savedOrder.getProductId(),
                savedOrder.getProductName(),
                savedOrder.getQuantity(),
                savedOrder.getStatus().name(),
                updateResponse.getReservedFromBatchIds(),
                "Order placed. Inventory reserved."
        );
    }
}

