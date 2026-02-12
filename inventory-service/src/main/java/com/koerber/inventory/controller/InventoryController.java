package com.koerber.inventory.controller;

import com.koerber.inventory.dto.InventoryResponse;
import com.koerber.inventory.dto.InventoryUpdateRequest;
import com.koerber.inventory.dto.InventoryUpdateResponse;
import com.koerber.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable Long productId) {
        try {
            InventoryResponse response = inventoryService.getInventoryByProductId(productId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<InventoryUpdateResponse> updateInventory(@RequestBody InventoryUpdateRequest request) {
        InventoryUpdateResponse response = inventoryService.updateInventory(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}

