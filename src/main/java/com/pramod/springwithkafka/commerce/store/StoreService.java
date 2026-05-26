package com.pramod.springwithkafka.commerce.store;

import com.pramod.springwithkafka.commerce.catalog.CatalogStore;
import com.pramod.springwithkafka.commerce.catalog.Product;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoreService {

	private final StoreStockRepository stockRepository;
	private final CatalogStore catalogStore;

	public StoreService(StoreStockRepository stockRepository, CatalogStore catalogStore) {
		this.stockRepository = stockRepository;
		this.catalogStore = catalogStore;
	}

	public Store createStore(String name) {
		String id = UUID.randomUUID().toString();
		Store s = new Store(id, name);
		stockRepository.putStore(s);
		return s;
	}

	public void addStock(String storeId, String productId, int quantity) {
		stockRepository.getStore(storeId).orElseThrow(() -> new IllegalArgumentException("Unknown store: " + storeId));
		catalogStore.getProduct(productId).orElseThrow(() -> new IllegalArgumentException("Unknown product: " + productId));
		stockRepository.addStock(storeId, productId, quantity);
	}

	public List<Store> findStoresStockingProduct(String nameContains, String model, Integer storageGb, String color) {
		if (nameContains == null && model == null && storageGb == null && color == null) {
			throw new IllegalArgumentException(
					"Provide at least one of: nameContains, model, storageGb, color (e.g. name=iPhone&model=16&storageGb=16&color=white)");
		}
		Set<String> productIds = catalogStore.allProducts().stream()
				.filter(p -> matchesAttributes(p, nameContains, model, storageGb, color))
				.map(Product::getId)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		Set<String> storeIds = new LinkedHashSet<>();
		for (String productId : productIds) {
			storeIds.addAll(stockRepository.storeIdsStockingProduct(productId));
		}
		return storeIds.stream()
				.map(id -> stockRepository.getStore(id).orElse(null))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	static boolean matchesAttributes(Product p, String nameContains, String model, Integer storageGb, String color) {
		if (nameContains != null && !p.getName().toLowerCase(Locale.ROOT).contains(nameContains.toLowerCase(Locale.ROOT))) {
			return false;
		}
		if (model != null) {
			if (p.getModel() == null || !p.getModel().equalsIgnoreCase(model.trim())) {
				return false;
			}
		}
		if (storageGb != null) {
			if (p.getStorageGb() == null || !p.getStorageGb().equals(storageGb)) {
				return false;
			}
		}
		if (color != null) {
			if (p.getColor() == null || !p.getColor().equalsIgnoreCase(color.trim())) {
				return false;
			}
		}
		return true;
	}
}
