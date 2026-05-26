package com.pramod.springwithkafka.commerce.cart;

import java.math.BigDecimal;
import java.util.Objects;

public final class CartLine {

	private final String lineId;
	private final String productId;
	private final int quantity;
	private final BigDecimal unitPriceSnapshot;
	private final BigDecimal lineTotal;

	public CartLine(String lineId, String productId, int quantity, BigDecimal unitPriceSnapshot, BigDecimal lineTotal) {
		this.lineId = Objects.requireNonNull(lineId);
		this.productId = Objects.requireNonNull(productId);
		this.quantity = quantity;
		this.unitPriceSnapshot = Objects.requireNonNull(unitPriceSnapshot);
		this.lineTotal = Objects.requireNonNull(lineTotal);
	}

	public String getLineId() {
		return lineId;
	}

	public String getProductId() {
		return productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPriceSnapshot() {
		return unitPriceSnapshot;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}
}
