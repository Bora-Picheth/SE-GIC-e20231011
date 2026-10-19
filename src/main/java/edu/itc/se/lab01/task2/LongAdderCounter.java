package edu.itc.se.lab01.task2;

import java.util.concurrent.atomic.LongAdder;

public class LongAdderCounter implements Counter {
    private final LongAdder count = new LongAdder();

    @Override 
    public void increment(){
        count.increment();
    }

    @Override 
    public long get(){
        return count.sum();
    }
}
/*
 * Why LongAdder is faster under contention:
 * 
 * AtomicLong uses a single memory address. When 8 threads try to update it at the 
 * exact same time (high contention), the CPU forces them to queue up and constantly 
 * retry their operations, creating a huge bottleneck.
 * 
 * LongAdder solves this by keeping an array of multiple variables (called cells) 
 * under the hood. Different threads hash to different cells and increment them 
 * completely independently without fighting over the same memory space. The final 
 * value is only calculated by summing all the separate cells together when get() 
 * is called.
 */