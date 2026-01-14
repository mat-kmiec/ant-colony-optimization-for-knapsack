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

public class SimulationController {

    // --- GUI References ---
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

    @FXML
    public void initialize() {
        setupSliders();
        setupCharts();
        setupTable();

        loadBenchmarkData();
    }

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

    private void loadBenchmarkData() {
        log("Generowanie danych testowych (Hard Knapsack)...");
        BenchmarkGenerator.ProblemInstance problem = BenchmarkGenerator.generateHardProblem(150, 30);
        loadEngineWithData(problem.items(), problem.capacity());
    }

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
    // ----------------------------------------

    private void setupSliders() {
        updateLabel(alphaSlider, alphaLabel, "Alpha: %.2f");
        updateLabel(betaSlider, betaLabel, "Beta: %.2f");
        updateLabel(rhoSlider, rhoLabel, "Rho: %.2f");
    }

    private void updateLabel(Slider slider, Label label, String format) {
        label.setText(String.format(format, slider.getValue()));
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            label.setText(String.format(format, newVal.doubleValue()));
            if (engine != null) updateEngineParams();
        });
    }

    private void setupCharts() {
        avgSeries = new XYChart.Series<>();
        avgSeries.setName("Średnia populacji");
        bestSeries = new XYChart.Series<>();
        bestSeries.setName("Global Best");
        performanceChart.getData().addAll(avgSeries, bestSeries);
        performanceChart.setCreateSymbols(false);
    }

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

    @FXML
    private void handleStop() {
        if (engine != null) engine.stop();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        resetButton.setDisable(false);
        loadFileButton.setDisable(false);
    }

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

    private void log(String message) {
        logListView.getItems().add(0, "> " + message);
        if (logListView.getItems().size() > 100) {
            logListView.getItems().remove(100);
        }
    }
}