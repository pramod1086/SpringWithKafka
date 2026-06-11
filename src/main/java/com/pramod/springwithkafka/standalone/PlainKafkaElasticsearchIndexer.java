package com.pramod.springwithkafka.standalone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pramod.springwithkafka.commerce.CommerceTopics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Reads from Kafka and indexes each message into Elasticsearch — same index and fields as
 * {@link com.pramod.springwithkafka.elasticsearch.KafkaElasticsearchIndexer}, without Spring Kafka or Spring Data.
 * <p>
 * Defaults: bootstrap {@code localhost:9092}, Elasticsearch {@code http://localhost:9200}, group
 * {@code plain-elasticsearch-indexer} (change if you want to share the load with the Spring consumer using
 * {@code elasticsearch-indexer}). Subscribes to {@code users}, {@code transactions},
 * {@link com.pramod.springwithkafka.commerce.CommerceTopics#CATALOG_EVENTS}, and
 * {@link com.pramod.springwithkafka.commerce.CommerceTopics#CART_EVENTS} unless you pass topic names as {@code main} args.
 * <p>
 * Run: {@code mvn -q compile exec:java -Dexec.mainClass=com.pramod.springwithkafka.standalone.PlainKafkaElasticsearchIndexer}
 * <p>
 * Overrides (optional): {@code -Dkafka.bootstrap.servers=... -Delasticsearch.uri=... -Dkafka.es.consumer.group=...}
 */
public final class PlainKafkaElasticsearchIndexer {

	private static final Logger log = LoggerFactory.getLogger(PlainKafkaElasticsearchIndexer.class);

	private static final String INDEX = "kafka-messages";
	private static final String DOCUMENT_CLASS = "com.pramod.springwithkafka.elasticsearch.IndexedKafkaMessage";

	private static final String BOOTSTRAP_SERVERS = System.getProperty("kafka.bootstrap.servers", "localhost:9092");
	private static final String ELASTICSEARCH_URI = System.getProperty("elasticsearch.uri", "http://localhost:9200");
	private static final String GROUP_ID = System.getProperty("kafka.es.consumer.group", "plain-elasticsearch-indexer");

	private static final List<String> TOPICS = List.of(
			"users",
			"transactions",
			CommerceTopics.CATALOG_EVENTS,
			CommerceTopics.CART_EVENTS);

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	public static void main(String[] args) {
		List<String> topics = args.length > 0 ? Arrays.asList(args) : TOPICS;
		String esBase = ELASTICSEARCH_URI.endsWith("/")
				? ELASTICSEARCH_URI.substring(0, ELASTICSEARCH_URI.length() - 1)
				: ELASTICSEARCH_URI;

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
			consumer.subscribe(topics);
			log.info("Kafka → ES indexer: topics={} bootstrap={} group={} elasticsearch={}",
					topics, BOOTSTRAP_SERVERS, GROUP_ID, esBase);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				log.info("Shutdown requested, waking consumer");
				consumer.wakeup();
			}));

			while (true) {
				try {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
					for (ConsumerRecord<String, String> r : records) {
						indexRecord(esBase, r.topic(), r.key(), r.value());
					}
				} catch (WakeupException e) {
					log.info("Consumer woken up, closing");
					break;
				}
			}
		}
	}

	private static void indexRecord(String esBase, String topic, String messageKey, String payload) {
		String id = UUID.randomUUID().toString();
		long indexedAt = Instant.now().toEpochMilli();

		Map<String, Object> doc = new LinkedHashMap<>();
		doc.put("_class", DOCUMENT_CLASS);
		doc.put("id", id);
		doc.put("topic", topic);
		if (messageKey != null && !messageKey.isEmpty()) {
			doc.put("messageKey", messageKey);
		}
		doc.put("payload", payload);
		doc.put("indexedAtEpochMillis", indexedAt);

		try {
			String json = MAPPER.writeValueAsString(doc);
			URI uri = URI.create(esBase + "/" + INDEX + "/_doc/" + id);
			HttpRequest request = HttpRequest.newBuilder(uri)
					.timeout(Duration.ofSeconds(30))
					.header("Content-Type", "application/json; charset=UTF-8")
					.PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
					.build();

			HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			int code = response.statusCode();
			if (code >= 200 && code < 300) {
				log.info("Indexed topic={} id={} status={}", topic, id, code);
			} else {
				log.warn("Index failed topic={} id={} status={} body={}", topic, id, code, response.body());
			}
		} catch (Exception e) {
			log.error("Index error topic={} id={}", topic, id, e);
		}
	}

	private PlainKafkaElasticsearchIndexer() {
	}
}
