package com.pramod.springwithkafka.commerce.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Cart {

	private final String id;
	private final String customerId;
	private final List<CartLine> lines = new ArrayList<>();

	public Cart(String id, String customerId) {
		this.id = Objects.requireNonNull(id);
		this.customerId = Objects.requireNonNull(customerId);
	}

	public String getId() {
		return id;
	}

	public String getCustomerId() {
		return customerId;
	}

	public List<CartLine> getLines() {
		return List.copyOf(lines);
	}

	public Optional<CartLine> findLine(String lineId) {
		return lines.stream().filter(l -> l.getLineId().equals(lineId)).findFirst();
	}

	public CartLine addLine(String productId, int quantity, BigDecimal unitPriceSnapshot) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be >= 1");
		}
		String lineId = UUID.randomUUID().toString();
		BigDecimal lineTotal = unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity));
		CartLine line = new CartLine(lineId, productId, quantity, unitPriceSnapshot, lineTotal);
		lines.add(line);
		return line;
	}

	public boolean removeLine(String lineId) {
		return lines.removeIf(l -> l.getLineId().equals(lineId));
	}

	public void clear() {
		lines.clear();
	}
}
