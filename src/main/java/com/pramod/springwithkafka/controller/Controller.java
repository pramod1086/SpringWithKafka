package com.pramod.springwithkafka.controller;

import com.pramod.springwithkafka.producer.KafkaProducerService;
import com.pramod.springwithkafka.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/kafka")
public class Controller {
    private final KafkaProducerService kafkaProducer;
    private final Producer producer;

    @Autowired
    Controller(KafkaProducerService kafkaProducer, Producer producer) {
        this.kafkaProducer = kafkaProducer;
        this.producer = producer;
    }

    @PostMapping(value = "/publish")
    public void sendMessageToKafkaTopic(
            @RequestParam("message") String message,
            @RequestParam(value = "partition", required = false) Integer partition) {
        if (partition != null) {
            this.kafkaProducer.sendMessage(message, partition);
            this.producer.sendMessage(message);
        } else {
            this.kafkaProducer.sendMessage(message);
            this.producer.sendMessage(message);
        }
    }
}
