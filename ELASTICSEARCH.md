# Elasticsearch in SpringWithKafka

This document describes how Kafka records flow into Elasticsearch, the index shape, how to run it locally, and how to query indexed data (including via the app’s HTTP search API).

## Overview

| Piece | Role |
|--------|------|
| **`KafkaElasticsearchIndexer`** | `@KafkaListener` consumes Kafka and saves each message into Elasticsearch. |
| **`IndexedKafkaMessage`** | Document model for index **`kafka-messages`**. |
| **`IndexedKafkaMessageRepository`** | Spring Data Elasticsearch repository (creates/updates index mapping on startup when enabled). |
| **`IndexedKafkaMessageSearchService`** | Criteria queries over `kafka-messages` (enabled when repositories are on). |
| **`ElasticsearchSearchRestController`** | `GET /api/elasticsearch/messages` for paged search. |
| **`PlainKafkaElasticsearchIndexer`** | Standalone `main` that does the same indexing without Spring (see `README.md`). |

## Topics written to the index

The Spring listener indexes these topics into **`kafka-messages`** (same document layout for all):

- `users`
- `transactions`
- `commerce.catalog.events`
- `commerce.cart.events`

Each document stores:

| Field | ES type | Description |
|--------|---------|-------------|
| `id` | keyword (id) | Random UUID per indexed row. |
| `topic` | keyword | Kafka topic name. |
| `messageKey` | keyword | Kafka record key when present (string producers). |
| `payload` | text | Full Kafka value (JSON string for commerce). |
| `indexedAtEpochMillis` | long | Ingest time (epoch ms). |

## Enabling Elasticsearch in the Spring Boot app

1. Start Elasticsearch (e.g. `docker compose up -d` from this project).
2. Run with the **`elasticsearch` profile** so repositories and the indexer are active:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=elasticsearch
```

Default `application.properties` sets `spring.data.elasticsearch.repositories.enabled=false` so the app starts without a cluster. Profile file `application-elasticsearch.properties` sets it to `true`.

Connection: `spring.elasticsearch.uris` (default `http://localhost:9200`).

## Consumer group and new topics

Indexer consumer group: `elasticsearch.kafka.consumer-group` (default `elasticsearch-indexer`).

If you **add topics** to the listener after data was already consumed, either use a **new** consumer group id or reset offsets for those partitions; otherwise the consumer may not read historical commerce events from the beginning.

## HTTP search API

When repositories are enabled, the app exposes:

```http
GET /api/elasticsearch/messages?topic=<optional>&q=<optional>&page=0&size=20
```

- **`topic`** — exact match on the `topic` field (e.g. `commerce.catalog.events`).
- **`q`** — analyzed text search on **`payload`** (good for keywords inside JSON).
- At least one of `topic` or `q` is required; otherwise **400** with `{"error":"..."}`.
- **`size`** is capped at 100.

Example:

```bash
curl -s "http://localhost:9000/api/elasticsearch/messages?topic=commerce.cart.events&page=0&size=5"
curl -s "http://localhost:9000/api/elasticsearch/messages?q=CART_CREATED&page=0&size=5"
```

## Direct Elasticsearch queries

See `README.md` → **Elasticsearch (Kafka → index)** for `curl` examples against `http://localhost:9200/kafka-messages/_search`.

## Index mapping changes

If you add fields to **`IndexedKafkaMessage`**, Elasticsearch may need a mapping update or reindex. For local dev you can delete the index and let Spring Data recreate it on the next startup (do **not** do this blindly in production).

## Tests

`SpringWithKafkaApplicationTests` disables Elasticsearch autoconfiguration and sets `spring.data.elasticsearch.repositories.enabled=false`, so the search beans are not loaded during tests.
