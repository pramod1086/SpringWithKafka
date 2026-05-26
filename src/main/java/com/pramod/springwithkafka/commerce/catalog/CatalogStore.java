package com.pramod.springwithkafka.commerce.catalog;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class CatalogStore {

	private final Map<String, Category> categories = new ConcurrentHashMap<>();
	private final Map<String, Product> products = new ConcurrentHashMap<>();

	public Category putCategory(Category c) {
		categories.put(c.getId(), c);
		return c;
	}

	public Optional<Category> getCategory(String id) {
		return Optional.ofNullable(categories.get(id));
	}

	public Collection<Category> allCategories() {
		return categories.values();
	}

	public void removeCategory(String id) {
		categories.remove(id);
	}

	public Product putProduct(Product p) {
		products.put(p.getId(), p);
		return p;
	}

	public Optional<Product> getProduct(String id) {
		return Optional.ofNullable(products.get(id));
	}

	public Optional<Product> getProductBySku(String sku) {
		return products.values().stream().filter(p -> p.getSku().equals(sku)).findFirst();
	}

	public Collection<Product> productsInCategory(String categoryId) {
		return products.values().stream()
				.filter(p -> p.getCategoryId().equals(categoryId))
				.collect(Collectors.toList());
	}

	public Collection<Product> allProducts() {
		return products.values();
	}

	public void removeProduct(String id) {
		products.remove(id);
	}
}
