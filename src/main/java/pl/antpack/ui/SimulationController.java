package pl.antpack.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import pl.antpack.core.ACOEngine;
import pl.antpack.model.Item;

import java.util.*;

public class SimulationController {

    @FXML private Slider rhoSlider;
    @FXML private Label rhoLabel;
    @FXML private Slider alphaSlider;
    @FXML private Label alphaLabel;
    @FXML private Slider betaSlider;
    @FXML private Label betaLabel;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button resetButton;

    @FXML private Label globalBestLabel;
    @FXML private Label currentIterationLabel;
    @FXML private LineChart<Number, Number> performanceChart;

    private ACOEngine acoEngine;
    private XYChart.Series<Number, Number> avgSeries;
    private XYChart.Series<Number, Number> bestSeries;
    @FXML
    private TableView<Item> itemsTable;
    private Set<Integer> highlightedItemIds = new HashSet<>();

    @FXML
    public void initialize() {
        setupSliders();
        setupChart();


        List<Item> items = generateDummyItems(50);
        itemsTable.setItems(FXCollections.observableArrayList(items));itemsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (highlightedItemIds.contains(item.getId())) {
                    setStyle("-fx-background-color: gold;");
                } else {
                    setStyle("");
                }
            }
        });
        acoEngine = new ACOEngine(items, 500);
        updateEngineParams();


        acoEngine.setOnIterationFinished(this::handleSimulationMetrics);
    }


    private void handleSimulationMetrics(ACOEngine.SimulationMetrics metrics) {
        Platform.runLater(() -> {
            updateChart(metrics);
            updateLabels(metrics);

            highlightedItemIds.clear();
            highlightedItemIds.addAll(metrics.bestItemIds());  // podświetl wszystkie
            itemsTable.refresh();
        });
    }



    private void highlightItem(int itemId) {
        for (int i = 0; i < itemsTable.getItems().size(); i++) {
            Item item = itemsTable.getItems().get(i);
            if (item.getId() == itemId) {
                itemsTable.getSelectionModel().select(i);
                break;
            }
        }
    }

    private void setupChart() {
        performanceChart.getData().clear();
        performanceChart.setAnimated(false);
        performanceChart.setCreateSymbols(false);

        avgSeries = new XYChart.Series<>();
        avgSeries.setName("Średnia populacji");

        bestSeries = new XYChart.Series<>();
        bestSeries.setName("Najlepszy w iteracji");

        performanceChart.getData().addAll(bestSeries, avgSeries);
    }

    private void updateChart(ACOEngine.SimulationMetrics metrics) {

        avgSeries.getData().add(new XYChart.Data<>(metrics.iteration(), metrics.avgValue()));

        bestSeries.getData().add(new XYChart.Data<>(metrics.iteration(), metrics.bestInIterationVal()));

        if (avgSeries.getData().size() > 100) {
            avgSeries.getData().remove(0);
            bestSeries.getData().remove(0);
        }
    }


    private void updateLabels(ACOEngine.SimulationMetrics metrics) {
        if (globalBestLabel != null) {
            globalBestLabel.setText(String.valueOf(metrics.globalBestVal()));
        }
        if (currentIterationLabel != null) {
            currentIterationLabel.setText(String.valueOf(metrics.iteration()));
        }
    }


    @FXML
    private void handleStart() {
        updateEngineParams();
        acoEngine.start();
        toggleButtons(true);
    }

    @FXML
    private void handleStop() {
        acoEngine.stop();
        toggleButtons(false);
    }

    @FXML
    private void handleReset() {
        acoEngine.reset();


        avgSeries.getData().clear();
        bestSeries.getData().clear();


        if (globalBestLabel != null) globalBestLabel.setText("0");
        if (currentIterationLabel != null) currentIterationLabel.setText("0");

        toggleButtons(false);
    }


    private void toggleButtons(boolean running) {
        startButton.setDisable(running);
        stopButton.setDisable(!running);
        if (resetButton != null) resetButton.setDisable(running);
    }



    private void updateEngineParams() {
        if (acoEngine != null) {
            acoEngine.updateParameters(
                    alphaSlider.getValue(),
                    betaSlider.getValue(),
                    rhoSlider.getValue()
            );
        }
    }

    private void setupSliders() {
        rhoSlider.valueProperty().addListener((obs, oldV, newV) -> {
            rhoLabel.setText(String.format("%.2f", newV));
            updateEngineParams();
        });
        alphaSlider.valueProperty().addListener((obs, oldV, newV) -> {
            alphaLabel.setText(String.format("%.2f", newV));
            updateEngineParams();
        });
        betaSlider.valueProperty().addListener((obs, oldV, newV) -> {
            betaLabel.setText(String.format("%.2f", newV));
            updateEngineParams();
        });
    }


    private List<Item> generateDummyItems(int count) {
        List<Item> list = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < count; i++) {
            // Waga: 1-50, Wartość: 1-100
            list.add(new Item(i, r.nextInt(50) + 1, r.nextInt(100) + 1));
        }
        return list;
    }
}