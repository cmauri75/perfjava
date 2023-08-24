/* Decathlon Italy - Tacos Team(C) 2023 */
package net.decathlon;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;
import jdk.incubator.concurrent.StructuredTaskScope;

public class VirtualThreads {
    private static final AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        if (args.length > 0) {
            switch (args[0]) {
                case "C-T" -> goOutOfMemoryThreads();
                case "C-VT" -> goOutOfMemoryVirtualThread();
                case "T-T" -> runLoadOfThreads(Integer.parseInt(args[1]));
                case "T-VT" -> runLoadOfVirtualThreads(Integer.parseInt(args[1]));
                case "HOC" -> handleOrderClassic();
                case "HOSC" -> handleOrderStructuredConcurrency();
                default -> System.err.println(
                        "Please pass C-T | C-VT | T-T #threads | T-VT #Threads | HOC | HOSC");
            }

        } else System.err.println("Please pass a parameter ");
    }

    // ----------------- OOM PART
    private static void doWork() {
        int count = counter.incrementAndGet();
        System.out.println("Thread count = " + count);
        LockSupport.park();
    }

    private static void goOutOfMemoryThreads() {
        while (true) {
            new Thread(() -> doWork()).start();
        }
    }

    private static void goOutOfMemoryVirtualThread() {
        while (true) {
            Thread.startVirtualThread(() -> doWork());
        }
    }

    // -------------------- THROUGHPUT PART
    private static int doThreadWork(int i) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(200));
        System.out.println(i);
        return i;
    }

    private static void runLoadOfThreads(int numOfThreads) {
        try (var executor = Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory())) {
            IntStream.range(0, numOfThreads)
                    .forEach(
                            i ->
                                    executor.submit(
                                            () -> {
                                                return doThreadWork(i);
                                            }));
        }
    }

    private static void runLoadOfVirtualThreads(int numOfThreads) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, numOfThreads)
                    .forEach(
                            i ->
                                    executor.submit(
                                            () -> {
                                                return doThreadWork(i);
                                            }));
        }
    }

    // ---------------- STRUCTURED CURRENCY PART

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    static Integer updateInventory() throws InterruptedException {
        System.out.println("Inventory updating");
        Thread.sleep(1000);
        if (3 == 3) throw new RuntimeException("Unexcepted error");
        System.out.println("Inventory updated");
        return 1000;
    }

    static Integer updateOrder() throws InterruptedException {
        Thread.sleep(200);
        System.out.println("Order updating");
        Thread.sleep(2000);
        System.out.println("Order updated");
        return 200;
    }

    static void handleOrderClassic() throws ExecutionException, InterruptedException {
        try (var esvc = new ScheduledThreadPoolExecutor(8)) {
            Future<Integer> inventory = esvc.submit(() -> updateInventory());
            Future<Integer> order = esvc.submit(() -> updateOrder());

            int theOrder = order.get(); // Join updateOrder
            int theInventory = inventory.get(); // Join updateInventory

            System.out.println("Inventory " + theInventory + " updated for order " + theOrder);
        }
    }

    static void handleOrderStructuredConcurrency() throws ExecutionException, InterruptedException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Future<Integer> inventory = scope.fork(() -> updateInventory());
            Future<Integer> order = scope.fork(() -> updateOrder());

            scope.join(); // Join both forks
            scope.throwIfFailed(); // ... and propagate errors

            // Here, both forks have succeeded, so compose their results
            System.out.println(
                    "Inventory "
                            + inventory.resultNow()
                            + " updated for order "
                            + order.resultNow());
        }
    }
}
