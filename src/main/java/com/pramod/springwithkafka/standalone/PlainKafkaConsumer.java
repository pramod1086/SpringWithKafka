package com.pramod.springwithkafka.standalone;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Minimal Kafka consumer using only the Apache {@link KafkaConsumer} API (no Spring Kafka).
 * <p>
 * Run (default topic {@code users}): {@code mvn -q compile exec:java}
 * <p>
 * Topics as arguments: {@code mvn -q compile exec:java -Dexec.args="users transactions"}
 */
public final class PlainKafkaConsumer {

	private static final Logger log = LoggerFactory.getLogger(PlainKafkaConsumer.class);

	private static final String BOOTSTRAP_SERVERS = "localhost:9092";
	private static final String GROUP_ID = "plain-java-consumer";

	public static void main(String[] args) {
		List<String> topics = args.length > 0 ? Arrays.asList(args) : List.of("users");

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
			consumer.subscribe(topics);
			log.info("Subscribed to {} (bootstrap={}, group={})", topics, BOOTSTRAP_SERVERS, GROUP_ID);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				log.info("Shutdown requested, waking consumer");
				consumer.wakeup();
			}));

			while (true) {
				try {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
					for (ConsumerRecord<String, String> r : records) {
						log.info("Consumed topic={} partition={} offset={} key={} value={}",
								r.topic(), r.partition(), r.offset(), r.key(), r.value());
					}
				} catch (WakeupException e) {
					log.info("Consumer woken up, closing");
					break;
				}
			}
		}
	}

	private PlainKafkaConsumer() {
	}
}
