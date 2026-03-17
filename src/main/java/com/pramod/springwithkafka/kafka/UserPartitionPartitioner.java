package com.pramod.springwithkafka.kafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

/**
 * Custom Kafka partitioner using username, id, and age.
 * Records with the same (username, id, age) go to the same partition.
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
