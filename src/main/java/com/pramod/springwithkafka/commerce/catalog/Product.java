package com.pramod.springwithkafka.commerce.catalog;

import java.math.BigDecimal;
import java.util.Objects;

public final class Product {

	private final String id;
	private final String sku;
	private final String name;
	private final String categoryId;
	private final BigDecimal unitPrice;
	/** e.g. phone generation "16" */
	private final String model;
	/** Storage in gigabytes, e.g. 16 for 16&nbsp;GB */
	private final Integer storageGb;
	/** e.g. "white" */
	private final String color;

	public Product(
			String id,
			String sku,
			String name,
			String categoryId,
			BigDecimal unitPrice,
			String model,
			Integer storageGb,
			String color) {
		this.id = Objects.requireNonNull(id, "id");
		this.sku = Objects.requireNonNull(sku, "sku");
		this.name = Objects.requireNonNull(name, "name");
		this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
		this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
		this.model = model;
		this.storageGb = storageGb;
		this.color = color;
	}

	public String getId() {
		return id;
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

	public String getModel() {
		return model;
	}

	public Integer getStorageGb() {
		return storageGb;
	}

	public String getColor() {
		return color;
	}
}
