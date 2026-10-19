### Q: Why does making only `increment()` synchronized but not `get()` still leave a bug?

**Answer:**
While synchronizing `increment()` prevents multiple threads from overwriting each other (fixing the **data race**), leaving `get()` unsynchronized creates a **memory visibility** bug due to the Java Memory Model.

1. **Stale Data (CPU Caching):** Modern CPUs keep variables in local core caches. A `synchronized` method forces the writing thread to flush its changes to main memory. However, if `get()` is not synchronized, the reading thread is not forced to fetch from main memory and might read an old, stale value from its local CPU cache.
2. **64-bit Word Tearing:** In Java, reads and writes to 64-bit primitives like `long` are not guaranteed to be atomic. If a thread calls an unsynchronized `get()` at the exact nanosecond another thread is updating the `long`, the reader might get a corrupted, half-written value (mixing old and new 32-bit halves).
3. **Missing "Happens-Before" Link:** To guarantee that a reader thread sees the exact, most up-to-date state left by a writer thread, *both* threads must synchronize on the exact same lock.

**Conclusion:** To ensure thread safety, you must either make `get()` synchronized as well, or declare the underlying `count` variable as `volatile`.