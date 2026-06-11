package com.pramod.springwithkafka.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "kafka-messages")
public class IndexedKafkaMessage {

	@Id
	private String id;

	@Field(type = FieldType.Keyword)
	private String topic;

	/** Kafka record key when string-serialized (same as producers use). */
	@Field(type = FieldType.Keyword)
	private String messageKey;

	@Field(type = FieldType.Text)
	private String payload;

	@Field(type = FieldType.Long)
	private long indexedAtEpochMillis;

	protected IndexedKafkaMessage() {
	}

	public IndexedKafkaMessage(String id, String topic, String messageKey, String payload, long indexedAtEpochMillis) {
		this.id = id;
		this.topic = topic;
		this.messageKey = messageKey;
		this.payload = payload;
		this.indexedAtEpochMillis = indexedAtEpochMillis;
	}

	public String getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public String getPayload() {
		return payload;
	}

	public long getIndexedAtEpochMillis() {
		return indexedAtEpochMillis;
	}
}
