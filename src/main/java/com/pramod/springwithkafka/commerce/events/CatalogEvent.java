package com.pramod.springwithkafka.commerce.events;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON contract for {@link com.pramod.springwithkafka.commerce.CommerceTopics#CATALOG_EVENTS}.
 * Designed to be stable when catalog becomes its own service; cart (or others) consume the same shape.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogEvent {

	private CatalogEventType type;
	private long occurredAtEpochMillis;

	private String categoryId;
	private String categoryName;
	private String parentCategoryId;

	private String productId;
	private String sku;
	private String productName;
	private String productCategoryId;
	private java.math.BigDecimal unitPrice;
	private String productModel;
	private Integer productStorageGb;
	private String productColor;

	public CatalogEvent() {
	}

	public CatalogEventType getType() {
		return type;
	}

	public void setType(CatalogEventType type) {
		this.type = type;
	}

	public long getOccurredAtEpochMillis() {
		return occurredAtEpochMillis;
	}

	public void setOccurredAtEpochMillis(long occurredAtEpochMillis) {
		this.occurredAtEpochMillis = occurredAtEpochMillis;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getParentCategoryId() {
		return parentCategoryId;
	}

	public void setParentCategoryId(String parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductCategoryId() {
		return productCategoryId;
	}

	public void setProductCategoryId(String productCategoryId) {
		this.productCategoryId = productCategoryId;
	}

	public java.math.BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(java.math.BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getProductModel() {
		return productModel;
	}

	public void setProductModel(String productModel) {
		this.productModel = productModel;
	}

	public Integer getProductStorageGb() {
		return productStorageGb;
	}

	public void setProductStorageGb(Integer productStorageGb) {
		this.productStorageGb = productStorageGb;
	}

	public String getProductColor() {
		return productColor;
	}

	public void setProductColor(String productColor) {
		this.productColor = productColor;
	}

	/** Kafka key: keep catalog changes ordered per aggregate when needed. */
	public String partitionKey() {
		if (productId != null) {
			return productId;
		}
		if (categoryId != null) {
			return categoryId;
		}
		return "catalog";
	}
}
