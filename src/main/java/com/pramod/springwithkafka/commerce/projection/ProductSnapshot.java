package com.pramod.springwithkafka.commerce.projection;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Read model derived from {@link com.pramod.springwithkafka.commerce.events.CatalogEvent}s.
 * A standalone cart microservice would maintain this projection by consuming the catalog topic only.
 */
public final class ProductSnapshot {

	private final String productId;
	private final String sku;
	private final String name;
	private final String categoryId;
	private final BigDecimal unitPrice;

	public ProductSnapshot(String productId, String sku, String name, String categoryId, BigDecimal unitPrice) {
		this.productId = Objects.requireNonNull(productId);
		this.sku = Objects.requireNonNull(sku);
		this.name = Objects.requireNonNull(name);
		this.categoryId = Objects.requireNonNull(categoryId);
		this.unitPrice = Objects.requireNonNull(unitPrice);
	}

	public String getProductId() {
		return productId;
	}

	public String getSku() {
		return sku;
	}

	public String getName() {
		return name;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}
}
