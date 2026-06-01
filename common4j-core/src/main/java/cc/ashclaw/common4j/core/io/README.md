# io

Everyday file and classpath operations that are still verbose with the JDK alone.

## Classes

### `IoUtils`

Recursive delete, directory size, and classpath resource reading — one line each.

```java
// Recursive delete (rm -rf)
IoUtils.deleteRecursive(Path.of("/tmp/build"));

// Directory size
long bytes = IoUtils.size(Path.of("/data"));

// Classpath resources
String content = IoUtils.resourceAsString("config/defaults.json");
String sql = IoUtils.resourceAsString(MyDao.class, "init.sql");
byte[] data = IoUtils.resourceAsBytes(MyDao.class, "schema.bin");
```

| Method | Returns | Notes |
|---|---|---|
| `deleteRecursive(path)` | `void` | `rm -rf` — walks tree, deletes files then directories |
| `size(path)` | `long` | Bytes; returns 0 if path does not exist |
| `resourceAsString(resource)` | `String` | UTF-8, uses `IoUtils` classloader |
| `resourceAsString(context, resource)` | `String` | UTF-8, resolves relative paths against `context`'s package |
| `resourceAsBytes(context, resource)` | `byte[]` | Raw bytes, same resolution rules |

### Resource path resolution

```java
// Absolute — starts with "/" → resolved from classpath root
IoUtils.resourceAsString(MyDao.class, "/sql/init.sql");

// Relative — no leading "/" → resolved against context class's package
IoUtils.resourceAsString(MyDao.class, "init.sql");
// If MyDao is in com.example.db, resolves to "/com/example/db/init.sql"
```

## Design notes

- **`deleteRecursive`** — unlike `Files.delete()`, does not require the directory to be empty. Symlinks are deleted without following.
- **`size`** — follows symlinks; uses `AtomicLong` for thread-safe accumulation even though traversal is single-threaded (defensive).
- **Resource methods throw `IllegalArgumentException`** if the resource is not found — fail fast with a clear message.
- **Resource methods throw `UncheckedIOException`** on read failure — keeps the API usable in streams/lambdas.
- All methods reject `null` with `NullPointerException`.
- `IoUtils` constructor throws — pure utility class.
