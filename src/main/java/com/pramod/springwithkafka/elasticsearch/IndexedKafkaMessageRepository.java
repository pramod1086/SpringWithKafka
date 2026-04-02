package com.pramod.springwithkafka.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IndexedKafkaMessageRepository extends ElasticsearchRepository<IndexedKafkaMessage, String> {
}
