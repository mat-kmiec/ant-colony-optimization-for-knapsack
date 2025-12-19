package pl.antpack.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import pl.antpack.core.ACOEngine;
import pl.antpack.model.Item;

import java.util.*;

public class SimulationController {

    @FXML private Slider rhoSlider, alphaSlider, betaSlider;
    @FXML private Label rhoLabel, alphaLabel, betaLabel;
    @FXML private Button startButton, stopButton, resetButton;

    @FXML private Label globalBestLabel, currentIterationLabel, knapsackFillLabel;

    @FXML private LineChart<Number, Number> performanceChart;
    @FXML private ScatterChart<Number, Number> itemScatterChart;
    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, Double> pheromoneCol;
    @FXML private ListView<String> logListView;

    private ACOEngine acoEngine;
    private XYChart.Series<Number, Number> avgSeries;
    private XYChart.Series<Number, Number> bestSeries;
    private XYChart.Series<Number, Number> scatterDataSeries;

    private Set<Integer> highlightedItemIds = new HashSet<>();
    private Map<Integer, Double> currentPheromones = new HashMap<>();

    @FXML
    public void initialize() {
        setupSliders();
        setupCharts();
        setupLogView();
        List<Item> items = generateHardItems(250);

        itemsTable.setItems(FXCollections.observableArrayList(items));
        setupTableColumns();

        acoEngine = new ACOEngine(items, 5000);
        acoEngine.setCallbacks(this::handleSimulationMetrics, this::addLog);

        updateEngineParams();
        addLog("INFO: Dashboard gotowy. Załadowano 250 przedmiotów.");
    }


    private void handleSimulationMetrics(ACOEngine.SimulationMetrics metrics) {
        updateChart(metrics);

        globalBestLabel.setText(String.valueOf(metrics.globalBestVal()));
        currentIterationLabel.setText(String.valueOf(metrics.iteration()));

        double fillPercent = (metrics.bestWeight() / 5000.0) * 100.0;
        knapsackFillLabel.setText(String.format("%.1f%%", fillPercent));

        for(int i=0; i<metrics.pheromonesSnapshot().length; i++) {
            currentPheromones.put(i, metrics.pheromonesSnapshot()[i]);
        }

        highlightedItemIds.clear();
        highlightedItemIds.addAll(metrics.bestItemIds());

        if (metrics.iteration() % 5 == 0) {
            itemsTable.refresh();
            updateScatterVisuals();
        }
    }

    private void updateScatterVisuals() {
        if(scatterDataSeries == null) return;
        for (XYChart.Data<Number, Number> data : scatterDataSeries.getData()) {
            Integer itemId = (Integer) data.getExtraValue();
            javafx.scene.Node node = data.getNode();
            if (node != null) {
                if (highlightedItemIds.contains(itemId)) {

                    node.setStyle("-fx-background-color: #ff3333; -fx-background-radius: 5px; -fx-padding: 6px;");
                    node.toFront();
                } else {

                    node.setStyle("-fx-background-color: #007acc; -fx-background-radius: 2px; -fx-padding: 3px; -fx-opacity: 0.6;");
                }
            }
        }
    }

    private void addLog(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logListView.getItems().add(0, "[" + timestamp + "] " + message);
        if (logListView.getItems().size() > 100) logListView.getItems().remove(100);
    }

    @FXML
    private void handleStop() {
        acoEngine.stop();
        toggleButtons(false);
        showSummaryReport();
    }

    private void showSummaryReport() {
        if (acoEngine.getGlobalBest() == null) return;

        long timeSec = acoEngine.getElapsedTime() / 1000;
        int value = acoEngine.getGlobalBest().getValue();
        int itemsCount = acoEngine.getGlobalBest().getItems().size();

        int totalWeight = acoEngine.getGlobalBest().getItems().stream().mapToInt(Item::getWeight).sum();
        double fill = (totalWeight / (double) acoEngine.getCapacity()) * 100.0;

        String report = String.format("""
            === RAPORT KOŃCOWY ===
            Czas obliczeń: %d sek
            Najlepsza wartość: %d pkt
            Ilość przedmiotów w plecaku: %d
            Waga całkowita: %d / %d
            Wypełnienie plecaka: %.2f%%
            """, timeSec, value, itemsCount, totalWeight, acoEngine.getCapacity(), fill);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Symulacja Zakończona");
        alert.setHeaderText("Wyniki Optymalizacji");
        alert.setContentText(report);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        alert.showAndWait();
    }


