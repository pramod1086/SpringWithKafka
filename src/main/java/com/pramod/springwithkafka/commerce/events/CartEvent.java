package com.pramod.springwithkafka.commerce.events;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON contract for {@link com.pramod.springwithkafka.commerce.CommerceTopics#CART_EVENTS}.
 * Downstream services (inventory, pricing, fulfillment) can subscribe without coupling to cart HTTP APIs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartEvent {

	private CartEventType type;
	private long occurredAtEpochMillis;

	private String cartId;
	private String customerId;
	private String lineId;
	private String productId;
	private Integer quantity;
	private java.math.BigDecimal lineTotal;

	public CartEvent() {
	}

	public CartEventType getType() {
		return type;
	}

	public void setType(CartEventType type) {
		this.type = type;
	}

	public long getOccurredAtEpochMillis() {
		return occurredAtEpochMillis;
	}

	public void setOccurredAtEpochMillis(long occurredAtEpochMillis) {
		this.occurredAtEpochMillis = occurredAtEpochMillis;
	}

	public String getCartId() {
		return cartId;
	}

	public void setCartId(String cartId) {
		this.cartId = cartId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getLineId() {
		return lineId;
	}

	public void setLineId(String lineId) {
		this.lineId = lineId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public java.math.BigDecimal getLineTotal() {
		return lineTotal;
	}

	public void setLineTotal(java.math.BigDecimal lineTotal) {
		this.lineTotal = lineTotal;
	}

	public String partitionKey() {
		return cartId != null ? cartId : "cart";
	}
}
