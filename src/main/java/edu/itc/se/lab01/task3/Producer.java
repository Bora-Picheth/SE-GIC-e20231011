package edu.itc.se.lab01.task3;

import java.util.Random;

public class Producer implements Runnable{
    private final OrderQueue queue;
    private final int numOrders;
    private final int numConsumers;
    private final Random random =  new Random();

    public Producer(OrderQueue queue, int numOrders, int numConsumers){
        this.queue = queue;
        this.numOrders = numOrders;
        this.numConsumers = numConsumers;
    }

    @Override 
    public void run(){
        try {
            // 1. Produce N orders

            for(int i = 1; i <= numOrders; i++){
                // Sleep for a random time between 0 and 20 milliseconds
                Thread.sleep(random.nextInt(21));

                // Generate a random quantity between 1 and 5
                int qty = random.nextInt(5) + 1;

                // Create and put the order into the queue
                Order order = new Order(i, "item-" + i, qty);
                queue.put(order);

                System.out.println("Produced: " + order);
            }

            // 2. Send the poison pills
            for(int i = 0; i < numConsumers; i++){
                Order poisonPill = new Order(-1, "POISON", 0);
                queue.put(poisonPill);
                System.out.println("Produced: POISON PILL sent.");
            }

        } catch (Exception e) {
            System.out.println("Produced was interrupted.");
            Thread.currentThread().interrupt();
        }
    }

}
