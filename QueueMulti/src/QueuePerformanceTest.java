import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class QueuePerformanceTest {
    private static final String FILENAME_QUEUE = "tiemposEnqDeq.csv";
    private static final String FILENAME_QUEUE_M = "tiemposEnqDeqMULTI.csv";

    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length < 5) {
            System.out.println("Usage: java QueuePerformanceTest <queueType> <numElements> <numThreads> <numRepetitions> <simulateWork>");
            return;
        }

        String queueType = args[0];
        int numElements = Integer.parseInt(args[1]);
        int numThreads = Integer.parseInt(args[2]);
        int numRepetitions = Integer.parseInt(args[3]);
        boolean simulateWork = Boolean.parseBoolean(args[4]);

        if ("LockFreeQueue".equals(queueType)) {
            testQueuePerformance(new LockFreeQueue<Integer>(), "LockFreeQueue", numElements, numThreads, numRepetitions, simulateWork, FILENAME_QUEUE);
        } else if ("LockFreeQueueM".equals(queueType)) {
            testQueuePerformance(new LockFreeQueueM<Integer>(), "LockFreeQueueM", numElements, numThreads, numRepetitions, simulateWork, FILENAME_QUEUE_M);
        } else {
            System.out.println("Invalid queue type. Use 'LockFreeQueue' or 'LockFreeQueueM'.");
        }
    }

    private static void testQueuePerformance(Object queue, String queueName, int numElements, int numThreads, int numRepetitions, boolean simulateWork, String filename) throws InterruptedException, IOException {
        long[] durations = new long[numRepetitions];

        for (int r = 0; r < numRepetitions; r++) {
            Thread[] threads = new Thread[numThreads];
            AtomicInteger enqueueCount = new AtomicInteger(0);
            AtomicInteger dequeueCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(numThreads);

            long startTime = System.nanoTime();

            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < numElements / numThreads; j++) {
                        if (Math.random() < 0.5) {
                            if (queue instanceof LockFreeQueue) {
                                ((LockFreeQueue<Integer>) queue).enq(j);
                            } else if (queue instanceof LockFreeQueueM) {
                                ((LockFreeQueueM<Integer>) queue).enq(j);
                            }
                            enqueueCount.incrementAndGet();
                        } else {
                            if (queue instanceof LockFreeQueue) {
                                try {
                                    ((LockFreeQueue<Integer>) queue).deq();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (queue instanceof LockFreeQueueM) {
                                try {
                                    ((LockFreeQueueM<Integer>) queue).deq();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            dequeueCount.incrementAndGet();
                        }
                        if (simulateWork) {
                            try {
                                Thread.sleep(1); // Simula un pequeño trabajo (1 ms)
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                    latch.countDown();
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            latch.await();

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000; // Convert to milliseconds
            durations[r] = duration;
        }

        long totalDuration = 0;
        for (long duration : durations) {
            totalDuration += duration;
        }
        long averageDuration = totalDuration / numRepetitions;
        double stdDev = calculateStdDev(durations, averageDuration);

        System.out.println("Probando " + queueName + " con " + numThreads + " hilos");
        System.out.println("Tiempo promedio de operaciones enq & deq: " + averageDuration + " ms");
        System.out.println("Desviacion estandar: " + stdDev + " ms");

        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(numThreads + "," + averageDuration + "," + stdDev + "," + numElements + "\n");
        }
    }

    private static double calculateStdDev(long[] durations, long mean) {
        long sum = 0;
        for (long duration : durations) {
            sum += Math.pow(duration - mean, 2);
        }
        return Math.sqrt(sum / durations.length);
    }
}
