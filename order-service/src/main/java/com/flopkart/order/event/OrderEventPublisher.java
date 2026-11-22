package com.flopkart.order.event;

import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.StockReleaseEvent;

public interface OrderEventPublisher {

    void publishOrderPlaced(OrderPlacedEvent event);

    void publishStockRelease(StockReleaseEvent event);
}
