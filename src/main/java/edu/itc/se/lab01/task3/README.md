### Q: What breaks when you change `notifyAll()` to `notify()` and the `while` loop to an `if` statement?

**Answer:**
If you make these two changes and run the program in a loop, the program will sporadically fail in two catastrophic ways: it will either **crash** or **freeze forever (deadlock)**. 

Here is exactly why each change breaks the multithreaded architecture:

#### 1. Why `if` breaks (The Spurious Wakeup & State Change Problem)
When you change the `while` loop to an `if` statement, waiting threads do not re-check the condition when they wake up. 
* **The Crash:** Imagine the buffer is empty and two Consumer threads are in `wait()`. A Producer adds exactly *one* item and wakes them up. The first Consumer grabs the item, making the buffer empty again. If you used an `if` statement, the second Consumer wakes up, skips the condition check, blindly tries to remove an item from the empty queue, and crashes the program with a `java.util.NoSuchElementException`. 
* **The Fix:** A `while` loop forces the second Consumer to re-evaluate `items.isEmpty()`, realize the item is already gone, and safely go back to `wait()`.

#### 2. Why `notify()` breaks (The Lost Wakeup / Deadlock Problem)
`notifyAll()` wakes up *every* thread waiting on the lock, but `notify()` wakes up only *one* randomly chosen thread.
* **The Freeze:** Imagine the buffer is completely full. Two Producers try to add an item and enter `wait()`. A Consumer takes one item, creating one empty slot, and calls `notify()`. Instead of waking up a Producer, the Java thread scheduler might accidentally wake up the *other* sleeping Consumer (if there was one). That Consumer takes an item and calls `notify()`, which wakes up a Producer. Eventually, due to bad luck over thousands of iterations, a Producer might wake up another Producer instead of a Consumer. The woken Producer sees the buffer is full and goes to sleep. Because the signal was "lost" on the wrong type of thread, all Producers and all Consumers end up permanently asleep.
* **The Fix:** `notifyAll()` ensures that *all* threads wake up, guaranteeing that the correct type of thread (a Consumer when full, a Producer when empty) will get a chance to execute.