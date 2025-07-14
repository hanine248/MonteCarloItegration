package montecarlo;

import java.util.Random;
import java.util.concurrent.*;

public class Integrator {
    public static double integrateSequential(Function f, double a, double b, int samples) {
        Random rand = new Random();
        double sum = 0;
        for (int i = 0; i < samples; i++) {
            double x = a + (b - a) * rand.nextDouble();
            sum += f.evaluate(x);
        }
        return (b - a) * sum / samples;
    }

    public static double integrateParallel(Function f, double a, double b, int samples, int numThreads)
            throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Future<Double>[] futures = new Future[numThreads];
        int samplesPerThread = samples / numThreads;

        for (int i = 0; i < numThreads; i++) {
            futures[i] = executor.submit(() -> {
                Random rand = new Random();
                double localSum = 0;
                for (int j = 0; j < samplesPerThread; j++) {
                    double x = a + (b - a) * rand.nextDouble();
                    localSum += f.evaluate(x);
                }
                return localSum;
            });
        }

        double totalSum = 0;
        for (Future<Double> future : futures) {
            totalSum += future.get();
        }

        executor.shutdown();
        return (b - a) * totalSum / samples;
    }
}

