package com.pramod.springwithkafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class Producer<U, S> {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private static final String TOPIC = "users";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {

        for (int i = 0; i < 10; i++) {
            String key = "id" + " " + i;
            logger.info(String.format("#### -&gt; Producing message -&gt; %s", message));
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, key, message);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.warn("Kafka send failed", ex);
                }
            });
        }
    }
}
