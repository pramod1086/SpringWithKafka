package com.pramod.springwithkafka.commerce.catalog;

import com.pramod.springwithkafka.commerce.events.CatalogEvent;
import com.pramod.springwithkafka.commerce.events.CatalogEventType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogService {

	private final CatalogStore store;
	private final CatalogEventPublisher eventPublisher;

	public CatalogService(CatalogStore store, CatalogEventPublisher eventPublisher) {
		this.store = store;
		this.eventPublisher = eventPublisher;
	}

	public Category createCategory(String name, String parentCategoryId) {
		if (parentCategoryId != null && store.getCategory(parentCategoryId).isEmpty()) {
			throw new IllegalArgumentException("Unknown parent category: " + parentCategoryId);
		}
		String id = UUID.randomUUID().toString();
		Category c = new Category(id, name, parentCategoryId);
		store.putCategory(c);
		CatalogEvent e = new CatalogEvent();
		e.setType(CatalogEventType.CATEGORY_UPSERTED);
		e.setOccurredAtEpochMillis(Instant.now().toEpochMilli());
		e.setCategoryId(id);
		e.setCategoryName(name);
		e.setParentCategoryId(parentCategoryId);
		eventPublisher.publish(e);
		return c;
	}

	public Collection<Category> listCategories() {
		return store.allCategories();
	}

	public void deleteCategory(String id) {
		store.getCategory(id).orElseThrow(() -> new IllegalArgumentException("Unknown category: " + id));
		store.removeCategory(id);
		CatalogEvent e = new CatalogEvent();
		e.setType(CatalogEventType.CATEGORY_REMOVED);
		e.setOccurredAtEpochMillis(Instant.now().toEpochMilli());
		e.setCategoryId(id);
		eventPublisher.publish(e);
	}

	public Product createProduct(
			String sku,
			String name,
			String categoryId,
			BigDecimal unitPrice,
			String model,
			Integer storageGb,
			String color) {
		store.getCategory(categoryId).orElseThrow(() -> new IllegalArgumentException("Unknown category: " + categoryId));
		if (store.getProductBySku(sku).isPresent()) {
			throw new IllegalArgumentException("Duplicate SKU: " + sku);
		}
		String id = UUID.randomUUID().toString();
		Product p = new Product(id, sku, name, categoryId, unitPrice, model, storageGb, color);
		store.putProduct(p);
		publishProductUpserted(p);
		return p;
	}

	public Collection<Product> listProducts(Optional<String> categoryId) {
		return categoryId.map(store::productsInCategory).orElseGet(store::allProducts);
	}

	public void deleteProduct(String id) {
		store.getProduct(id).orElseThrow(() -> new IllegalArgumentException("Unknown product: " + id));
		store.removeProduct(id);
		CatalogEvent e = new CatalogEvent();
		e.setType(CatalogEventType.PRODUCT_REMOVED);
		e.setOccurredAtEpochMillis(Instant.now().toEpochMilli());
		e.setProductId(id);
		eventPublisher.publish(e);
	}

	private void publishProductUpserted(Product p) {
		CatalogEvent e = new CatalogEvent();
		e.setType(CatalogEventType.PRODUCT_UPSERTED);
		e.setOccurredAtEpochMillis(Instant.now().toEpochMilli());
		e.setProductId(p.getId());
		e.setSku(p.getSku());
		e.setProductName(p.getName());
		e.setProductCategoryId(p.getCategoryId());
		e.setUnitPrice(p.getUnitPrice());
		e.setProductModel(p.getModel());
		e.setProductStorageGb(p.getStorageGb());
		e.setProductColor(p.getColor());
		eventPublisher.publish(e);
	}
}
