package com.pramod.springwithkafka.kafka;

import com.pramod.springwithkafka.model.User;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

/**
 * Custom Kafka partitioner for {@link UserPartitionKey} or {@link User} keys.
 * Same key → same partition; {@link User} uses name and age in the routing hash.
 */
public class UserPartitionPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                         Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.availablePartitionsForTopic(topic);
        int numPartitions = partitions.size();
        if (numPartitions == 0) return 0;

        String partitionKey;
        if (key instanceof UserPartitionKey) {
            partitionKey = ((UserPartitionKey) key).toPartitionKey();
        } else if (key instanceof User) {
            User u = (User) key;
            partitionKey = u.getName() + ":" + u.getAge();
        } else if (key != null) {
            partitionKey = key.toString();
        } else {
            partitionKey = "null";
        }

        return Math.abs(partitionKey.hashCode()) % numPartitions;
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs) {}
}
