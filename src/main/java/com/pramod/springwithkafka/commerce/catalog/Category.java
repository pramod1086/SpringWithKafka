package com.pramod.springwithkafka.commerce.catalog;

import java.util.Objects;

public final class Category {

	private final String id;
	private final String name;
	private final String parentCategoryId;

	public Category(String id, String name, String parentCategoryId) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.parentCategoryId = parentCategoryId;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getParentCategoryId() {
		return parentCategoryId;
	}
}
