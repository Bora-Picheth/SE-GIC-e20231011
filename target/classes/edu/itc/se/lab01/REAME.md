# Lab 01: Java Concurrency and Multithreading
**Student:** Bora Picheth
**Package:** `edu.itc.se.lab01`

---

## Task 1 & 2: Thread Synchronization and Performance
**Objective:** Benchmark different thread synchronization mechanisms under high contention (8 threads, 1,000,000 increments each).

### Benchmark Results
*   **UnsafeCounter:** ~51 ms (Failed: Lost increments due to data races)
*   **SynchronizedCounter:** ~216 ms (Correct, but slowest due to heavy locking)
*   **LockCounter:** ~69 ms (Correct, faster intrinsic locking via `ReentrantLock`)
*   **AtomicCounter:** ~58 ms (Correct, hardware-level Compare-And-Swap)
*   **LongAdderCounter:** ~7 ms (Correct, fastest due to distributed memory cells)

### Q: Why does making only `increment()` synchronized but not `get()` still leave a bug?
While synchronizing `increment()` prevents multiple threads from overwriting each other (fixing the data race), leaving `get()` unsynchronized creates a **memory visibility** bug due to the Java Memory Model:
1. **Stale Data:** Modern CPUs keep variables in local core caches. A `synchronized` method forces the writing thread to flush changes to main memory. Without synchronizing `get()`, the reading thread is not forced to fetch from main memory and may read a stale value.
2. **Word Tearing:** In Java, reads/writes to 64-bit primitives (`long`) are not guaranteed to be atomic. An unsynchronized `get()` could read a corrupted, half-written value if accessed at the exact nanosecond of an update.
3. **Missing "Happens-Before" Link:** To guarantee a reader thread sees the exact state left by a writer thread, both threads must synchronize on the exact same lock.

---

## Task 3: Bounded Buffer (Monitor Pattern)
**Objective:** Implement a Producer-Consumer pattern using `wait()` and `notifyAll()` within a custom `BoundedBuffer`.

### Q: What breaks when you change `notifyAll()` to `notify()` and the `while` loop to an `if` statement?
Making these two changes causes the program to sporadically **crash** or **deadlock**. 
* **Why `if` breaks (The Crash):** A `while` loop protects against spurious wakeups. If changed to an `if` statement, a waiting Consumer that wakes up will skip re-checking the condition. If another thread already emptied the buffer in the meantime, the Consumer blindly attempts to remove an item from an empty queue, throwing a `NoSuchElementException`.
* **Why `notify()` breaks (The Deadlock):** `notifyAll()` wakes all threads, but `notify()` wakes only one randomly. If the buffer is full and a Consumer removes an item, it calls `notify()`. If the thread scheduler accidentally wakes another Consumer instead of a Producer, that Consumer takes an item and calls `notify()`. Eventually, a Producer might wake another Producer, which sees a full buffer and goes to sleep. The signal is "lost," leaving all Producers and Consumers permanently waiting (deadlock).

---

## Task 4: Executors, Futures, and Virtual Threads
**Objective:** Compare thread management using `ExecutorService`, platform threads, and virtual threads for CPU-bound and I/O-bound tasks.

### Q: What is the difference between the 20 Platform Threads and Virtual Threads for I/O-bound work?
* **Platform Threads (~5210 ms):** A fixed pool of 20 OS-level threads can only process 20 downloads simultaneously. Because each simulated download blocks the thread for 200ms, the pool takes roughly 25 sequential "batches" to process all 500 tasks, creating a massive bottleneck.
* **Virtual Threads (~1230 ms):** Virtual threads are extremely lightweight, allowing the JVM to run all 500 tasks concurrently. When a virtual thread hits the 200ms network sleep, it unmounts and yields the underlying OS thread, preventing blocking. Total execution time drops to roughly 1000ms (dictated entirely by the 1-second timeout of the single hanging task, while all others finish concurrently in 200ms).

---

## Task 5: Deadlock Detection and Resolution
**Objective:** Induce a thread deadlock, analyze it using JDK tools (`jstack`), and resolve it using two different locking strategies.

### Deadlock Evidence (`jstack`)
The original `Bank` implementation caused a circular wait because threads locked accounts based on argument order rather than a consistent hierarchy. 
*(See `deadlock.txt` for the full JVM thread dump confirming the circular wait between transfer threads).*

### Resolution Strategies
1. **OrderedBank (Fix A):** Resolved the deadlock by establishing a strict lock hierarchy. Threads always acquire the lock for the `Account` with the smaller ID first, breaking the circular wait condition entirely. 
2. **TryLockBank (Fix B):** Replaced intrinsic `synchronized` blocks with `ReentrantLock`. Implemented a `tryLock` loop with a timeout (10ms). If a thread fails to acquire both locks, it releases any held locks and backs off for a random duration (5-15ms) before retrying, preventing a permanent freeze. Added cooperative cancellation using `Thread.currentThread().isInterrupted()` for graceful shutdowns.