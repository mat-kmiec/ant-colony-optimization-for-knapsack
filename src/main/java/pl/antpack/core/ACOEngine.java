package pl.antpack.core;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pl.antpack.model.Item;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class ACOEngine {

    private final List<Item> items;
    private final int capacity;
    private double alpha;
    private double beta;
    private double evaporationRate;
    private int antCount = 50;
    private double[] pheromones;

    private Solution globalBestSolution;
    private boolean isRunning = false;
    private Thread workerThread;
    private Consumer<SimulationMetrics> onIterationFinished;
    private Consumer<String> onLogMessage;

    private int stagnationCounter = 0;
    private final int MAX_STAGNATION = 60;
    private long startTime;

    public record SimulationMetrics(int iteration, double avgValue, int bestInIterationVal,
                                    int globalBestVal, List<Integer> bestItemIds,
                                    double[] pheromonesSnapshot, double bestWeight) {}

    public ACOEngine(List<Item> items, int capacity) {
        this.items = items;
        this.capacity = capacity;
        this.pheromones = new double[items.size()];
        reset();
    }

    public void updateParameters(double alpha, double beta, double rho) {
        this.alpha = alpha;
        this.beta = beta;
        this.evaporationRate = rho;
    }

    public void setCallbacks(Consumer<SimulationMetrics> onMetrics, Consumer<String> onLog) {
        this.onIterationFinished = onMetrics;
        this.onLogMessage = onLog;
    }

    public void reset() {
        stop();
        this.globalBestSolution = null;
        this.stagnationCounter = 0;
        Arrays.fill(pheromones, 1.0);
    }

    private void log(String msg) {
        if (onLogMessage != null) Platform.runLater(() -> onLogMessage.accept(msg));
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        startTime = System.currentTimeMillis();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                log("SYSTEM: Uruchamianie silnika ACO. Wątki równoległe aktywne.");
                int iteration = 1;
                while (isRunning) {
                    runIteration(iteration);
                    iteration++;
                    Thread.sleep(20);
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
        log("SYSTEM: Zatrzymano symulację.");
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    private void runIteration(int iteration) {
        List<Solution> solutions = IntStream.range(0, antCount)
                .parallel()
                .mapToObj(i -> new Ant(items, pheromones, alpha, beta).buildSolution(capacity))
                .toList();

        Solution iterationBest = solutions.stream()
                .max(Comparator.comparingInt(Solution::getValue))
                .orElseThrow();

        boolean improved = false;
        if (globalBestSolution == null || iterationBest.getValue() > globalBestSolution.getValue()) {
            globalBestSolution = iterationBest;
            improved = true;
            stagnationCounter = 0;
            log("SUKCES: Nowy rekord w iteracji " + iteration + ": " + globalBestSolution.getValue() + " pkt");
        } else {
            stagnationCounter++;
        }

        if (stagnationCounter >= MAX_STAGNATION) {
            log("ALARM: Wykryto stagnację (" + MAX_STAGNATION + " iteracji). Reset feromonów!");
            Arrays.fill(pheromones, 1.0);
            stagnationCounter = 0;
        } else {
            updatePheromones(solutions, iterationBest);
        }

        double avgValue = solutions.stream().mapToInt(Solution::getValue).average().orElse(0);
        List<Integer> bestItemIds = iterationBest.getItems().stream().map(Item::getId).toList();

        double currentBestWeight = iterationBest.getItems().stream().mapToDouble(Item::getWeight).sum();

        double[] pheromonesCopy = Arrays.copyOf(pheromones, pheromones.length);

        SimulationMetrics metrics = new SimulationMetrics(
                iteration, avgValue, iterationBest.getValue(),
                globalBestSolution.getValue(), bestItemIds, pheromonesCopy, currentBestWeight
        );

        if (onIterationFinished != null) {
            Platform.runLater(() -> onIterationFinished.accept(metrics));
        }
    }

    private void updatePheromones(List<Solution> solutions, Solution iterationBest) {
        for (int i = 0; i < pheromones.length; i++) {
            pheromones[i] *= (1.0 - evaporationRate);
            if (pheromones[i] < 0.05) pheromones[i] = 0.05;
            if (pheromones[i] > 100.0) pheromones[i] = 100.0;
        }

        for (Solution s : solutions) {
            double reward = (double) s.getValue() / 2000.0;
            for (Item item : s.getItems()) {
                pheromones[item.getId()] += reward;
            }
        }

        if (globalBestSolution != null) {
            double eliteReward = (double) globalBestSolution.getValue() / 1000.0;
            for (Item item : globalBestSolution.getItems()) {
                pheromones[item.getId()] += eliteReward;
            }
        }
    }

    public Solution getGlobalBest() { return globalBestSolution; }
    public int getCapacity() { return capacity; }
}