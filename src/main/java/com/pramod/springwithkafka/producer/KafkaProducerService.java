package com.pramod.springwithkafka.producer;

import com.pramod.springwithkafka.kafka.UserPartitionPartitioner;
import com.pramod.springwithkafka.model.User;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "transactions";

    public void sendMessage(String message) {
        sendMessage(message, null);
    }

    /**
     * @param partition if non-null, record is routed to this partition (partitioner is skipped);
     *                  if null, {@link UserPartitionPartitioner} chooses from the message key
     */
    public void sendMessage(String message, Integer partition) {
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, UserPartitionPartitioner.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");



        Callback callback = (metadata, exception) -> {
            if (exception != null) {
                System.out.println("Error sending message: " + exception.getMessage());
            } else {
                System.out.println("Message sent — partition: " + metadata.partition()
                        + ", offset: " + metadata.offset());
            }
        };

        try (KafkaProducer<User, String> producer = new KafkaProducer<>(properties)) {
            User user = new User("pramod", 20);
            ProducerRecord<User, String> record = partition != null
                    ? new ProducerRecord<>(TOPIC, partition, user, message)
                    : new ProducerRecord<>(TOPIC, user, message);
            producer.send(record, callback);
        }
    }
}
