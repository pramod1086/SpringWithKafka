package com.pramod.springwithkafka.commerce.web;

import com.pramod.springwithkafka.commerce.catalog.CatalogService;
import com.pramod.springwithkafka.commerce.catalog.Category;
import com.pramod.springwithkafka.commerce.catalog.Product;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/commerce/catalog")
public class CatalogRestController {

	private final CatalogService catalogService;

	public CatalogRestController(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@PostMapping("/categories")
	public Category createCategory(@RequestBody CreateCategoryRequest body) {
		return catalogService.createCategory(body.getName(), body.getParentCategoryId());
	}

	@GetMapping("/categories")
	public Collection<Category> listCategories() {
		return catalogService.listCategories();
	}

	@DeleteMapping("/categories/{id}")
	public void deleteCategory(@PathVariable("id") String id) {
		catalogService.deleteCategory(id);
	}

	@PostMapping("/products")
	public Product createProduct(@RequestBody CreateProductRequest body) {
		return catalogService.createProduct(
				body.getSku(),
				body.getName(),
				body.getCategoryId(),
				body.getUnitPrice(),
				body.getModel(),
				body.getStorageGb(),
				body.getColor());
	}

	@GetMapping("/products")
	public Collection<Product> listProducts(@RequestParam(value = "categoryId", required = false) String categoryId) {
		return catalogService.listProducts(Optional.ofNullable(categoryId));
	}

	@DeleteMapping("/products/{id}")
	public void deleteProduct(@PathVariable("id") String id) {
		catalogService.deleteProduct(id);
	}

	public static class CreateCategoryRequest {
		private String name;
		private String parentCategoryId;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getParentCategoryId() {
			return parentCategoryId;
		}

		public void setParentCategoryId(String parentCategoryId) {
			this.parentCategoryId = parentCategoryId;
		}
	}

	public static class CreateProductRequest {
		private String sku;
		private String name;
		private String categoryId;
		private BigDecimal unitPrice;
		/** Phone / device generation, e.g. {@code "16"} */
		private String model;
		/** Storage size in GB, e.g. {@code 16} for 16 GB */
		private Integer storageGb;
		/** Finish color, e.g. {@code "white"} */
		private String color;

		public String getSku() {
			return sku;
		}

		public void setSku(String sku) {
			this.sku = sku;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(String categoryId) {
			this.categoryId = categoryId;
		}

		public BigDecimal getUnitPrice() {
			return unitPrice;
		}

		public void setUnitPrice(BigDecimal unitPrice) {
			this.unitPrice = unitPrice;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public Integer getStorageGb() {
			return storageGb;
		}

		public void setStorageGb(Integer storageGb) {
			this.storageGb = storageGb;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}
	}
}
