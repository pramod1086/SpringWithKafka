package com.pramod.springwithkafka;

import com.pramod.springwithkafka.commerce.CommerceTopics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(properties = {
		"spring.data.elasticsearch.repositories.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchAutoConfiguration,org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchRepositoriesAutoConfiguration,org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchReactiveRepositoriesAutoConfiguration,org.springframework.boot.elasticsearch.autoconfigure.ElasticsearchClientAutoConfiguration,org.springframework.boot.elasticsearch.autoconfigure.ElasticsearchRestClientAutoConfiguration,org.springframework.boot.data.elasticsearch.autoconfigure.health.DataElasticsearchReactiveHealthContributorAutoConfiguration,org.springframework.boot.elasticsearch.autoconfigure.health.ElasticsearchRestHealthContributorAutoConfiguration",
		"spring.kafka.plain-consumer.enabled=false"
})
@EmbeddedKafka(partitions = 1, topics = {
		"users",
		"transactions",
		CommerceTopics.CATALOG_EVENTS,
		CommerceTopics.CART_EVENTS,
		"transactions.streams.processed"
})
class SpringWithKafkaApplicationTests {

	@Test
	void contextLoads() {
	}

}
