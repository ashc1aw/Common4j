# common4j-retry

Lightweight retry with exponential backoff and jitter — fills a gap the JDK leaves completely open.

## Philosophy

Zero dependencies. Thread-safe. Virtual Thread friendly. One `Retryer` instance, shared everywhere.

## Quick Start

```java
// Default config: 3 attempts, 200ms initial, 2x backoff, 20% jitter
var retryer = new Retryer(RetryConfig.DEFAULTS);

// Execute with retry
String result = retryer.execute(() -> unstableService.fetch());

// Or runnable-style
retryer.executeRunnable(() -> cache.warmUp());
```

## Custom Configuration

```java
var config = RetryConfig.builder()
    .maxAttempts(5)
    .initialDelay(500)
    .maxDelay(Duration.ofSeconds(10))
    .backoffMultiplier(2.0)
    .jitter(0.3)
    .retryOn(IOException.class, TimeoutException.class)
    .build();

var retryer = new Retryer(config);
String result = retryer.execute(() -> httpClient.get(url));
```

## API Overview

| Class | Purpose |
|---|---|
| `RetryConfig` | Immutable config record with `Builder` |
| `Retryer` | Executor — `execute(Callable)`, `executeRunnable(Runnable)` |
| `RetryExhaustedException` | Thrown when all attempts fail; carries last cause |

### RetryConfig defaults

| Property | Default | Notes |
|---|---|---|
| `maxAttempts` | 3 | 1 = no retry |
| `initialDelay` | 200ms | Delay before first retry |
| `maxDelay` | 30s | Cap on exponential growth |
| `backoffMultiplier` | 2.0 | Each delay = previous × multiplier |
| `jitter` | 0.2 | ±20% random variance |
| `retryOn` | `Exception.class` | Only exceptions, never `Error` |

## Design Notes

- **Errors are never retried** — `OutOfMemoryError`, `StackOverflowError`, etc. are rethrown immediately regardless of `retryOn`.
- **Non-matching exceptions fail fast** — if `retryOn` is `IOException.class` and the task throws `IllegalStateException`, it's wrapped in `RetryExhaustedException` and thrown immediately (1 attempt).
- **Thread-safe** — config is immutable, `Retryer` holds no mutable shared state. One instance per application is fine.
- **Virtual Thread friendly** — uses `Thread.sleep()`, which plays well with VT scheduling. No pinning.
- **No external dependencies** — pure JDK.

## Why Not `@Retryable`?

Annotations require AOP/proxy infrastructure (dynamic proxies, ByteBuddy, CGLIB) to do anything. Without a DI container, `@Retryable` is dead metadata. Spring already provides this for DI environments. This module stays dependency-free and programmatic.

For zero-boilerplate interface wrapping, `Retryer.proxy()` is available:

```java
var client = Retryer.proxy(ApiClient.class, realClientImpl, config);
client.fetch(); // auto-retries on failure
```

## Maven

```xml
<dependency>
    <groupId>cc.ashclaw</groupId>
    <artifactId>common4j-retry</artifactId>
    <version>2.0.0</version>
</dependency>
```
