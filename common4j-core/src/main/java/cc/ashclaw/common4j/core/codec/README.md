# codec

Binary-to-text encoding utilities. For standard hex encode/decode, use the JDK's built-in [`java.util.HexFormat`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/HexFormat.html). HexUtils fills the gaps it doesn't cover.

## Classes

### `HexUtils`

Multi-line hex dump for debugging binary data.

```java
byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x21};

System.out.println(HexUtils.dump(data));
// 00000000  48 65 6C 6C 6F 20 57 6F 72 6C 64 21              |Hello World!|

// Dump a slice
HexUtils.dump(buffer, offset, 64);

// Custom line width
HexUtils.dump(data, 0, data.length, 8);  // 8 bytes per line
```

| Method | Returns | Notes |
|---|---|---|
| `dump(bytes)` | `String` | Default 16 bytes per line |
| `dump(bytes, offset, length)` | `String` | Slice, 16 bytes per line |
| `dump(bytes, offset, length, bytesPerLine)` | `String` | Full control |

## Design notes

- Use `java.util.HexFormat` for simple encode/decode — it has been in the JDK since Java 17 and handles delimiters, uppercase, prefix/suffix.
- `HexUtils.dump` offsets are relative to the start of the slice, not the original array — the dump shows what you asked for.
- `HexUtils` constructor throws — pure utility class.
