package com.pramod.springwithkafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Consumer {
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @KafkaListener(topics = "users", groupId = "group_id")
    public void consume(String message) throws IOException {
        logger.info(String.format("First  Consumed message -&gt; %s", message));
    }

    @KafkaListener(topics = "users", groupId = "group")
    public void consumes(String message,  @Header(KafkaHeaders.RECEIVED_PARTITION_ID ) int header){
        logger.info(String.format("Second Consumer Consumed message -&gt; %s", message));
    }

}
