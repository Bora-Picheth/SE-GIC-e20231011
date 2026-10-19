package edu.itc.se.lab01.task3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main2 {
    public static void main(String[] args) throws InterruptedException {

        OrderQueue queue = new BlockingOrderQueue(5);
        
        AtomicInteger totalProcessed = new AtomicInteger(0);
        
        List<Thread> producers = new ArrayList<>();
        List<Thread> consumers = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            Thread c = new Thread(new Consumer(queue, "consumer-" + i, totalProcessed));
            consumers.add(c);
            c.start();
        }
        
        for (int i = 1; i <= 2; i++) {
            Thread p = new Thread(new Producer(queue, 50, 0));
            producers.add(p);
            p.start();
        }
        
        for (Thread p : producers) p.join();
        
        for (int i = 0; i < 3; i++) {
            queue.put(new Order(-1, "POISON", 0));
        }
        
        for (Thread c : consumers) c.join();
        
        System.out.println("Main2 Total processed: " + totalProcessed.get());
        assert totalProcessed.get() == 100 : "Error: Expected 100 but got " + totalProcessed.get();
        System.out.println("Main2 Success! Built-in queue processed 100 orders.");
    }
}