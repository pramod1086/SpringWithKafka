# SpringWithKafka

Spring Boot app that publishes messages to Kafka. Messages to the `transactions` topic use a custom partitioner (`UserPartitionPartitioner`) keyed by `User` (name + age), or you can target a partition explicitly from the HTTP API.

Optionally, records on **`users`**, **`transactions`**, **`commerce.catalog.events`**, and **`commerce.cart.events`** can be indexed into **Elasticsearch** by `KafkaElasticsearchIndexer` (consumer group `elasticsearch-indexer`) into the **`kafka-messages`** index. That requires a running ES cluster and the **`elasticsearch` Spring profile** (see [Run the Spring Boot app](#run-the-spring-boot-app)); by default repositories are off so the app starts with **Kafka only**. Commerce payloads are stored as JSON in the `payload` text field alongside the `topic` keyword.

The app also exposes a **commerce** REST API (categories, products, shopping carts, **store inventory and store search**). Endpoints, JSON bodies, and `curl` examples are in [Commerce (catalog, cart, and stores)](#commerce-catalog-cart-and-stores). Catalog and cart mutations publish to Kafka topics **`commerce.catalog.events`** and **`commerce.cart.events`** for future microservice boundaries.

## Running Kafka and Elasticsearch (Docker)

```bash
docker compose up -d
```

- **Kafka** broker: `localhost:9092` (see `docker-compose.yml`).
- **Elasticsearch**: `http://localhost:9200` (single-node, security disabled for local dev).

Connection URI is `spring.elasticsearch.uris` in `src/main/resources/application.properties`. For Kafka → Elasticsearch indexing, start Elasticsearch **and** run the app with the **`elasticsearch`** profile (see below).

## Run the Spring Boot app

The API is only reachable while the app is running. From the project root:

```bash
./mvnw spring-boot:run
```

(or `mvn spring-boot:run` if you use a local Maven install)

**With Elasticsearch indexing** (after `docker compose up -d` so Kafka and ES are up):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=elasticsearch
```

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

The app exposes a legacy **Kafka publish** endpoint and the **commerce** APIs documented under [Commerce (catalog, cart, and stores)](#commerce-catalog-cart-and-stores).

### Kafka publish

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

### Commerce (catalog, cart, and stores)

Base path: **`http://localhost:9000`**. Controllers live in `com.pramod.springwithkafka.commerce.web`. Data is **in-memory** (restarts clear it). Invalid input returns **HTTP 400** with JSON `{"error":"…"}`.

**Kafka:** mutations publish to **`commerce.catalog.events`** (catalog) and **`commerce.cart.events`** (cart). Topics are created at startup (`CommerceKafkaTopicConfig`). For a future split, cart can build a product read model from `commerce.catalog.events` (see `commerce.kafka.catalog-projection-consumer-enabled` in `application.properties`).

#### Catalog (`/api/commerce/catalog`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/commerce/catalog/categories` | Create a category (optional parent). |
| `GET` | `/api/commerce/catalog/categories` | List all categories. |
| `DELETE` | `/api/commerce/catalog/categories/{id}` | Delete a category. |
| `POST` | `/api/commerce/catalog/products` | Create a product (SKU must be unique). |
| `GET` | `/api/commerce/catalog/products` | List products; optional query `categoryId`. |
| `DELETE` | `/api/commerce/catalog/products/{id}` | Delete a product. |

**Create category** — JSON body:

| Field | Required | Description |
|--------|----------|-------------|
| `name` | yes | Category display name. |
| `parentCategoryId` | no | Existing category id for nesting. |

**Create product** — JSON body:

| Field | Required | Description |
|--------|----------|-------------|
| `sku` | yes | Unique stock-keeping unit. |
| `name` | yes | Product name (e.g. contains `iPhone` for phone search). |
| `categoryId` | yes | Must reference an existing category. |
| `unitPrice` | yes | Decimal price (e.g. `9.99`). |
| `model` | no | Device generation / model key, e.g. `"16"` (used with store search). |
| `storageGb` | no | Storage in gigabytes, e.g. `16` for 16 GB. |
| `color` | no | Color / finish, e.g. `"white"`. |

**List products** — optional query parameter: `categoryId` (filter by category).

Example:

```bash
curl -s "http://localhost:9000/api/commerce/catalog/products?categoryId=<CATEGORY_ID>"
```

#### Stores (`/api/commerce/stores`)

Register **stores** and **on-hand quantities** per catalog `productId`. **Search** returns every store that has **quantity &gt; 0** for at least one product matching the given attributes (all supplied filters must match; omitted filters are ignored except you must pass **at least one** of `name`, `model`, `storageGb`, `color`).

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/commerce/stores` | Create a store (`{ "name": "…" }`). |
| `POST` | `/api/commerce/stores/{storeId}/inventory` | Add stock: `{ "productId": "…", "quantity": <int> }` (quantities accumulate). |
| `GET` | `/api/commerce/stores/search` | Query params: `name`, `model`, `storageGb`, `color` — match catalog products, then list stores stocking any match. |

**Example — find all stores that have iPhone 16, 16 GB, white** (product rows must have been created with matching `name` substring, `model`, `storageGb`, and `color`, and inventory added for those product ids):

```bash
curl -s "http://localhost:9000/api/commerce/stores/search?name=iPhone&model=16&storageGb=16&color=white"
```

#### Cart (`/api/commerce/carts`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/commerce/carts` | Create an empty cart for a customer. |
| `GET` | `/api/commerce/carts/{cartId}` | Get cart with line items. |
| `POST` | `/api/commerce/carts/{cartId}/lines` | Add a line (product must exist / be visible in projection). |
| `DELETE` | `/api/commerce/carts/{cartId}/lines/{lineId}` | Remove one line. |
| `POST` | `/api/commerce/carts/{cartId}/clear` | Remove all lines. |

**Create cart** — JSON body: `{ "customerId": "<string>" }`.

**Add line** — JSON body: `{ "productId": "<uuid from create product>", "quantity": <int >= 1> }`.

**Responses:** `Category` includes `id`, `name`, `parentCategoryId`. `Product` includes `id`, `sku`, `name`, `categoryId`, `unitPrice`, and optional `model`, `storageGb`, `color`. `Store` includes `id`, `name`. `Cart` includes `id`, `customerId`, and `lines` (each `CartLine`: `lineId`, `productId`, `quantity`, `unitPriceSnapshot`, `lineTotal`).

#### Example `curl` sequence

Replace placeholders with ids returned from previous steps.

```bash
# 1) Category
curl -s -X POST http://localhost:9000/api/commerce/catalog/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics"}'

# 2) Product (use category id from step 1)
curl -s -X POST http://localhost:9000/api/commerce/catalog/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"BOOK-1","name":"Kafka guide","categoryId":"<CATEGORY_ID>","unitPrice":19.99}'

# 2b) Example iPhone SKU with variant fields (for store search)
curl -s -X POST http://localhost:9000/api/commerce/catalog/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"IPHONE-16-16GB-WHT","name":"Apple iPhone","categoryId":"<CATEGORY_ID>","unitPrice":799,"model":"16","storageGb":16,"color":"white"}'

# 2c) Store + stock (use store id from POST /stores and product id from 2b)
curl -s -X POST http://localhost:9000/api/commerce/stores \
  -H "Content-Type: application/json" \
  -d '{"name":"Downtown"}'
curl -s -X POST http://localhost:9000/api/commerce/stores/<STORE_ID>/inventory \
  -H "Content-Type: application/json" \
  -d '{"productId":"<IPHONE_PRODUCT_ID>","quantity":3}'

# 2d) Stores carrying iPhone 16 / 16 GB / white
curl -s "http://localhost:9000/api/commerce/stores/search?name=iPhone&model=16&storageGb=16&color=white"

# 3) Cart
curl -s -X POST http://localhost:9000/api/commerce/carts \
  -H "Content-Type: application/json" \
  -d '{"customerId":"user-123"}'

# 4) Line (use cart id and product id from steps 2–3)
curl -s -X POST http://localhost:9000/api/commerce/carts/<CART_ID>/lines \
  -H "Content-Type: application/json" \
  -d '{"productId":"<PRODUCT_ID>","quantity":2}'

# 5) Inspect cart
curl -s http://localhost:9000/api/commerce/carts/<CART_ID>
```

## Elasticsearch (Kafka → index)

- **Topics indexed:** `users`, `transactions`, `commerce.catalog.events`, `commerce.cart.events`.
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

**Filter by `topic` (exact keyword),** for example `users` or commerce streams:

```bash
curl -s -H "Content-Type: application/json" \
  "http://localhost:9200/kafka-messages/_search?pretty" \
  -d '{"query":{"term":{"topic":"users"}}}'
```

```bash
curl -s -H "Content-Type: application/json" \
  "http://localhost:9200/kafka-messages/_search?pretty" \
  -d '{"query":{"term":{"topic":"commerce.cart.events"}}}'
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

By default `spring.data.elasticsearch.repositories.enabled=false` in `application.properties`, so the app starts without a cluster (same idea as tests). Unit tests additionally exclude Elasticsearch auto-configuration via `src/test/resources/application.properties`.

## Related code

- `KafkaProducerService` — producer config, optional explicit partition.
- `com.pramod.springwithkafka.kafka.UserPartitionPartitioner` — routing for `User` and `UserPartitionKey` keys.
- `com.pramod.springwithkafka.elasticsearch` — `IndexedKafkaMessage`, `IndexedKafkaMessageRepository`, `KafkaElasticsearchIndexer`.
- `com.pramod.springwithkafka.commerce` — catalog/cart/store services, Kafka event publishers, `CommerceTopics`, REST in `commerce.web`.
