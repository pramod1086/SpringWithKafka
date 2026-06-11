package com.pramod.springwithkafka.elasticsearch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP search over indexed Kafka messages. Active only when Elasticsearch repositories are enabled
 * (typically {@code spring-boot.run.profiles=elasticsearch}).
 */
@RestController
@RequestMapping("/api/elasticsearch")
@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true")
@ConditionalOnBean(IndexedKafkaMessageSearchService.class)
public class ElasticsearchSearchRestController {

	private final IndexedKafkaMessageSearchService searchService;

	public ElasticsearchSearchRestController(IndexedKafkaMessageSearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * Search the {@code kafka-messages} index. Supply {@code topic} and/or {@code q} (payload match).
	 */
	@GetMapping("/messages")
	public Page<IndexedKafkaMessage> searchMessages(
			@RequestParam(value = "topic", required = false) String topic,
			@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		if (size > 100) {
			size = 100;
		}
		if (page < 0) {
			page = 0;
		}
		return searchService.search(topic, q, PageRequest.of(page, size));
	}
}
