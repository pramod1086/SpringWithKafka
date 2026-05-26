package com.pramod.springwithkafka.commerce.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pramod.springwithkafka.commerce.CommerceTopics;
import com.pramod.springwithkafka.commerce.events.CartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaCartEventPublisher implements CartEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaCartEventPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public KafkaCartEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public void publish(CartEvent event) {
		try {
			String json = objectMapper.writeValueAsString(event);
			kafkaTemplate.send(CommerceTopics.CART_EVENTS, event.partitionKey(), json)
					.whenComplete((r, ex) -> {
						if (ex != null) {
							log.warn("Failed to publish cart event type={}", event.getType(), ex);
						}
					});
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Cart event serialization failed", e);
		}
	}
}
