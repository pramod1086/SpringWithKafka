package com.pramod.springwithkafka.standalone;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Starts the same Apache {@link KafkaConsumer} poll loop as {@link PlainKafkaConsumer#} when the
 * Spring context is up, without using {@link org.springframework.kafka.annotation.KafkaListener}.
 */
@Component
@ConditionalOnProperty(name = "spring.kafka.plain-consumer.enabled", havingValue = "true", matchIfMissing = true)
public class PlainKafkaConsumerLifecycle implements SmartLifecycle {

	private static final Logger log = LoggerFactory.getLogger(PlainKafkaConsumerLifecycle.class);

	private static final Duration POLL_TIMEOUT = Duration.ofMillis(500);

	private final KafkaProperties kafkaProperties;
	private final String topicsCsv;
	private final String groupId;

	private final AtomicReference<KafkaConsumer<String, String>> consumerRef = new AtomicReference<>();

	private volatile boolean running;
	private volatile Thread worker;

	public PlainKafkaConsumerLifecycle(
			KafkaProperties kafkaProperties,
			@Value("${spring.kafka.plain-consumer.topics}") String topicsCsv,
			@Value("${spring.kafka.plain-consumer.group-id}") String groupId) {
		this.kafkaProperties = kafkaProperties;
		this.topicsCsv = topicsCsv;
		this.groupId = groupId;
	}

	@Override
	public void start() {
		if (this.running) {
			return;
		}
		List<String> topics = parseTopics(this.topicsCsv);
		Map<String, Object> configs = new HashMap<>(this.kafkaProperties.buildConsumerProperties(null));
		configs.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);

		this.running = true;
		this.worker = new Thread(() -> runConsumer(topics, configs), "plain-kafka-consumer");
		this.worker.start();
	}

	private void runConsumer(List<String> topics, Map<String, Object> configs) {
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs)) {
			this.consumerRef.set(consumer);
			consumer.subscribe(topics);
			log.info("Subscribed (plain poll) to {} group={}", topics, this.groupId);
			PlainKafkaConsumer.runPollLoop(consumer, POLL_TIMEOUT);
		} finally {
			this.consumerRef.set(null);
			this.running = false;
		}
	}

	private static List<String> parseTopics(String csv) {
		List<String> topics = new ArrayList<>();
		for (String part : csv.split(",")) {
			String t = part.trim();
			if (!t.isEmpty()) {
				topics.add(t);
			}
		}
		return topics;
	}

	@Override
	public void stop() {
		stop(() -> {});
	}

	@Override
	public void stop(Runnable callback) {
		KafkaConsumer<String, String> c = this.consumerRef.get();
		if (c != null) {
			c.wakeup();
		}
		Thread w = this.worker;
		if (w != null) {
			try {
				w.join(30_000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			this.worker = null;
		}
		this.running = false;
		callback.run();
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}
}
