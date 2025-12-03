package pl.antpack.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class SimulationController {

    @FXML
    private Slider rhoSlider;
    @FXML
    private Label rhoLabel;

    @FXML
    private Slider alphaSlider;
    @FXML
    private Label alphaLabel;

    @FXML
    private Slider betaSlider;
    @FXML
    private Label betaLabel;

    private double rho;
    private double alpha;
    private double beta;

    @FXML
    public void initialize() {
        updateRho(rhoSlider.getValue());
        updateAlpha(alphaSlider.getValue());
        updateBeta(betaSlider.getValue());

        // Parowanie
        rhoSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateRho(newValue.doubleValue());
        });

        // Alfa
        alphaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateAlpha(newValue.doubleValue());
        });

        // Beta
        betaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateBeta(newValue.doubleValue());
        });
    }

    private void updateRho(double value) {
        this.rho = value;
        rhoLabel.setText(String.format("%.2f", value));
        System.out.println("Zmieniono parametr Parowania (ρ) na: " + rho);
    }

    private void updateAlpha(double value) {
        this.alpha = value;
        alphaLabel.setText(String.format("%.2f", value));
        System.out.println("Zmieniono Wpływ Feromonów (α) na: " + alpha);
    }

    private void updateBeta(double value) {
        this.beta = value;
        betaLabel.setText(String.format("%.2f", value));
        System.out.println("Zmieniono Wpływ Heurystyki (β) na: " + beta);
    }

    public double getRho() {
        return rho;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }
}