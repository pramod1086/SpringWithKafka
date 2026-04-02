package com.pramod.springwithkafka.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Indexes Kafka record values into Elasticsearch (separate consumer group from other listeners).
 */
@Service
@ConditionalOnBean(IndexedKafkaMessageRepository.class)
public class KafkaElasticsearchIndexer {

	private static final Logger log = LoggerFactory.getLogger(KafkaElasticsearchIndexer.class);

	private final IndexedKafkaMessageRepository repository;

	public KafkaElasticsearchIndexer(IndexedKafkaMessageRepository repository) {
		this.repository = repository;
	}

	@KafkaListener(
			topics = { "users", "transactions" },
			groupId = "${elasticsearch.kafka.consumer-group:elasticsearch-indexer}"
	)
	public void index(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		String id = UUID.randomUUID().toString();
		repository.save(new IndexedKafkaMessage(id, topic, payload, Instant.now().toEpochMilli()));
		log.info("Indexed Kafka message to Elasticsearch topic={} id={}", topic, id);
	}
}
