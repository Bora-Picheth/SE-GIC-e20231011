package edu.itc.se.lab01.task3;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer implements Runnable {
    private final OrderQueue queue;
    private final String name;
    private final AtomicInteger processCount;
    private final Random random = new Random();

    public Consumer(OrderQueue queue, String name, AtomicInteger processCount) {
        this.queue = queue;
        this.name = name;
        this.processCount = processCount;
    }

    @Override
    public void run() {
        try {
            while (true) {
  
                Order order = queue.take();


                if (order.id() == -1) {
                    System.out.println(name + "received POISON PILL. Shutting down.");
                    break;
                }

                Thread.sleep(random.nextInt(26) + 5);

                System.out.println(name + " processed " + order);

                // 4. Increment the thread-safe counter
                processCount.incrementAndGet();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
