package edu.itc.se.lab01.task3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
public class Main {
    public static void main(String[] args ) throws InterruptedException {

        long startTime = System.currentTimeMillis();


        OrderQueue queue = new BoundedBuffer(5);
        AtomicInteger totalProcessed = new AtomicInteger(0);

        List<Thread> producers = new ArrayList<>();
        List<Thread> consumers = new ArrayList<>();


        for(int i = 1; i <= 3; i++){
            Thread c = new Thread(new Consumer(queue, "consumer-" + i, totalProcessed));
            consumers.add(c);
            c.start();
        }

        
        for(int i = 1; i <=2; i++){
            Thread p = new Thread(new Producer(queue, 50, 0));
            producers.add(p);
            p.start();
        }

        for(Thread p : producers){
            p.join();
        }
        System.out.println("--- All producers finished. Sending Poison Pills... ---");

        for(int i = 0 ; i < 3; i++){
            queue.put(new Order(-1, "POISON", 0));
        }


        for(Thread c : consumers){
            c.join();
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Total processed: %d (expected 100) in %d ms%n", totalProcessed.get(), (endTime - startTime));

        
    }
}
