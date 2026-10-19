package edu.itc.se.lab01.task1;

/**
 * CountdownTask
 */
public class CountdownTask implements Runnable {

    public void run() {
        String name = Thread.currentThread().getName();

        try {
            for (int i = 3; i > 0; i--) {
                System.out.println(name + ": " + i);
                Thread.sleep(200);
            }
            System.out.println(name + " liftoff!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
