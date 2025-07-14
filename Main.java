package montecarlo;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // 1. Let user choose a function
        System.out.println("Choose a function to integrate:");
        System.out.println("1. sin(x)");
        System.out.println("2. x^2");
        System.out.println("3. e^(-x^2)");
        System.out.print("Your choice (1-3): ");
        int choice = scanner.nextInt();

        Function f;
        switch (choice) {
            case 1 -> f = FunctionsLibrary.sinFunction();
            case 2 -> f = FunctionsLibrary.squareFunction();
            case 3 -> f = FunctionsLibrary.expNegSquare();
            default -> {
                System.out.println("Invalid choice. Exiting.");
                return;
            }
        }

        // 2. Get interval
        System.out.print("Enter lower bound a: ");
        double a = scanner.nextDouble();

        System.out.print("Enter upper bound b: ");
        double b = scanner.nextDouble();

        // 3. Get sample size
        System.out.print("Enter number of samples: ");
        int samples = scanner.nextInt();

        int threads = 4; // or ask user if you want

        // === Run Sequential ===
        long startSeq = System.nanoTime();
        double resultSeq = Integrator.integrateSequential(f, a, b, samples);
        long endSeq = System.nanoTime();
        double timeSeq = (endSeq - startSeq) / 1e6;

        // === Run Parallel ===
        long startPar = System.nanoTime();
        double resultPar = Integrator.integrateParallel(f, a, b, samples, threads);
        long endPar = System.nanoTime();
        double timePar = (endPar - startPar) / 1e6;

        // === Output ===
        System.out.println("\n=== Results ===");
        System.out.printf("Sequential Result: %.6f (%.2f ms)%n", resultSeq, timeSeq);
        System.out.printf("Parallel Result:   %.6f (%.2f ms)%n", resultPar, timePar);
        System.out.printf("Speed-up: %.2fx%n", timeSeq / timePar);

        scanner.close();
    }
}
