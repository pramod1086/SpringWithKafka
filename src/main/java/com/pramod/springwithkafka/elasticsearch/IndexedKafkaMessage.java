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

	@Field(type = FieldType.Text)
	private String payload;

	@Field(type = FieldType.Long)
	private long indexedAtEpochMillis;

	protected IndexedKafkaMessage() {
	}

	public IndexedKafkaMessage(String id, String topic, String payload, long indexedAtEpochMillis) {
		this.id = id;
		this.topic = topic;
		this.payload = payload;
		this.indexedAtEpochMillis = indexedAtEpochMillis;
	}

	public String getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public String getPayload() {
		return payload;
	}

	public long getIndexedAtEpochMillis() {
		return indexedAtEpochMillis;
	}
}
