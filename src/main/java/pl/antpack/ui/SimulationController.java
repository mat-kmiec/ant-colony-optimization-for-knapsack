package pl.antpack.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import pl.antpack.core.ACOEngine;
import pl.antpack.core.ACOEngine.SimulationMetrics;
import pl.antpack.model.Item;
import pl.antpack.utils.BenchmarkGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * The SimulationController class manages the user interface and the underlying ACOEngine
 * used for solving knapsack problems. This class is responsible for initializing the UI,
 * handling file input, configuring sliders and charts, and managing the communication
 * and state updates between the UI and the simulation engine.
 */
public class SimulationController {


    @FXML private Label currentIterationLabel;
    @FXML private Label globalBestLabel;
    @FXML private Label knapsackFillLabel;

    @FXML private Slider alphaSlider;
    @FXML private Label alphaLabel;
    @FXML private Slider betaSlider;
    @FXML private Label betaLabel;
    @FXML private Slider rhoSlider;
    @FXML private Label rhoLabel;

    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button resetButton;
    @FXML private Button loadFileButton;

    @FXML private LineChart<Number, Number> performanceChart;
    @FXML private ScatterChart<Number, Number> itemScatterChart;

    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, Double> pheromoneCol;
    @FXML private ListView<String> logListView;

    private ACOEngine engine;
    private XYChart.Series<Number, Number> avgSeries;
    private XYChart.Series<Number, Number> bestSeries;
    private double[] currentPheromones;

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method sets up the required UI components and performs essential configuration.
     * It prepares sliders, charts, and tables for user interaction, and loads the necessary benchmark data.
     * The following setup tasks are executed:
     * - Configuring and initializing sliders via the {@code setupSliders} method.
     * - Configuring and initializing charts via the {@code setupCharts} method.
     * - Configuring and initializing tables via the {@code setupTable} method.
     * - Loading benchmark-specific data to populate the application via the {@code loadBenchmarkData} method.
     */
    @FXML
    public void initialize() {
        setupSliders();
        setupCharts();
        setupTable();

        loadBenchmarkData();
    }

