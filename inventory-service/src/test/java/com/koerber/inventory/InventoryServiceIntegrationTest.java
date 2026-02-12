package com.koerber.inventory;

import com.koerber.inventory.dto.InventoryResponse;
import com.koerber.inventory.dto.InventoryUpdateRequest;
import com.koerber.inventory.dto.InventoryUpdateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/inventory";
    }

    @Test
    void testGetInventory_Integration() {
        // Act
        ResponseEntity<InventoryResponse> response = restTemplate.getForEntity(
                getBaseUrl() + "/1001",
                InventoryResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1001L, response.getBody().getProductId());
        assertEquals("Laptop", response.getBody().getProductName());
        assertFalse(response.getBody().getBatches().isEmpty());
    }

    @Test
    void testUpdateInventory_Integration() {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest(1002L, 5, null);

        // Act
        ResponseEntity<InventoryUpdateResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/update",
                request,
                InventoryUpdateResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void testGetInventory_ProductNotFound() {
        // Act
        ResponseEntity<InventoryResponse> response = restTemplate.getForEntity(
                getBaseUrl() + "/99999",
                InventoryResponse.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

