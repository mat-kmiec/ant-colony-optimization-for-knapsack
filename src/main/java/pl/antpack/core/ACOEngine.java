package pl.antpack.core;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pl.antpack.model.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class ACOEngine {

    private final List<Item> items;
    private final int capacity;
    private double alpha;
    private double beta;
    private double evaporationRate;
    private int antCount = 20;
    private double[] pheromones;
    private Solution globalBestSolution;
    private boolean isRunning = false;
    private Thread workerThread;
    private BiConsumer<Integer, Double> onIterationFinished;

    public ACOEngine(List<Item> items, int capacity) {
        this.items = items;
        this.capacity = capacity;
        this.pheromones = new double[items.size()];
        Arrays.fill(pheromones, 1.0);
    }

    public void updateParameters(double alpha, double beta, double rho) {
        this.alpha = alpha;
        this.beta = beta;
        this.evaporationRate = rho;
    }

    public void setOnIterationFinished(BiConsumer<Integer, Double> callback) {
        this.onIterationFinished = callback;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int iteration = 0;
                while (isRunning) {
                    runIteration(iteration);
                    iteration++;
                    Thread.sleep(50);
                }
                return null;
            }
        };

        workerThread = new Thread(task);
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void stop() {
        isRunning = false;
    }

    private void runIteration(int iteration) {
        List<Ant> ants = new ArrayList<>();
        List<Solution> solutions = new ArrayList<>();

        for (int i = 0; i < antCount; i++) {
            Ant ant = new Ant(items, pheromones, alpha, beta);
            Solution sol = ant.buildSolution(capacity);
            solutions.add(sol);
        }

        Solution iterationBest = solutions.stream()
                .max((s1, s2) -> Integer.compare(s1.getValue(), s2.getValue()))
                .orElseThrow();

        if (globalBestSolution == null || iterationBest.getValue() > globalBestSolution.getValue()) {
            globalBestSolution = iterationBest;
            System.out.println("Nowe najlepsze: " + globalBestSolution.getValue());
        }

        updatePheromones(solutions);

        if (onIterationFinished != null) {
            double avgValue = solutions.stream().mapToInt(Solution::getValue).average().orElse(0);
            Platform.runLater(() -> onIterationFinished.accept(iteration, avgValue));
        }
    }

    private void updatePheromones(List<Solution> solutions) {
        for (int i = 0; i < pheromones.length; i++) {
            pheromones[i] *= (1.0 - evaporationRate);
        }
        for (Solution s : solutions) {
            double delta = (double) s.getValue() / 1000.0;
            for (Item item : s.getItems()) {
                pheromones[item.getId()] += delta;
            }
        }
    }
}