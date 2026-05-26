package com.pramod.springwithkafka.commerce.web;

import com.pramod.springwithkafka.commerce.store.Store;
import com.pramod.springwithkafka.commerce.store.StoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/commerce/stores")
public class StoreRestController {

	private final StoreService storeService;

	public StoreRestController(StoreService storeService) {
		this.storeService = storeService;
	}

	@PostMapping
	public Store createStore(@RequestBody CreateStoreRequest body) {
		return storeService.createStore(body.getName());
	}

	@PostMapping("/{storeId}/inventory")
	public void addInventory(@PathVariable("storeId") String storeId, @RequestBody AddInventoryRequest body) {
		storeService.addStock(storeId, body.getProductId(), body.getQuantity());
	}

	/**
	 * Find stores that have at least one matching catalog product in stock (quantity &gt; 0).
	 * Example: iPhone 16, 16&nbsp;GB, white — {@code ?name=iPhone&model=16&storageGb=16&color=white}
	 */
	@GetMapping("/search")
	public List<Store> searchStoresByProduct(
			@RequestParam(value = "name", required = false) String nameContains,
			@RequestParam(value = "model", required = false) String model,
			@RequestParam(value = "storageGb", required = false) Integer storageGb,
			@RequestParam(value = "color", required = false) String color) {
		return storeService.findStoresStockingProduct(nameContains, model, storageGb, color);
	}

	public static class CreateStoreRequest {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class AddInventoryRequest {
		private String productId;
		private int quantity;

		public String getProductId() {
			return productId;
		}

		public void setProductId(String productId) {
			this.productId = productId;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}
}
