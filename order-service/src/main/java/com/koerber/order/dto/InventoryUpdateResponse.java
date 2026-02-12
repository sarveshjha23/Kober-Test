package com.koerber.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateResponse {
    private boolean success;
    private String message;
    private Long productId;
    private Integer quantity;
    private java.util.List<Long> reservedFromBatchIds;
}

