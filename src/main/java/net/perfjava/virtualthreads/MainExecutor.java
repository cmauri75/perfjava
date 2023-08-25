/* CMauri - Italy - 2023 */
package net.perfjava.virtualthreads;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;
import jdk.incubator.concurrent.StructuredTaskScope;

public class MainExecutor {
    private static final AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        if (args.length > 0) {
            switch (args[0]) {
                case "C-T" -> goOutOfMemoryThreads();
                case "C-VT" -> goOutOfMemoryVirtualThread();
                case "T-T" -> runLoadOfThreads(Integer.parseInt(args[1]));
                case "T-VT" -> runLoadOfVirtualThreads(Integer.parseInt(args[1]));
                case "HOC" -> handleOrderClassic();
                case "HOSCF" -> handleOrderStructuredConcurrencyFailing();
                case "HOSC" -> handleOrderStructuredConcurrency();
                default -> System.err.println(
                        "Please pass C-T | C-VT | T-T #threads | T-VT #Threads | HOC | HOSCF |"
                                + " HOSC");
            }

        } else System.err.println("Please pass a parameter ");
    }

    // ----------------- C-T/ C-VT PART --- Out of memory
    private static void doWork() {
        int count = counter.incrementAndGet();
        System.out.println("Started thread: " + count);
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

    // -------------------- T-T/ T-VT PART - THROUGHPUT Check
    private static int doThreadWork(int i) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(200));
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
        if (3 == 3) throw new RuntimeException("Unexcepted error updating inventory");
        System.out.println("Inventory updated");
        return 1000;
    }

    static Integer updateStocks() throws InterruptedException {
        Thread.sleep(200);
        System.out.println("Order updating");
        Thread.sleep(2000);
        System.out.println("Order updated");
        return 2200;
    }

    static Integer updateStocksProviderB() throws InterruptedException {
        Thread.sleep(200);
        System.out.println("Order updating on provider B");
        Thread.sleep(200);
        System.out.println("Order updated on provider B");
        return 400;
    }

    static void handleOrderClassic() throws ExecutionException, InterruptedException {
        try (var esvc = new ScheduledThreadPoolExecutor(8)) {
            Future<Integer> inventory = esvc.submit(() -> updateInventory());
            Future<Integer> order = esvc.submit(() -> updateStocks());

            int theOrder = order.get();
            int theInventory = inventory.get();

            System.out.println("Inventory " + theInventory + " updated for order " + theOrder);
        }
    }

    static void handleOrderStructuredConcurrencyFailing()
            throws ExecutionException, InterruptedException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Future<Integer> inventory = scope.fork(() -> updateInventory());
            Future<Integer> order = scope.fork(() -> updateStocks());

            scope.join(); // Join both forks
            scope.throwIfFailed();

            // Here, both forks have succeeded, so compose their results
            System.out.println(
                    "Inventory "
                            + inventory.resultNow()
                            + " updated for order "
                            + order.resultNow());
        }
    }

    static void handleOrderStructuredConcurrency() throws ExecutionException, InterruptedException {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Integer>()) {
            scope.fork(() -> updateStocksProviderB());
            scope.fork(() -> updateStocks());

            scope.join();

            // Here, both forks have succeeded, so compose their results
            System.out.println("Stock update result: " + scope.result());
        }
    }
}
