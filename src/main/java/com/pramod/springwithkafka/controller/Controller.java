package com.pramod.springwithkafka.controller;

import com.pramod.springwithkafka.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/kafka")
public class Controller {
    private final KafkaProducerService kafkaProducer;

    @Autowired
    Controller(KafkaProducerService kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping(value = "/publish")
    public void sendMessageToKafkaTopic(
            @RequestParam("message") String message,
            @RequestParam(value = "partition", required = false) Integer partition) {
        if (partition != null) {
            this.kafkaProducer.sendMessage(message, partition);
        } else {
            this.kafkaProducer.sendMessage(message);
        }
    }
}
