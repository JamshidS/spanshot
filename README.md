# Spanshot

**Send a request, see every span.**

Spanshot is an API client that shows you what your Spring Boot app *actually did* for each request: every method call, SQL query, outbound HTTP call and exception, as a call tree with timings.

```text
POST /api/order/create                         487 ms
└─ OrderController.createOrder                 479 ms
   └─ OrderServiceImpl.createOrder             472 ms
      ├─ ProductRepository.findById ×3          31 ms   ⚠ N+1
      ├─ UserProductOrderRepository.save        83 ms
      └─ OrderItemRepository.saveAll            42 ms
```

> **Status:** early development, heading for v0.1. Nothing is usable yet.

## How it works

```text
UI (React) ──► Spanshot server (local) ──► your Spring Boot app + spanshot-agent
                      ▲                                   │
                      └──────────── spans (OTLP) ─────────┘
```

A Java agent instruments your application's own packages. The local server sends your request, collects the resulting trace and shows it in the UI.

## Repository layout

| Path | What |
|---|---|
| `agent-java/` | The `-javaagent` that records spans |
| `examples/spring-demo/` | Demo Spring Boot app (in-memory H2, no setup) |
| `server/`, `ui/` | Coming in v0.1 |

## Building

Requires JDK 17+ to run Gradle. The JDK toolchains are downloaded automatically if missing.

```bash
./gradlew build
```

Run the demo app with the agent attached:

```bash
java -javaagent:agent-java/build/libs/spanshot-agent-0.1.0-SNAPSHOT.jar=include=com.example \
     -jar examples/spring-demo/build/libs/spring-demo-0.1.0-SNAPSHOT.jar

curl -X POST localhost:8080/api/order/create -H 'Content-Type: application/json' \
     -d '{"userId":1,"products":[{"productId":1,"quantity":2}]}'
```

## License

[Apache 2.0](LICENSE)
