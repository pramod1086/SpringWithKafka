package com.pramod.springwithkafka.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

/**
 * Kafka Streams topology: the first building block is a <strong>source node</strong>—a
 * {@link KStream} created with {@link StreamsBuilder#stream(String, Consumed)} that
 * continuously reads records from a topic into the stream graph.
 */
@Configuration
@EnableKafkaStreams
public class TransactionsSourceTopology {

    public static final String SOURCE_TOPIC = "transactions";
    /** Sink used so the graph has a terminal edge; swap for your operators (map, filter, join, etc.). */
    public static final String STUB_SINK_TOPIC = "transactions.streams.processed";

    @Bean
    public KStream<String, String> transactionsStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> source = sourceNode(streamsBuilder);
        // Without a downstream terminal (e.g. to, through, or materialized state), the topology may not run.
        source.to(STUB_SINK_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
        return source;
    }

    /** Source node: attaches this application to {@link #SOURCE_TOPIC} as the stream input. */
    private static KStream<String, String> sourceNode(StreamsBuilder builder) {
        return builder.stream(SOURCE_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
    }
}
