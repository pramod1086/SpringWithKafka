# SpringWithKafka

Spring Boot app that publishes messages to Kafka. Messages to the `transactions` topic use a custom partitioner (`UserPartitionPartitioner`) keyed by `User` (name + age), or you can target a partition explicitly from the HTTP API.

Records on the **`users`** and **`transactions`** topics are also indexed into **Elasticsearch** by a dedicated consumer (`KafkaElasticsearchIndexer`, consumer group `elasticsearch-indexer`). Documents land in the **`kafka-messages`** index (see `src/main/java/com/pramod/springwithkafka/elasticsearch/`).

## Running Kafka and Elasticsearch (Docker)

```bash
docker compose up -d
```

- **Kafka** broker: `localhost:9092` (see `docker-compose.yml`).
- **Elasticsearch**: `http://localhost:9200` (single-node, security disabled for local dev).

Start Elasticsearch before or with the app so indexing and health checks can reach the cluster. Connection URI is `spring.elasticsearch.uris` in `src/main/resources/application.properties`.

## Run the Spring Boot app

The API is only reachable while the app is running. From the project root:

```bash
./mvnw spring-boot:run
```

(or `mvn spring-boot:run` if you use a local Maven install)

The server listens on **port 9000** (`server.port` in `application.properties`). If `curl` reports “Failed to connect”, start the app first or confirm nothing else is using that port (`lsof -i :9000` on macOS).

## Kafka partitions

Partitions belong to the **topic**. The producer only **selects** a partition; the topic must already exist with enough partitions (indices `0` … `N-1`).

### Create a topic with multiple partitions

If you see `Topic 'transactions' already exists`, skip `--create` and use **describe** (below) or **alter** to add partitions. Do not delete a topic in production without a plan.

Inside the Kafka container, `kafka-topics.sh` is usually under `/opt/kafka/bin`. If that path fails, locate the script:

```bash
docker compose exec kafka find / -name "kafka-topics.sh" 2>/dev/null
```

Create `transactions` with 3 partitions and replication factor 1 (typical for local dev):

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic transactions \
  --partitions 3 \
  --replication-factor 1
```

### Increase partitions on an existing topic

Partition count can only go **up**, not down:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --alter \
  --topic transactions \
  --partitions 6
```

### Inspect a topic

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic transactions
```

### Auto-created topics

If the broker creates the topic on first produce (`auto.create.topics.enable`), it often defaults to **one** partition. For predictable partitioning, create the topic with `--partitions` **before** sending messages, or adjust the broker’s `num.partitions` for your environment.

## HTTP API

| Method | Path | Parameters |
|--------|------|------------|
| `POST` | `/kafka/publish` | `message` (required), `partition` (optional) |

- **Without `partition`:** the record is routed by the custom partitioner using the `User` key.
- **With `partition`:** the record goes to that partition index (partitioner is skipped). The index must be valid for the topic.

Example:

```bash
curl -X POST "http://localhost:9000/kafka/publish?message=%7B%22item%22%3A%22book%22%7D"
curl -X POST "http://localhost:9000/kafka/publish?message=hello&partition=1"
curl -X POST "http://localhost:9000/kafka/publish?message=hello world"
```

Server port is configured in `src/main/resources/application.properties`.

## Elasticsearch (Kafka → index)

- **Topics indexed:** `users`, `transactions`.
- **Consumer group:** `elasticsearch-indexer` (override with `elasticsearch.kafka.consumer-group` in `application.properties`).
- **Index:** `kafka-messages` — each document has an id (UUID), `topic` (keyword), `payload` (text, the Kafka value string), and `indexedAtEpochMillis` (long).

### Check the cluster

```bash
curl -s http://localhost:9200/
```

You should see JSON with `cluster_name` and `version`.

### Check the index

Index metadata (mappings, settings):

```bash
curl -s http://localhost:9200/kafka-messages
```

HTTP status only (e.g. `200` if the index exists):

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9200/kafka-messages
```

### Search with `curl`

**Match all documents** (pretty-printed):

```bash
curl -s -H "Content-Type: application/json" \
  "http://localhost:9200/kafka-messages/_search?pretty" \
  -d '{"query":{"match_all":{}}}'
```

**Search text in `payload`:**

```bash
curl -s -H "Content-Type: application/json" \
  "http://localhost:9200/kafka-messages/_search?pretty" \
  -d '{"query":{"match":{"payload":"your text here"}}}'
```

**Filter by `topic` (exact keyword):**

```bash
curl -s -H "Content-Type: application/json" \
  "http://localhost:9200/kafka-messages/_search?pretty" \
  -d '{"query":{"term":{"topic":"users"}}}'
```

**Quick search (GET, no body)** — returns hits; useful for a fast sanity check:

```bash
curl -s "http://localhost:9200/kafka-messages/_search?pretty"
```

### Kibana

If you run Kibana against the same cluster, use **Dev Tools** and run the same queries, for example:

```http
GET kafka-messages/_search
{
  "query": { "match_all": {} }
}
```

Unit tests disable Elasticsearch via `src/test/resources/application.properties` so the Spring context loads without a running cluster.

## Related code

- `KafkaProducerService` — producer config, optional explicit partition.
- `com.pramod.springwithkafka.kafka.UserPartitionPartitioner` — routing for `User` and `UserPartitionKey` keys.
- `com.pramod.springwithkafka.elasticsearch` — `IndexedKafkaMessage`, `IndexedKafkaMessageRepository`, `KafkaElasticsearchIndexer`.