    /**
     * Handles the action of loading a file containing backpack data using a file chooser dialog.
     * The method allows the user to select a file and attempts to parse and load its contents.
     * If the file is not in the expected format, an error alert is displayed to the user.
     *
     * The expected file format is:
     * - The first line contains the capacity of the backpack.
     * - Subsequent lines contain pairs of weight and value, separated by spaces.
     *
     * If the file is successfully loaded, its contents are parsed and used.
     * If an error occurs during parsing, an alert is displayed with instructions for the correct format.
     *
     * The supported file type is text files with the extension ".txt".
     *
     * This method is marked with the `@FXML` annotation, indicating its association with JavaFX.
     */
    @FXML
    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik z danymi plecaka");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"));

        File file = fileChooser.showOpenDialog(startButton.getScene().getWindow());

        if (file != null) {
            try {
                parseAndLoadFile(file);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd formatu");
                alert.setHeaderText("Nie udało się wczytać pliku");
                alert.setContentText("Upewnij się, że format to:\nCAPACITY\nWAGA WARTOŚĆ\nWAGA WARTOŚĆ...");
                alert.showAndWait();
            }
        }
    }

    /**
     * Parses the provided file to extract items and their attributes, and loads
     * the data into the simulation engine. The file's content should follow a
     * specific format where the first line specifies the capacity of the knapsack,
     * and subsequent lines represent items with their weight and value.
     *
     * This method performs the following operations:
     * - Reads the file content line by line.
     * - Extracts the knapsack capacity from the first line.
     * - Parses subsequent lines to create {@code Item} objects based on weight and value.
     * - Stops the current simulation engine if it is running.
     * - Loads the parsed items and capacity into the engine for processing.
     * - Logs the details of the loaded file, including the knapsack capacity and
     *   the number of parsed items.
     *
     * @param file the file containing knapsack capacity and item data.
     *             The file must not be null and should be formatted correctly.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    private void parseAndLoadFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        if (lines.isEmpty()) return;

        int capacity = Integer.parseInt(lines.get(0).trim());
        List<Item> newItems = new ArrayList<>();

        int idCounter = 0;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            if (parts.length >= 2) {
                int weight = Integer.parseInt(parts[0]);
                int value = Integer.parseInt(parts[1]);
                newItems.add(new Item(idCounter++, weight, value));
            }
        }

        if (engine != null) engine.stop();
        loadEngineWithData(newItems, capacity);
        log("Wczytano plik: " + file.getName() + " (Pojemność: " + capacity + ", Przedmioty: " + newItems.size() + ")");
    }

    /**
     * Generates and loads benchmark data for the simulation using a predefined
     * "hard knapsack" problem configuration.
     *
     * This method performs the following steps:
     * 1. Logs a message indicating the generation of test data for the knapsack problem.
     * 2. Calls the {@code BenchmarkGenerator.generateHardProblem} method to generate
     *    a problem instance with 150 items and a capacity factor of 30%.
     * 3. Loads the engine with the generated problem data, including the list of items
     *    and the calculated knapsack capacity, by invoking the {@code loadEngineWithData} method.
     *
     * The generated data is used to configure the engine and facilitate the simulation process.
     */
    private void loadBenchmarkData() {
        log("Generowanie danych testowych (Hard Knapsack)...");
        BenchmarkGenerator.ProblemInstance problem = BenchmarkGenerator.generateHardProblem(150, 30);
        loadEngineWithData(problem.items(), problem.capacity());
    }

    /**
     * Initializes and loads the engine with the provided data, sets up necessary callbacks,
     * and updates the associated UI elements to reflect the new engine state.
     *
     * @param items    the list of items to be processed by the engine
     * @param capacity the capacity value to configure the engine with
     */
    private void loadEngineWithData(List<Item> items, int capacity) {
        if (engine != null) engine.stop();
        engine = new ACOEngine(items, capacity);
        engine.setCallbacks(this::onSimulationUpdate, this::log);
        itemsTable.setItems(FXCollections.observableArrayList(items));
        currentPheromones = new double[items.size()];
        avgSeries.getData().clear();
        bestSeries.getData().clear();
        itemScatterChart.getData().clear();

        currentIterationLabel.setText("0");
        globalBestLabel.setText("0");
        knapsackFillLabel.setText("0%");

        startButton.setDisable(false);
        stopButton.setDisable(true);
        resetButton.setDisable(false);

        XYChart.Series<Number, Number> itemSeries = new XYChart.Series<>();
        itemSeries.setName("Przedmioty");
        for (Item item : items) {
            itemSeries.getData().add(new XYChart.Data<>(item.getWeight(), item.getValue()));
        }
        itemScatterChart.getData().add(itemSeries);
        updateEngineParams();
    }

    /**
     * Configures the sliders and their respective labels with formatted values.
     * This method updates the displayed label for each slider to reflect its current value
     * formatted according to the specified pattern.
     */
    private void setupSliders() {
        updateLabel(alphaSlider, alphaLabel, "Alpha: %.2f");
        updateLabel(betaSlider, betaLabel, "Beta: %.2f");
        updateLabel(rhoSlider, rhoLabel, "Rho: %.2f");
    }

    /**
     * Updates the text of the provided label based on the value of the given slider. The label text
     * is formatted using the specified format string. Additionally, adds a listener to update the
     * label whenever the slider's value changes. Updates engine parameters if the engine is not null.
     *
     * @param slider the slider whose value is used to update the label
     * @param label the label to be updated with the formatted slider value
     * @param format the format string used to display the slider's value
     */
    private void updateLabel(Slider slider, Label label, String format) {
        label.setText(String.format(format, slider.getValue()));
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            label.setText(String.format(format, newVal.doubleValue()));
            if (engine != null) updateEngineParams();
        });
    }

    /**
     * Initializes and configures the charts used in the application.
     * This method sets up the data series for the performance chart,
     * assigns appropriate names to the series, and adds the configured
     * series to the chart. It also disables the creation of symbols
     * on the chart for better visualization.
     */
    private void setupCharts() {
        avgSeries = new XYChart.Series<>();
        avgSeries.setName("Średnia populacji");
        bestSeries = new XYChart.Series<>();
        bestSeries.setName("Global Best");
        performanceChart.getData().addAll(avgSeries, bestSeries);
        performanceChart.setCreateSymbols(false);
    }

    /**
     * Configures the table view to display pheromone levels associated with items.
     *
     * This method sets up a column in the items table to represent pheromone levels.
     * It defines a cell value factory to extract and represent the pheromone levels
     * for each item using their unique identifiers. If the pheromone level data is
     * unavailable or the item's identifier exceeds the array bounds, a default value
     * of 0.0 is used.
     *
     * Additionally, a custom cell factory is applied to style and format the table cells
     * based on the pheromone level. Each cell displays the pheromone value up to four
     * decimal places. The background color is dynamically adjusted to represent the
     * pheromone intensity, with higher intensity levels resulting in a darker green shade.
     *
     * This setup integrates with the simulation process, allowing dynamic updates of
     * pheromone levels in the table during execution.
     */
    private void setupTable() {
        pheromoneCol.setCellValueFactory(data -> {
            if (currentPheromones != null && data.getValue().getId() < currentPheromones.length) {
                return new SimpleObjectProperty<>(currentPheromones[data.getValue().getId()]);
            }
            return new SimpleObjectProperty<>(0.0);
        });

        pheromoneCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.4f", item));
                    double intensity = Math.min(item / 5.0, 1.0);
                    int green = (int) (intensity * 255);
                    setStyle("-fx-text-fill: white; -fx-background-color: rgba(0, " + green + ", 0, 0.4);");
                }
            }
        });
    }

    /**
     * Handles the action of starting the engine process.
     * This method is triggered when the start button is pressed.
     * Ensures engine parameters are updated and UI components are
     * adjusted to reflect the ongoing process.
     *
     * Behavior:
     * - If the engine is not initialized, the method exits immediately.
     * - Updates engine parameters required for execution.
     * - Clears data series if no global best state exists in the engine.
     * - Disables the start button, reset button, and file load button
     *   to prevent conflicting actions during engine execution.
     * - Enables the stop button to allow manual interruption of the process.
     * - Initiates the engine process by calling the start method on it.
     */
    @FXML
    private void handleStart() {
        if (engine == null) return;
        updateEngineParams();

        if (engine.getGlobalBest() == null) {
            avgSeries.getData().clear();
            bestSeries.getData().clear();
        }

        startButton.setDisable(true);
        stopButton.setDisable(false);
        resetButton.setDisable(true);
        loadFileButton.setDisable(true);

        engine.start();
    }

    /**
     * Handles the stop action for the simulation.
     *
     * This method stops the currently running simulation engine, if it is not null,
     * and updates the user interface to reflect the stopped state. It enables the
     * start, reset, and load file buttons while disabling the stop button.
     * The method is designed to maintain consistent behavior and ensure proper
     * user interaction after the simulation is stopped.
     */
    @FXML
    private void handleStop() {
        if (engine != null) engine.stop();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        resetButton.setDisable(false);
        loadFileButton.setDisable(false);
    }

    /**
     * Resets the state of the simulation and updates the user interface.
     *
     * This method performs the following actions:
     * - Resets the ACO engine to its initial state, if it exists.
     * - Clears the data in the series used for displaying average and best results in the charts.
     * - Resets displayed metrics such as the current iteration, global best solution value,
     *   and knapsack fill percentage to their default values (0 or "0%").
     * - Logs a message indicating that the simulation state has been reset.
     * - Refreshes the items table in the user interface to reflect the reset state.
     *
     * This method is typically invoked to prepare the simulation for a new run or
     * to terminate the current simulation and restore a default baseline.
     */
    @FXML
    private void handleReset() {
        if (engine != null) engine.reset();
        avgSeries.getData().clear();
        bestSeries.getData().clear();
        currentIterationLabel.setText("0");
        globalBestLabel.setText("0");
        knapsackFillLabel.setText("0%");
        log("Zresetowano stan symulacji.");
        itemsTable.refresh();
    }

    private void updateEngineParams() {
        if (engine != null) {
            engine.updateParameters(
                    alphaSlider.getValue(),
                    betaSlider.getValue(),
                    rhoSlider.getValue()
            );
        }
    }

    /**
     * Handles the periodic update of simulation metrics during the Ant Colony Optimization (ACO) process.
     * Updates various UI components with the latest data from the simulation, including iteration number,
     * global best value, knapsack fill percentage, and charts for average and best values. Additionally,
     * refreshes the items table at specific intervals and updates the pheromone snapshot.
     *
     * @param metrics The {@code SimulationMetrics} object containing the statistics of the current
     *                iteration, such as iteration number, global best value, average solution value,
     *                pheromone levels, and solution weights.
     */
    private void onSimulationUpdate(SimulationMetrics metrics) {
        currentIterationLabel.setText(String.valueOf(metrics.iteration()));
        globalBestLabel.setText(String.valueOf(metrics.globalBestVal()));

        double fillPercent = (metrics.bestWeight() / engine.getCapacity()) * 100.0;
        knapsackFillLabel.setText(String.format("%.1f%% (%.0f/%d)", fillPercent, metrics.bestWeight(), engine.getCapacity()));

        avgSeries.getData().add(new XYChart.Data<>(metrics.iteration(), metrics.avgValue()));
        bestSeries.getData().add(new XYChart.Data<>(metrics.iteration(), metrics.globalBestVal()));

        if (avgSeries.getData().size() > 200) {
            avgSeries.getData().remove(0);
            bestSeries.getData().remove(0);
        }

        this.currentPheromones = metrics.pheromonesSnapshot();
        if (metrics.iteration() % 5 == 0) {
            itemsTable.refresh();
        }
    }

    /**
     * Logs a message to the log view with a prepended indicator and
     * limits the maximum number of log entries to 100.
     *
     * @param message the message to be logged. It will appear at the top
     *                of the log view and will be formatted with a "> " prefix.
     */
    private void log(String message) {
        logListView.getItems().add(0, "> " + message);
        if (logListView.getItems().size() > 100) {
            logListView.getItems().remove(100);
        }
    }
}