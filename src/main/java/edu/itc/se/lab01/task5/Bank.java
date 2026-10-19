package edu.itc.se.lab01.task5;

import java.util.concurrent.ThreadLocalRandom;

public class Bank {
    static final class Account {
        final int id;
        private long balance;

        Account(int id, long balance) {
            this.id = id;
            this.balance = balance;
        }

        long balance() {
            return balance;
        }

        void withdraw(long a) {
            balance -= a;
        }

        void deposit(long a) {
            balance += a;
        }
    }

    // BUG: lock order depends on the argument order → circular wait
    static void transfer(Account from, Account to, long amount) {
        synchronized (from) {
            synchronized (to) {
                if (from.balance() >= amount) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("PID: " + ProcessHandle.current().pid());
        Thread watchdog = new Thread(() -> {
            java.lang.management.ThreadMXBean bean = java.lang.management.ManagementFactory.getThreadMXBean();
            while (true) {
                long[] deadlockedIds = bean.findDeadlockedThreads();
                if (deadlockedIds != null) {
                    java.lang.management.ThreadInfo[] infos = bean.getThreadInfo(deadlockedIds);
                    System.out.print("[watchdog] DEADLOCK detected between: ");
                    System.out.println(java.util.Arrays.stream(infos)
                            .map(java.lang.management.ThreadInfo::getThreadName)
                            .collect(java.util.stream.Collectors.joining(", ")));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        watchdog.setDaemon(true);
        watchdog.start();
        Account[] accounts = new Account[4];
        for (int i = 0; i < accounts.length; i++)
            accounts[i] = new Account(i, 1_000);

        Thread[] workers = new Thread[8];
        for (int w = 0; w < workers.length; w++) {
            workers[w] = new Thread(() -> {
                var rnd = ThreadLocalRandom.current();
                for (int i = 0; i < 100_000; i++) {
                    Account a = accounts[rnd.nextInt(accounts.length)];
                    Account b = accounts[rnd.nextInt(accounts.length)];
                    if (a != b)
                        transfer(a, b, rnd.nextInt(1, 50));
                }
            }, "transfer-" + w);
            workers[w].start();
        }
        for (Thread t : workers)
            t.join();
        long total = 0;
        for (Account a : accounts)
            total += a.balance();
        System.out.println("total = " + total + " (expected 4000)"); // never printed – deadlock
    }
}