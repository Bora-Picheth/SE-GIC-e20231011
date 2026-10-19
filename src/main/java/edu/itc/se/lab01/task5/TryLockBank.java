package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

public class TryLockBank {
    static final class Account {
        final int id; private long balance;
        final ReentrantLock lock = new ReentrantLock();
        
        Account(int id, long balance) { this.id = id; this.balance = balance; }
        long balance() { return balance; }
        void withdraw(long a) { balance -= a; }
        void deposit(long a)  { balance += a; }
    }

    static final LongAdder retries = new LongAdder();

    static void transfer(Account from, Account to, long amount) throws InterruptedException {
        while (true) {
            
            if (from.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                try {
                    
                    if (to.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                        try {
                            if (from.balance() >= amount) {
                                from.withdraw(amount);
                                to.deposit(amount);
                            }
                            return; 
                        } finally {
                            to.lock.unlock();
                        }
                    }
                } finally {
                    from.lock.unlock();
                }
            }
           
            retries.increment();
            Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Account[] accounts = new Account[4];
        for (int i = 0; i < accounts.length; i++) accounts[i] = new Account(i, 1_000);

        Thread[] workers = new Thread[8];
        LongAdder[] individualTransfers = new LongAdder[8];

        for (int w = 0; w < workers.length; w++) {
            final int workerIndex = w;
            individualTransfers[w] = new LongAdder();
            
            workers[w] = new Thread(() -> {
                var rnd = ThreadLocalRandom.current();
                
                while (!Thread.currentThread().isInterrupted()) {
                    Account a = accounts[rnd.nextInt(accounts.length)];
                    Account b = accounts[rnd.nextInt(accounts.length)];
                    if (a != b) {
                        try {
                            transfer(a, b, rnd.nextInt(1, 50));
                            individualTransfers[workerIndex].increment();
                        } catch (InterruptedException e) {
                            
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                System.out.printf("%s stopped after %d transfers%n", 
                        Thread.currentThread().getName(), individualTransfers[workerIndex].sum());
            }, "transfer-" + w);
            workers[w].start();
        }

        
        Thread.sleep(2000);
        System.out.println("[main] interrupting 8 workers after 2000 ms");

        
        for (Thread t : workers) t.interrupt();

        
        boolean allStopped = true;
        for (Thread t : workers) {
            t.join(1000);
            if (t.isAlive()) allStopped = false;
        }

        long total = 0; 
        for (Account a : accounts) total += a.balance();
        
        System.out.printf("all workers stopped=%b total=%d (expected 4000)%n", allStopped, total);
        System.out.println("Total tryLock retries across all threads: " + retries.sum());
    }
}