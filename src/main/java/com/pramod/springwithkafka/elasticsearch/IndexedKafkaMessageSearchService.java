package com.pramod.springwithkafka.elasticsearch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-side queries over the {@code kafka-messages} index (populated by {@link KafkaElasticsearchIndexer}).
 */
@Service
@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true")
@ConditionalOnBean({IndexedKafkaMessageRepository.class, ElasticsearchOperations.class})
public class IndexedKafkaMessageSearchService {

	private final ElasticsearchOperations operations;

	public IndexedKafkaMessageSearchService(ElasticsearchOperations operations) {
		this.operations = operations;
	}

	/**
	 * @param topic optional exact topic (keyword), e.g. {@code commerce.catalog.events}
	 * @param q     optional text match against {@code payload} (analyzed field)
	 */
	public Page<IndexedKafkaMessage> search(String topic, String q, Pageable pageable) {
		boolean hasTopic = topic != null && !topic.isBlank();
		boolean hasQ = q != null && !q.isBlank();
		if (!hasTopic && !hasQ) {
			throw new IllegalArgumentException("Provide at least one of: topic (exact), q (search inside payload)");
		}
		Criteria criteria = null;
		if (hasTopic) {
			criteria = new Criteria("topic").is(topic.trim());
		}
		if (hasQ) {
			Criteria payloadCriteria = new Criteria("payload").matches(q.trim());
			criteria = criteria == null ? payloadCriteria : criteria.and(payloadCriteria);
		}
		CriteriaQuery query = new CriteriaQuery(criteria).setPageable(pageable);
		SearchHits<IndexedKafkaMessage> hits = operations.search(query, IndexedKafkaMessage.class);
		List<IndexedKafkaMessage> content = hits.getSearchHits().stream()
				.map(SearchHit::getContent)
				.collect(Collectors.toList());
		return new PageImpl<>(content, pageable, hits.getTotalHits());
	}
}
