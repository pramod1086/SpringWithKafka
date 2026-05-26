package com.pramod.springwithkafka.commerce;

/**
 * Kafka topic names for the commerce bounded contexts. When splitting into microservices,
 * each service keeps its own producers/consumers but shares these topic contracts (or a
 * small shared schema module).
 */
public final class CommerceTopics {

	public static final String CATALOG_EVENTS = "commerce.catalog.events";
	public static final String CART_EVENTS = "commerce.cart.events";

	private CommerceTopics() {
	}
}
