package com.pramod.springwithkafka.commerce.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pramod.springwithkafka.commerce.CommerceTopics;
import com.pramod.springwithkafka.commerce.events.CatalogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Optional consumer used when this application acts as the cart (or another) service and catalog
 * runs elsewhere. Do not enable together with local publisher projection unless you handle idempotency.
 */
@Component
@ConditionalOnProperty(name = "commerce.kafka.catalog-projection-consumer-enabled", havingValue = "true")
public class CatalogProjectionKafkaListener {

	private static final Logger log = LoggerFactory.getLogger(CatalogProjectionKafkaListener.class);

	private final ObjectMapper objectMapper;
	private final ProductCatalogProjection projection;

	public CatalogProjectionKafkaListener(ObjectMapper objectMapper, ProductCatalogProjection projection) {
		this.objectMapper = objectMapper;
		this.projection = projection;
	}

	@KafkaListener(
			topics = CommerceTopics.CATALOG_EVENTS,
			groupId = "${commerce.kafka.catalog-projection-consumer-group:catalog-projection}")
	public void onCatalogEvent(String json) {
		try {
			CatalogEvent event = objectMapper.readValue(json, CatalogEvent.class);
			projection.apply(event);
		}
		catch (Exception e) {
			log.warn("Could not apply catalog projection from Kafka payload={}", json, e);
		}
	}
}
