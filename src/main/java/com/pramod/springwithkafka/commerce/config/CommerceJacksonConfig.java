package com.pramod.springwithkafka.commerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4 favors Jackson 3; this app still uses Jackson 2 for Kafka JSON payloads
 * (aligned with {@code spring-kafka} serializers and {@code jackson-databind} on the classpath).
 */
@Configuration
public class CommerceJacksonConfig {

	@Bean
	public ObjectMapper commerceKafkaObjectMapper() {
		return new ObjectMapper();
	}
}
