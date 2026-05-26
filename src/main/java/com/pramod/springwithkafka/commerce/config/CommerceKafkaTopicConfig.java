package com.pramod.springwithkafka.commerce.config;

import com.pramod.springwithkafka.commerce.CommerceTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class CommerceKafkaTopicConfig {

	@Bean
	public NewTopic commerceCatalogEventsTopic() {
		return TopicBuilder.name(CommerceTopics.CATALOG_EVENTS).partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic commerceCartEventsTopic() {
		return TopicBuilder.name(CommerceTopics.CART_EVENTS).partitions(3).replicas(1).build();
	}
}
