package pl.antpack.ui;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import pl.antpack.core.ACOEngine;
import pl.antpack.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationController {

    @FXML
    private Slider rhoSlider;
    @FXML private Label rhoLabel;
    @FXML private Slider alphaSlider;
    @FXML private Label alphaLabel;
    @FXML private Slider betaSlider;
    @FXML private Label betaLabel;

    @FXML private LineChart<Number, Number> performanceChart;
    @FXML private Button startButton;
    @FXML private Button stopButton;

    private ACOEngine acoEngine;
    private XYChart.Series<Number, Number> avgSeries;

    @FXML
    public void initialize() {
        setupSliders();

        avgSeries = new XYChart.Series<>();
        avgSeries.setName("Średnia wartość populacji");
        performanceChart.getData().add(avgSeries);
        performanceChart.setAnimated(false);

        List<Item> items = generateDummyItems(50);

        acoEngine = new ACOEngine(items, 500);
        updateEngineParams();

        acoEngine.setOnIterationFinished((iteration, avgValue) -> {
            avgSeries.getData().add(new XYChart.Data<>(iteration, avgValue));

            if (avgSeries.getData().size() > 100) {
                avgSeries.getData().remove(0);
            }
        });
    }

    @FXML
    private void handleStart() {
        updateEngineParams();
        acoEngine.start();
        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    @FXML
    private void handleStop() {
        acoEngine.stop();
        startButton.setDisable(false);
        stopButton.setDisable(true);
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
    }

    private List<Item> generateDummyItems(int count) {
        List<Item> list = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < count; i++) {
            list.add(new Item(i, r.nextInt(50) + 1, r.nextInt(100) + 1));
        }
        return list;
    }
}