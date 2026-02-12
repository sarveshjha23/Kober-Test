package com.koerber.inventory.factory;

import com.koerber.inventory.entity.InventoryBatch;

import java.util.List;

/**
 * Interface for inventory handling strategies.
 * This allows for future extension of different inventory management approaches.
 */
public interface InventoryHandler {

    /**
     * Reserve inventory for an order by deducting from batches
     * @param batches List of available batches sorted by expiry date
     * @param quantityNeeded Quantity to reserve
     * @return List of batch IDs from which inventory was reserved
     * @throws IllegalArgumentException if insufficient inventory
     */
    List<Long> reserveInventory(List<InventoryBatch> batches, int quantityNeeded);

    /**
     * Get the handler type name
     * @return Handler type identifier
     */
    String getHandlerType();
}

