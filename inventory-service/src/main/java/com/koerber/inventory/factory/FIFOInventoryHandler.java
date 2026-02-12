package com.koerber.inventory.factory;

import com.koerber.inventory.entity.InventoryBatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FIFO (First In First Out) based on expiry date.
 * This handler reserves inventory from batches with earliest expiry dates first.
 */
@Component
public class FIFOInventoryHandler implements InventoryHandler {

    @Override
    public List<Long> reserveInventory(List<InventoryBatch> batches, int quantityNeeded) {
        List<Long> reservedBatchIds = new ArrayList<>();
        int remainingQuantity = quantityNeeded;

        for (InventoryBatch batch : batches) {
            if (remainingQuantity <= 0) {
                break;
            }

            if (batch.getQuantity() > 0) {
                int quantityToReserve = Math.min(batch.getQuantity(), remainingQuantity);
                batch.setQuantity(batch.getQuantity() - quantityToReserve);
                remainingQuantity -= quantityToReserve;
                reservedBatchIds.add(batch.getBatchId());
            }
        }

        if (remainingQuantity > 0) {
            throw new IllegalArgumentException("Insufficient inventory. Still need " + remainingQuantity + " units.");
        }

        return reservedBatchIds;
    }

    @Override
    public String getHandlerType() {
        return "FIFO";
    }
}

