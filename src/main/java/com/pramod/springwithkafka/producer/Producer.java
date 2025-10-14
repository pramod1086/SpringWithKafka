package com.pramod.springwithkafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private static final String TOPIC = "users";
    
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String message) {

        for(int i =0;i<10;i++){
            String key = "id"+" "+i;
            logger.info(String.format("#### -&gt; Producing message -&gt; %s", message));
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC,key, message);

            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    // Perform actions on successful delivery
                }

                @Override
                public void onFailure(Throwable ex) {
                    // Perform actions on delivery failure, e.g., logging, error handling
                }
            });
        }

      //  this.kafkaTemplate.send(TOPIC, message);
    }
}
