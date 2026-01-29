package pl.antpack.core;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pl.antpack.model.Item;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 *
 */
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

    /**
     * This record represents a snapshot of metrics for a single iteration of the simulation process
     * in an Ant Colony Optimization (ACO) algorithm.
     *
     * It captures various statistical and tracking data to monitor the performance and progress of
     * the optimization process and to provide feedback to the calling code for further analysis or display.
     *
     * @param iteration         The current iteration number of the simulation.
     * @param avgValue          The average value of the solutions found by the ants in this iteration.
     * @param bestInIterationVal The best value among all solutions found by the ants in this iteration.
     * @param globalBestVal     The overall best value encountered in the simulation so far.
     * @param bestItemIds       The list of item IDs included in the best solution for this iteration.
     * @param pheromonesSnapshot A snapshot of the pheromone levels at the end of this iteration.
     * @param bestWeight        The total weight of items in the best solution for this iteration.
     */
    public record SimulationMetrics(int iteration, double avgValue, int bestInIterationVal,
                                    int globalBestVal, List<Integer> bestItemIds,
                                    double[] pheromonesSnapshot, double bestWeight) {}

    /**
     * Constructs an instance of the ACOEngine.
     * Initializes the Ant Colony Optimization engine with a list of items and a capacity.
     * This initialization also prepares the pheromone matrix and resets the engine state.
     *
     * @param items    the list of items available for the optimization process, where each item contains an id, weight, and value.
     * @param capacity the maximum capacity of the knapsack for which the optimization is performed.
     */
    public ACOEngine(List<Item> items, int capacity) {
        this.items = items;
        this.capacity = capacity;
        this.pheromones = new double[items.size()];
        reset();
    }

    /**
     * Updates the parameters of the Ant Colony Optimization algorithm.
     *
     * @param alpha The importance factor of pheromone trails in decision-making.
     * @param beta The importance factor of heuristic information in decision-making.
     * @param rho The evaporation rate of pheromones after each iteration.
     */
    public void updateParameters(double alpha, double beta, double rho) {
        this.alpha = alpha;
        this.beta = beta;
        this.evaporationRate = rho;
    }

    /**
     *
     */
    public void setCallbacks(Consumer<SimulationMetrics> onMetrics, Consumer<String> onLog) {
        this.onIterationFinished = onMetrics;
        this.onLogMessage = onLog;
    }

    /**
     * Resets the state of the Ant Colony Optimization engine.
     *
     * This method performs the following operations:
     * - Stops any ongoing simulation by invoking the {@code stop} method.
     * - Resets the global best solution to {@code null}.
     * - Resets the stagnation counter to 0.
     * - Reinitializes the pheromone matrix to its default values (all entries set to 1.0).
     *
     * This method is typically used to prepare the ACO engine for a fresh simulation
     * or to restart an ongoing optimization process.
     */
    public void reset() {
        stop();
        this.globalBestSolution = null;
        this.stagnationCounter = 0;
        Arrays.fill(pheromones, 1.0);
    }

    /**
     * Logs a message using the callback provided for logging. If the logging callback is set,
     * the message will be passed to it for processing in the JavaFX application thread.
     *
     * @param msg the message to be logged
     */
    private void log(String msg) {
        if (onLogMessage != null) Platform.runLater(() -> onLogMessage.accept(msg));
    }

    /**
     * Starts the Ant Colony Optimization (ACO) engine simulation.
     *
     * This method initializes and starts the worker thread for running the ACO algorithm. It performs the following actions:
     * 1. Checks if the simulation is already running, and if so, does nothing.
     * 2. Marks the simulation as running and records the start time.
     * 3. Initializes and starts a background thread using a JavaFX `Task` to execute ACO iterations.
     * 4. Logs a message indicating the start of the ACO engine with parallel threads active.
     * 5. Continuously executes ACO iterations by calling the `runIteration` method until the simulation is stopped.
     * 6. Enforces a short sleep interval between iterations to control execution pace.
     *
     * Note: This method is non-blocking and runs the optimization process in a separate thread.
     * The worker thread is set as a daemon, which means it does not block the JVM from shutting down.
     * Ensure to stop the simulation using the `stop` method before terminating the main application.
     */
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

    /**
     *
     */
    public void stop() {
        isRunning = false;
        log("SYSTEM: Zatrzymano symulację.");
    }

    /**
     * Computes the elapsed time since the start of the process.
     *
     * @return the elapsed time in milliseconds since the recorded start time.
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Executes a single iteration of the Ant Colony Optimization (ACO) algorithm.
     *
     * The method simulates the behavior of a colony of ants to build solutions,
     * evaluates the solutions to determine the iteration's best solution,
     * updates the global best solution if a better one is found, and adjusts
     * pheromone trails. If stagnation in solution quality is detected, pheromone
     * levels are reset to encourage new exploration.
     *
     * @param iteration the current iteration number of the ACO simulation
     */
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
            log("ALARM: Wykryto stagnację. Resetujemy feromony do poziomu 1.0, żeby wymusić nową eksplorację!");
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

    /**
     * Updates the pheromone matrix based on the solutions generated during the current iteration
     * and the best solutions found globally and in the current iteration.
     *
     * This method simulates the pheromone deposit and evaporation mechanism of Ant Colony Optimization:
     * - Pheromones are decreased by a factor proportional to the evaporation rate.
     * - Pheromones are increased based on the quality of the solutions provided.
     * - Limits are enforced to constrain pheromone values within a predefined range.
     *
     * @param solutions      the list of solutions generated by the ants during the current iteration.
     *                        Each solution contributes to pheromone updates based on its quality.
     * @param iterationBest  the best solution found in the current iteration. This solution has
     *                        a greater influence on the pheromone matrix than other solutions.
     */
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

    /**
     * Retrieves the current global best solution found by the Ant Colony Optimization (ACO) engine.
     *
     * The global best solution represents the most optimal solution discovered across all iterations
     * of the ACO process.
     *
     * @return the global best solution, encapsulated as a {@code Solution} object. Returns {@code null}
     * if no solution has been found or the simulation has not been started.
     */
    public Solution getGlobalBest() { return globalBestSolution; }
    /**
     * Retrieves the current capacity of the system.
     *
     * @return the maximum capacity of the knapsack for which the optimization process is performed.
     */
    public int getCapacity() { return capacity; }
}