package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;

public class OrderedBank {
    static final class Account {
        final int id; private long balance;
        Account(int id, long balance) { this.id = id; this.balance = balance; }
        long balance() { return balance; }
        void withdraw(long a) { balance -= a; }
        void deposit(long a)  { balance += a; }
    }

    static void transfer(Account from, Account to, long amount) {
        
        Account lock1 = from.id < to.id ? from : to;
        Account lock2 = from.id < to.id ? to : from;

        synchronized (lock1) {
            synchronized (lock2) {
                if (from.balance() >= amount) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Account[] accounts = new Account[4];
        for (int i = 0; i < accounts.length; i++) accounts[i] = new Account(i, 1_000);

        long t0 = System.currentTimeMillis();
        Thread[] workers = new Thread[8];
        LongAdder totalTransfers = new LongAdder();

        for (int w = 0; w < workers.length; w++) {
            workers[w] = new Thread(() -> {
                var rnd = ThreadLocalRandom.current();
                for (int i = 0; i < 100_000; i++) {
                    Account a = accounts[rnd.nextInt(accounts.length)];
                    Account b = accounts[rnd.nextInt(accounts.length)];
                    if (a != b) {
                        transfer(a, b, rnd.nextInt(1, 50));
                        totalTransfers.increment();
                    }
                }
            }, "transfer-" + w);
            workers[w].start();
        }

        for (Thread t : workers) t.join();
        
        long total = 0; 
        for (Account a : accounts) total += a.balance();
        System.out.printf("transfers=%d total=%d (expected 4000) in %d ms%n", 
                totalTransfers.sum(), total, (System.currentTimeMillis() - t0));
    }
}