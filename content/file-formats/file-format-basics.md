---
layout: page
title: File format basics
---
The `.LFL` and `.LEC` files are made-up of chunks. Every chunk consists of three parts: *length*, *type* and *value*.
The file format structure is similar to the [TLV](https://en.wikipedia.org/wiki/Type–length–value)
and [IFF](https://en.wikipedia.org/wiki/Interchange_File_Format) standards.

```
┌────────────────────────────────┬────────────────┬────────────────~──┐
│            length              │      type      │       value       │
│           (32 bit)             │    (2 byte)    │ (variable length) │
└────────────────────────────────┴────────────────┴────────────────~──┘
```

* The *length* is a 32-bit number and contains the length of the entire chunk
  (including the length field itself).
* The *type* are 2 bytes of ASCII characters.
* The *value* is a variable structure based on the chunk *type*. It could also contain another
  chunk (tree structure).

The [byte order](https://en.wikipedia.org/wiki/Endianness) of multibyte numbers
(like chunk length) is *little-endian* if not otherwise specified. For example a 32-bit number `0x01234567`
would be stored on disk as `0x67 0x45 0x23 0x01`, a value of decimal `10` would be stored as `0x0A 0x00 0x00 0x00` (when using 32-bit).

**Watch out for these inconsistencies:**

* The *length* field sometimes is simply wrong
* The *type* field sometimes is ambiguous
* Some files are (partially) encrypted

The documentation about the specific file types contains more details about this.
