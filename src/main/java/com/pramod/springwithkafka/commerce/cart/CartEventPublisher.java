package com.pramod.springwithkafka.commerce.cart;

import com.pramod.springwithkafka.commerce.events.CartEvent;

public interface CartEventPublisher {

	void publish(CartEvent event);
}
