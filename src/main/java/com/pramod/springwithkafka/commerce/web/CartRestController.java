package com.pramod.springwithkafka.commerce.web;

import com.pramod.springwithkafka.commerce.cart.Cart;
import com.pramod.springwithkafka.commerce.cart.CartLine;
import com.pramod.springwithkafka.commerce.cart.CartService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commerce/carts")
public class CartRestController {

	private final CartService cartService;

	public CartRestController(CartService cartService) {
		this.cartService = cartService;
	}

	@PostMapping
	public Cart createCart(@RequestBody CreateCartRequest body) {
		return cartService.createCart(body.getCustomerId());
	}

	@GetMapping("/{cartId}")
	public Cart getCart(@PathVariable("cartId") String cartId) {
		return cartService.getCart(cartId);
	}

	@PostMapping("/{cartId}/lines")
	public CartLine addLine(@PathVariable("cartId") String cartId, @RequestBody AddLineRequest body) {
		return cartService.addLine(cartId, body.getProductId(), body.getQuantity());
	}

	@DeleteMapping("/{cartId}/lines/{lineId}")
	public void removeLine(@PathVariable("cartId") String cartId, @PathVariable("lineId") String lineId) {
		cartService.removeLine(cartId, lineId);
	}

	@PostMapping("/{cartId}/clear")
	public void clear(@PathVariable("cartId") String cartId) {
		cartService.clear(cartId);
	}

	public static class CreateCartRequest {
		private String customerId;

		public String getCustomerId() {
			return customerId;
		}

		public void setCustomerId(String customerId) {
			this.customerId = customerId;
		}
	}

	public static class AddLineRequest {
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
