package com.pramod.springwithkafka.commerce.cart;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CartStore {

	private final Map<String, Cart> carts = new ConcurrentHashMap<>();

	public Cart put(Cart cart) {
		carts.put(cart.getId(), cart);
		return cart;
	}

	public Optional<Cart> get(String cartId) {
		return Optional.ofNullable(carts.get(cartId));
	}
}
