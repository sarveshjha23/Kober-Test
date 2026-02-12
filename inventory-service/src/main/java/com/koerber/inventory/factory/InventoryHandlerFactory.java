package com.koerber.inventory.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating inventory handlers.
 * This allows easy extension with new inventory handling strategies.
 */
@Component
public class InventoryHandlerFactory {

    private final Map<String, InventoryHandler> handlers = new HashMap<>();

    @Autowired
    public InventoryHandlerFactory(List<InventoryHandler> handlerList) {
        for (InventoryHandler handler : handlerList) {
            handlers.put(handler.getHandlerType(), handler);
        }
    }

    /**
     * Get an inventory handler by type
     * @param type Handler type (e.g., "FIFO", "LIFO", etc.)
     * @return The appropriate inventory handler
     */
    public InventoryHandler getHandler(String type) {
        InventoryHandler handler = handlers.get(type);
        if (handler == null) {
            // Default to FIFO if type not found
            return handlers.get("FIFO");
        }
        return handler;
    }

    /**
     * Get the default inventory handler (FIFO)
     * @return Default inventory handler
     */
    public InventoryHandler getDefaultHandler() {
        return handlers.get("FIFO");
    }
}