    private void setupCharts() {
        performanceChart.getData().clear();
        performanceChart.setAnimated(false);
        performanceChart.setCreateSymbols(false);
        avgSeries = new XYChart.Series<>(); avgSeries.setName("Średnia");
        bestSeries = new XYChart.Series<>(); bestSeries.setName("Best Iteration");
        performanceChart.getData().addAll(bestSeries, avgSeries);
        itemScatterChart.setAnimated(false);
        itemScatterChart.setLegendVisible(false);
        scatterDataSeries = new XYChart.Series<>();
        itemScatterChart.getData().add(scatterDataSeries);
        itemScatterChart.getXAxis().setLabel("Waga (Koszt)");
        itemScatterChart.getYAxis().setLabel("Wartość (Zysk)");
    }

    private List<Item> generateHardItems(int count) {
        List<Item> list = new ArrayList<>();
        Random r = new Random();
        if(scatterDataSeries != null) scatterDataSeries.getData().clear();

        for (int i = 0; i < count; i++) {
            int weight = r.nextInt(100) + 5;
            int value = (int) (weight * (0.8 + r.nextDouble() * 1.5)) + r.nextInt(20);

            Item item = new Item(i, weight, value);
            list.add(item);

            XYChart.Data<Number, Number> data = new XYChart.Data<>(weight, value);
            data.setExtraValue(i);
            scatterDataSeries.getData().add(data);
        }
        return list;
    }

    private void setupTableColumns() {
        pheromoneCol.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(currentPheromones.getOrDefault(cell.getValue().getId(), 0.0)));

        pheromoneCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                    double hue = Math.max(0, 120 - (item * 10));
                    setTextFill(Color.hsb(hue, 0.9, 0.8));
                }
            }
        });

        itemsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && highlightedItemIds.contains(item.getId())) {
                    setStyle("-fx-background-color: #388e3c; -fx-text-fill: white;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupLogView() {

        logListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.contains("SUKCES")) setTextFill(Color.web("#00ff41"));
                    else if (item.contains("ALARM")) setTextFill(Color.web("#ff3333"));
                    else setTextFill(Color.web("#e0e0e0"));
                }
            }
        });
    }


    @FXML private void handleStart() { updateEngineParams(); acoEngine.start(); toggleButtons(true); addLog("SYSTEM: Start symulacji."); }
    @FXML private void handleReset() {
        acoEngine.reset();
        avgSeries.getData().clear(); bestSeries.getData().clear();
        if(globalBestLabel!=null) globalBestLabel.setText("0");
        highlightedItemIds.clear(); currentPheromones.clear();
        itemsTable.refresh(); updateScatterVisuals();
        toggleButtons(false);
        addLog("SYSTEM: Reset danych.");
    }
    private void toggleButtons(boolean running) { startButton.setDisable(running); stopButton.setDisable(!running); resetButton.setDisable(running); }
    private void updateEngineParams() { if(acoEngine!=null) acoEngine.updateParameters(alphaSlider.getValue(), betaSlider.getValue(), rhoSlider.getValue()); }
    private void setupSliders() {
        rhoSlider.valueProperty().addListener((o, old, val) -> { rhoLabel.setText(String.format("%.2f", val)); updateEngineParams(); });
        alphaSlider.valueProperty().addListener((o, old, val) -> { alphaLabel.setText(String.format("%.2f", val)); updateEngineParams(); });
        betaSlider.valueProperty().addListener((o, old, val) -> { betaLabel.setText(String.format("%.2f", val)); updateEngineParams(); });
    }
    private void updateChart(ACOEngine.SimulationMetrics metrics) {
        avgSeries.getData().add(new XYChart.Data<>(metrics.iteration(), metrics.avgValue()));
        bestSeries.getData().add(new XYChart.Data<>(metrics.iteration(), metrics.bestInIterationVal()));
        if (avgSeries.getData().size() > 200) { avgSeries.getData().remove(0); bestSeries.getData().remove(0); }
    }
}