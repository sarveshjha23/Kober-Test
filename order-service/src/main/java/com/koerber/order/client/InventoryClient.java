package com.koerber.order.client;

import com.koerber.order.dto.InventoryResponse;
import com.koerber.order.dto.InventoryUpdateRequest;
import com.koerber.order.dto.InventoryUpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with Inventory Service
 */
@Component
@Slf4j
public class InventoryClient {

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public InventoryClient(RestTemplate restTemplate,
                          @Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    /**
     * Check inventory availability for a product
     * @param productId Product ID
     * @return Inventory response
     */
    public InventoryResponse checkInventory(Long productId) {
        String url = inventoryServiceUrl + "/inventory/" + productId;
        log.info("Checking inventory for product {} at {}", productId, url);

        try {
            return restTemplate.getForObject(url, InventoryResponse.class);
        } catch (Exception e) {
            log.error("Failed to check inventory: {}", e.getMessage());
            throw new RuntimeException("Failed to communicate with Inventory Service", e);
        }
    }

    /**
     * Update inventory after placing an order
     * @param request Inventory update request
     * @return Inventory update response
     */
    public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {
        String url = inventoryServiceUrl + "/inventory/update";
        log.info("Updating inventory at {}", url);

        try {
            return restTemplate.postForObject(url, request, InventoryUpdateResponse.class);
        } catch (Exception e) {
            log.error("Failed to update inventory: {}", e.getMessage());
            throw new RuntimeException("Failed to communicate with Inventory Service", e);
        }
    }
}

