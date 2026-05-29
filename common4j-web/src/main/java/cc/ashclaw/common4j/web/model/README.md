# model

Generic API model types used across controllers and services.

## Classes

### `R<T>`

A standard API response envelope with a business code, message, and payload.

**Fields:** `code` (int), `message` (String), `data` (T)

Success is indicated by `code == 0` (legacy) or `code == 200` (HTTP-aligned). Prefer `ResultCode` constants over raw ints.

**Creating responses**

```java
// Using static factories (legacy)
R<User> ok   = R.ok(user);
R<Void> fail = R.fail("not found");

// Using ResultCode (preferred — no magic numbers)
R<User> ok   = ResultCode.SUCCESS.toResponse(user);
R<Void> fail = ResultCode.NOT_FOUND.toResponse("User 42 not found");
R<Void> fail = ResultCode.BAD_REQUEST.toResponse("email is required");
```

**Checking and transforming**

```java
if (r.isSuccess()) { ... }

R<UserDto> dto = r.map(User::toDto);          // only maps on success
```

### `ResultCode`

HTTP-aligned response codes — named constants to replace magic numbers when building `R<T>` responses.

**Constants:** `SUCCESS` (200), `BAD_REQUEST` (400), `UNAUTHORIZED` (401), `FORBIDDEN` (403), `NOT_FOUND` (404), `METHOD_NOT_ALLOWED` (405), `CONFLICT` (409), `TOO_MANY_REQUESTS` (429), `INTERNAL_ERROR` (500), `SERVICE_UNAVAILABLE` (503)

```java
// Four overloads for building R<T> instances
ResultCode.SUCCESS.toResponse(user);              // default msg + data
ResultCode.SUCCESS.toResponse("created", user);   // custom msg + data
ResultCode.NOT_FOUND.toResponse();                // default msg, no data
ResultCode.NOT_FOUND.toResponse("No such user");  // custom msg, no data

// Resolve from external input (e.g. deserialization)
ResultCode code = ResultCode.fromCode(404);       // NOT_FOUND
```

### `PageQuery`

Immutable pagination request — 1-based page, page size clamped to [1, 100].

**Fields:** `page` (int), `size` (int)

```java
var pq = new PageQuery(3, 20);    // page 3, 20 per page
var pq = new PageQuery();         // defaults: page 1, size 20
var pq = PageQuery.of(3);         // page 3, default size

int offset = pq.offset();         // 40 — zero-based, for LIMIT clauses
```

### `PageResult<T>`

A page of records with total count and navigation metadata.

**Fields:** `records` (List\<T\>), `total` (long), `page` (int), `size` (int)

```java
PageResult<User> pr = PageResult.of(users, totalCount, pageQuery);

int pages = pr.totalPages();
boolean more = pr.hasNext();
boolean prev = pr.hasPrev();
boolean empty = pr.isEmpty();

PageResult<UserDto> dtos = pr.map(User::toDto);
```

## Design notes

- All classes are records — immutable, `equals`/`hashCode`/`toString` provided by the compiler.
- `PageQuery` silently clamps out-of-range values instead of throwing — controllers don't need to guard against bad query params.
- `PageResult.of()` wraps the records list with `Collections.unmodifiableList` to prevent accidental mutation.
- `R.map()` and `PageResult.map()` pass through unchanged on failure/empty — safe for chaining in pipelines.
- `ResultCode` codes are HTTP status codes (200, 400, 401, …) — wire-compatible with HTTP response standards.
- `ResultCode.fromCode()` supports deserializing codes from external input without fragile manual mapping.
