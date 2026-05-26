package com.pramod.springwithkafka.commerce.cart;

import com.pramod.springwithkafka.commerce.events.CartEvent;
import com.pramod.springwithkafka.commerce.events.CartEventType;
import com.pramod.springwithkafka.commerce.projection.ProductCatalogProjection;
import com.pramod.springwithkafka.commerce.projection.ProductSnapshot;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CartService {

	private final CartStore cartStore;
	private final ProductCatalogProjection productCatalog;
	private final CartEventPublisher cartEvents;

	public CartService(CartStore cartStore, ProductCatalogProjection productCatalog, CartEventPublisher cartEvents) {
		this.cartStore = cartStore;
		this.productCatalog = productCatalog;
		this.cartEvents = cartEvents;
	}

	public Cart createCart(String customerId) {
		String id = UUID.randomUUID().toString();
		Cart cart = new Cart(id, customerId);
		cartStore.put(cart);
		CartEvent e = new CartEvent();
		e.setType(CartEventType.CART_CREATED);
		e.setOccurredAtEpochMillis(Instant.now().toEpochMilli());
		e.setCartId(id);
		e.setCustomerId(customerId);
		cartEvents.publish(e);
		return cart;
	}

	public Cart getCart(String cartId) {
		return cartStore.get(cartId).orElseThrow(() -> new IllegalArgumentException("Unknown cart: " + cartId));
	}

	public CartLine addLine(String cartId, String productId, int quantity) {
		Cart cart = getCart(cartId);
		ProductSnapshot snap = productCatalog.findProduct(productId)
				.orElseThrow(() -> new IllegalArgumentException("Unknown or unpublished product: " + productId));
		CartLine line = cart.addLine(productId, quantity, snap.getUnitPrice());
		CartEvent e = new CartEvent();
		e.setType(CartEventType.LINE_ITEM_ADDED);
		e.setOccurredAtEpochMillis(Instant.now().toEpochMilli());
		e.setCartId(cartId);
		e.setCustomerId(cart.getCustomerId());
		e.setLineId(line.getLineId());
		e.setProductId(productId);
		e.setQuantity(quantity);
		e.setLineTotal(line.getLineTotal());
		cartEvents.publish(e);
		return line;
	}

	public void removeLine(String cartId, String lineId) {
		Cart cart = getCart(cartId);
		if (!cart.removeLine(lineId)) {
			throw new IllegalArgumentException("Unknown line: " + lineId);
		}
		CartEvent e = new CartEvent();
		e.setType(CartEventType.LINE_ITEM_REMOVED);
		e.setOccurredAtEpochMillis(Instant.now().toEpochMilli());
		e.setCartId(cartId);
		e.setCustomerId(cart.getCustomerId());
		e.setLineId(lineId);
		cartEvents.publish(e);
	}

	public void clear(String cartId) {
		Cart cart = getCart(cartId);
		cart.clear();
		CartEvent e = new CartEvent();
		e.setType(CartEventType.CART_CLEARED);
		e.setOccurredAtEpochMillis(Instant.now().toEpochMilli());
		e.setCartId(cartId);
		e.setCustomerId(cart.getCustomerId());
		cartEvents.publish(e);
	}
}
