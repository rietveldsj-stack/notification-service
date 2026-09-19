# Notification Service

A real-time notification backend built with Spring Boot. Users subscribe to topics,
admins publish to a topic, and every subscriber with an open connection receives the
message immediately over **Server-Sent Events** — no polling.

Built as a hands-on project to work through push delivery over a long-lived HTTP
connection, and the concurrency and connection-lifecycle problems that come with it.

## Why Server-Sent Events

Notifications are one-directional: the server has something to say, the client only
listens. WebSockets would work, but they buy a bidirectional channel this problem does
not need, and they cost a protocol upgrade and extra infrastructure. SSE is plain HTTP,
survives proxies, and reconnects on its own in the browser. It was the right size for
the job.

## How delivery works

```
POST /api/topics/{id}/notifications   (ADMIN)
              |
              v
      NotificationService.publish()
              |
              ├── load every Subscription for the topic (one fetch-join query)
              ├── build a Notification per subscriber, saveAll()   ← durable record
              └── for each: SseService.sendToUser(userId, ...)     ← live push
                                    |
                                    v
                      Map<Long, List<SseEmitter>>
                                    |
                                    v
                   GET /api/notifications/stream  (open connections)
```

Every notification is **persisted first and pushed second**, so a message is never lost
just because nobody happened to be connected — the durable record exists regardless of
who was listening.

### The connection registry

Open connections live in a `ConcurrentHashMap<Long, List<SseEmitter>>` keyed by user id,
with `CopyOnWriteArrayList` values — one user can have several tabs open, and emitters
are added and removed by different request threads than the one publishing.

Emitters are unregistered on `onCompletion`, `onTimeout` and `onError`, and again when a
send throws `IOException` (a client that vanished without a clean close). When a user's
last emitter goes, the key is removed rather than left behind as an empty list.

Each payload is serialised to JSON **once** per publish and the same string is written to
every emitter, instead of letting the framework re-serialise per connection.

## Features

- JWT authentication — registration and login, stateless sessions, BCrypt hashing
- Role-based access control, enforced with `@PreAuthorize` at the method level
- Topics, created by admins
- Per-user topic subscriptions
- Publish to a topic, fan out to every subscriber
- Live delivery over SSE with a one-hour connection timeout
- Durable notification history in MySQL
- JPA auditing — `createdAt` / `updatedAt` on entities via a `@MappedSuperclass`
- Centralised error handling with a consistent error response body
- CORS configuration for browser clients

## Tech stack

| | |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 (Web MVC, Data JPA, Security, Validation) |
| Transport | Server-Sent Events (`SseEmitter`) |
| Database | MySQL |
| Auth | Spring Security + JJWT |
| Build | Maven |
| Other | Lombok |

## API

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | — | Register a user |
| `POST` | `/api/auth/login` | — | Log in, returns a JWT |
| `POST` | `/api/topics` | ADMIN | Create a topic |
| `POST` | `/api/subscriptions/{topicId}` | JWT | Subscribe to a topic |
| `GET` | `/api/subscriptions` | JWT | List your subscriptions |
| `POST` | `/api/topics/{topicId}/notifications` | ADMIN | Publish to a topic |
| `GET` | `/api/notifications/stream?token=…` | JWT | Open the SSE stream |

The stream endpoint takes the JWT as a query parameter rather than an `Authorization`
header, because the browser `EventSource` API cannot set custom headers.

## Running it

**Prerequisites:** Java 17, Maven, MySQL.

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

The example file reads credentials from the environment, so export them before starting:

```bash
export DB_USERNAME=your_db_user DB_PASSWORD=your_db_password JWT_SECRET=your_base64_secret
```

```bash
./mvnw spring-boot:run
```

Watching the stream from the command line:

```bash
curl -N "http://localhost:8081/api/notifications/stream?token=$JWT"
```

## Tests

```bash
./mvnw test
```

Unit tests cover the topic and subscription services. Delivery is not yet covered.

## Status and next steps

This is a learning project, not production software. Known gaps I'd address next:

- No tests around SSE delivery or emitter cleanup — the most interesting part is the
  least tested part
- The emitter registry is in-process, so this does not survive being run on more than one
  instance; a shared broker would be needed to fan out across nodes
- Notifications are persisted but there is no endpoint to read them back, so a user who
  was offline at publish time cannot retrieve what they missed. A `GET /api/notifications`
  history endpoint is the obvious next addition
- No `Last-Event-ID` replay, so a reconnecting client resumes from nothing rather than
  from where it left off
- `logging.level.org.springframework.security=DEBUG` is a development setting
