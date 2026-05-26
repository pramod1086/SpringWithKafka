package com.pramod.springwithkafka.commerce.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pramod.springwithkafka.commerce.CommerceTopics;
import com.pramod.springwithkafka.commerce.events.CatalogEvent;
import com.pramod.springwithkafka.commerce.projection.ProductCatalogProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes catalog domain events and (in the monolith) updates the local product projection immediately
 * so cart operations stay consistent without waiting for a consumer loop. When catalog is extracted,
 * remove the projection side-effect here and let the cart service consume {@link CommerceTopics#CATALOG_EVENTS}.
 */
@Component
public class KafkaCatalogEventPublisher implements CatalogEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaCatalogEventPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final ProductCatalogProjection projection;
	private final boolean applyProjectionLocally;

	public KafkaCatalogEventPublisher(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			ProductCatalogProjection projection,
			@Value("${commerce.catalog.apply-projection-in-publisher:true}") boolean applyProjectionLocally) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.projection = projection;
		this.applyProjectionLocally = applyProjectionLocally;
	}

	@Override
	public void publish(CatalogEvent event) {
		if (applyProjectionLocally) {
			projection.apply(event);
		}
		try {
			String json = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(CommerceTopics.CATALOG_EVENTS, event.partitionKey(), json)
					.whenComplete((r, ex) -> {
						if (ex != null) {
							log.warn("Failed to publish catalog event type={}", event.getType(), ex);
						}
					});
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Catalog event serialization failed", e);
		}
	}
}
