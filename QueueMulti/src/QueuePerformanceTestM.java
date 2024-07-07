import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class QueuePerformanceTestM {
    private static final String FILENAME_QUEUE = "tiemposDeq.csv";
    private static final String FILENAME_QUEUE_M = "tiemposDeqMULTI.csv";

    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length < 6) {
            System.out.println("Usage: java QueuePerformanceTest <queueType> <numEnqueues> <numDequeues> <numThreads> <numRepetitions> <simulateWork>");
            return;
        }

        String queueType = args[0];
        int numEnqueues = Integer.parseInt(args[1]);
        int numDequeues = Integer.parseInt(args[2]);
        int numThreads = Integer.parseInt(args[3]);
        int numRepetitions = Integer.parseInt(args[4]);
        boolean simulateWork = Boolean.parseBoolean(args[5]);

        if ("LockFreeQueue".equals(queueType)) {
            testQueuePerformance(new LockFreeQueue<Integer>(), "LockFreeQueue", numEnqueues, numDequeues, numThreads, numRepetitions, simulateWork, FILENAME_QUEUE);
        } else if ("LockFreeQueueM".equals(queueType)) {
            testQueuePerformance(new LockFreeQueueM<Integer>(), "LockFreeQueueM", numEnqueues, numDequeues, numThreads, numRepetitions, simulateWork, FILENAME_QUEUE_M);
        } else {
            System.out.println("Invalid queue type. Use 'LockFreeQueue' or 'LockFreeQueueM'.");
        }
    }

    private static void testQueuePerformance(Object queue, String queueName, int numEnqueues, int numDequeues, int numThreads, int numRepetitions, boolean simulateWork, String filename) throws InterruptedException, IOException {
        long[] durations = new long[numRepetitions];

        for (int r = 0; r < numRepetitions; r++) {
            // Realizar todas las operaciones enqueue
            for (int i = 0; i < numEnqueues; i++) {
                if (queue instanceof LockFreeQueue) {
                    ((LockFreeQueue<Integer>) queue).enq(i);
                } else if (queue instanceof LockFreeQueueM) {
                    ((LockFreeQueueM<Integer>) queue).enq(i);
                }
            }

            Thread[] threads = new Thread[numThreads];
            AtomicInteger dequeueCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(numThreads);

            long startTime = System.nanoTime();

            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < numDequeues / numThreads; j++) {
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
        System.out.println("Tiempo promedio del dequeue: " + averageDuration + " ms");
        System.out.println("Desviacion estandar: " + stdDev + " ms");

        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(numThreads + "," + averageDuration + "," + stdDev + "," + numEnqueues + "," + numDequeues + "," + numRepetitions + "\n");
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
