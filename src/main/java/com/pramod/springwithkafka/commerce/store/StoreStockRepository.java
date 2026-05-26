package com.pramod.springwithkafka.commerce.store;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class StoreStockRepository {

	private final Map<String, Store> stores = new ConcurrentHashMap<>();
	/** storeId -> (productId -> quantity on hand) */
	private final Map<String, ConcurrentHashMap<String, Integer>> stockByStore = new ConcurrentHashMap<>();

	public Store putStore(Store store) {
		stores.put(store.getId(), store);
		stockByStore.putIfAbsent(store.getId(), new ConcurrentHashMap<>());
		return store;
	}

	public Optional<Store> getStore(String storeId) {
		return Optional.ofNullable(stores.get(storeId));
	}

	public Collection<Store> allStores() {
		return stores.values();
	}

	public void addStock(String storeId, String productId, int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be >= 1");
		}
		ConcurrentHashMap<String, Integer> row = stockByStore.computeIfAbsent(storeId, k -> new ConcurrentHashMap<>());
		row.merge(productId, quantity, Integer::sum);
	}

	public int quantityAt(String storeId, String productId) {
		ConcurrentHashMap<String, Integer> row = stockByStore.get(storeId);
		if (row == null) {
			return 0;
		}
		return row.getOrDefault(productId, 0);
	}

	/** Store ids that carry {@code productId} with positive quantity. */
	public Collection<String> storeIdsStockingProduct(String productId) {
		return stockByStore.entrySet().stream()
				.filter(e -> e.getValue().getOrDefault(productId, 0) > 0)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}
}
