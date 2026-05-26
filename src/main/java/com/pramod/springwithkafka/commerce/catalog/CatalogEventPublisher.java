package com.pramod.springwithkafka.commerce.catalog;

import com.pramod.springwithkafka.commerce.events.CatalogEvent;

/**
 * Outbound catalog notifications. In a split deployment the implementation lives only in the catalog service.
 */
public interface CatalogEventPublisher {

	void publish(CatalogEvent event);
}
