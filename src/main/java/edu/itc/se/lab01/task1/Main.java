package edu.itc.se.lab01.task1;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        boolean sequential = args.length > 0 && args[0].equals("--sequential");

        Thread apollo = new Countdown("apollo");
        Thread gemini = new Thread(new CountdownTask(), "gemini");
        Thread mercury = new Thread(() -> {
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
        }, "mercury");
        Thread artemis = Thread.ofVirtual().name("artemis").unstarted(new CountdownTask());

        // TODO: start (or run) all four depending on 'sequential'

        Thread[] arr = { apollo, gemini, mercury, artemis };

        for (Thread thread : arr) {
            if (sequential) {
                thread.run();
            } else {
                thread.start();
            }

        }
        System.out.println("Mission control: all launched");

        // TODO: join all four

        for (Thread thread2 : arr) {
            thread2.join();
        }
        System.out.println("Mission control: all landed");
        // TODO: print name, isVirtual(), getState() of each

        for (Thread thread3 : arr) {
            System.out.println(thread3.getName() + " " + "virtual=" + thread3.isVirtual() + " " + "state="
                    + thread3.getState());
        }

    }
}