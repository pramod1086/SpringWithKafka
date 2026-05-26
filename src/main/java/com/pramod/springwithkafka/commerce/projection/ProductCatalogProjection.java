package com.pramod.springwithkafka.commerce.projection;

import com.pramod.springwithkafka.commerce.events.CatalogEvent;
import com.pramod.springwithkafka.commerce.events.CatalogEventType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductCatalogProjection {

	private final Map<String, ProductSnapshot> productsById = new ConcurrentHashMap<>();

	public void apply(CatalogEvent event) {
		if (event == null || event.getType() == null) {
			return;
		}
		switch (event.getType()) {
			case PRODUCT_UPSERTED:
				if (event.getProductId() != null && event.getUnitPrice() != null) {
					productsById.put(event.getProductId(), new ProductSnapshot(
							event.getProductId(),
							event.getSku() != null ? event.getSku() : "",
							event.getProductName() != null ? event.getProductName() : "",
							event.getProductCategoryId() != null ? event.getProductCategoryId() : "",
							event.getUnitPrice()));
				}
				break;
			case PRODUCT_REMOVED:
				if (event.getProductId() != null) {
					productsById.remove(event.getProductId());
				}
				break;
			default:
				break;
		}
	}

	public Optional<ProductSnapshot> findProduct(String productId) {
		return Optional.ofNullable(productsById.get(productId));
	}
}
