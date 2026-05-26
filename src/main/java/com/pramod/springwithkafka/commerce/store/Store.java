package com.pramod.springwithkafka.commerce.store;

import java.util.Objects;

public final class Store {

	private final String id;
	private final String name;

	public Store(String id, String name) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
