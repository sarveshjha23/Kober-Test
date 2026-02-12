package com.koerber.inventory.service;

import com.koerber.inventory.dto.*;
import com.koerber.inventory.entity.InventoryBatch;
import com.koerber.inventory.factory.InventoryHandler;
import com.koerber.inventory.factory.InventoryHandlerFactory;
import com.koerber.inventory.repository.InventoryBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryHandlerFactory handlerFactory;

    /**
     * Get inventory batches for a product, sorted by expiry date
     * @param productId Product ID
     * @return Inventory response with batches
     */
    public InventoryResponse getInventoryByProductId(Long productId) {
        log.info("Fetching inventory for product ID: {}", productId);

        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);

        if (batches.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        String productName = batches.get(0).getProductName();

        List<BatchDTO> batchDTOs = batches.stream()
                .map(batch -> new BatchDTO(batch.getBatchId(), batch.getQuantity(), batch.getExpiryDate()))
                .collect(Collectors.toList());

        return new InventoryResponse(productId, productName, batchDTOs);
    }

    /**
     * Reserve inventory and return the batch IDs from which inventory was reserved
     * @param productId Product ID
     * @param quantity Quantity to reserve
     * @return List of batch IDs from which inventory was reserved
     */
    @Transactional
    public List<Long> reserveInventory(Long productId, int quantity) {
        log.info("Reserving {} units of product ID: {}", quantity, productId);

        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);

        if (batches.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        // Use factory to get the appropriate handler (FIFO by default)
        InventoryHandler handler = handlerFactory.getDefaultHandler();

        // Reserve inventory using the handler
        List<Long> reservedBatchIds = handler.reserveInventory(batches, quantity);

        // Save updated batches
        inventoryBatchRepository.saveAll(batches);

        log.info("Successfully reserved inventory from batches: {}", reservedBatchIds);

        return reservedBatchIds;
    }

    /**
     * Update inventory (called by Order Service)
     * @param request Inventory update request
     * @return Update response
     */
    @Transactional
    public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {
        log.info("Updating inventory for product ID: {} with quantity: {}",
                request.getProductId(), request.getQuantity());

        try {
            List<Long> reservedBatchIds = reserveInventory(request.getProductId(), request.getQuantity());
            request.setReservedFromBatchIds(reservedBatchIds);

            return new InventoryUpdateResponse(true, "Inventory updated successfully");
        } catch (IllegalArgumentException e) {
            log.error("Failed to update inventory: {}", e.getMessage());
            return new InventoryUpdateResponse(false, e.getMessage());
        }
    }
}

