package edu.itc.se.lab01.task4;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    static long countPrimes(long from, long to) {
        long count = 0;
        for (long n = Math.max(from, 2); n < to; n++) {
            boolean prime = true;
            for (long d = 2; d * d <= n; d++)
                if (n % d == 0) {
                    prime = false;
                    break;
                }
            if (prime)
                count++;
        }
        return count;
    }

    public static void main(String[] args) throws Exception {
        final long LIMIT = 4_000_000, CHUNKS = 16;

        long t0 = System.nanoTime();
        long single = countPrimes(0, LIMIT);
        System.out.printf("single-threaded: %d primes in %d ms%n", single, (System.nanoTime() - t0) / 1_000_000);

        t0 = System.nanoTime();
        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            List<Future<Long>> futures = new ArrayList<>();
            long chunkSize = LIMIT / CHUNKS;

            for (int i = 0; i < CHUNKS; i++) {
                long start = i * chunkSize;
                long end = (i == CHUNKS - 1) ? LIMIT : start + chunkSize;
                futures.add(pool.submit(new PrimeCounter(start, end)));
            }
            long total = 0;
            for (Future<Long> f : futures) {
                total += f.get();
            }
            System.out.printf("pool: %d primes in %d ms%n", total, (System.nanoTime() - t0) / 1_000_000);
        }

        try (ExecutorService platformPool = Executors.newFixedThreadPool(20)) {
            runDownloads("downloads on fixed(20) platform threads", platformPool);
        }
        try (ExecutorService virtualPool = Executors.newVirtualThreadPerTaskExecutor()) {
            runDownloads("downloads on virtual threads           ", virtualPool);
        }
    }

    static void runDownloads(String label, ExecutorService executor) {
        long t0 = System.nanoTime();
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            futures.add(executor.submit(new FakeDownload("http://example.com/file" + i, i)));
        }

        int ok = 0, failed = 0, timedOut = 0;

        for (Future<String> future : futures) {
            try {
                future.get(1, TimeUnit.SECONDS);
                ok++;
            } catch (TimeoutException e) {
                timedOut++;
                future.cancel(true);
            } catch (ExecutionException e) {
                failed++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("%s : ok=%d failed=%d timedOut=%d in %d ms%n", label, ok, failed, timedOut, ms);
    }
}