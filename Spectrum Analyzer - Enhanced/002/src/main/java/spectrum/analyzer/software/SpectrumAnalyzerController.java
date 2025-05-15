package spectrum.analyzer.software;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Controller for the spectrum analyzer GUI, managing user interactions and canvas updates.
 */
public class SpectrumAnalyzerController {

    @FXML private VBox canvasContainer;
    @FXML private VBox demodCanvasContainer;
    @FXML private Slider frequencySlider;
    @FXML private Slider dynamicRangeSlider;
    @FXML private Slider sweepSpeedSlider;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private ComboBox<String> analysisModeComboBox;
    @FXML private ComboBox<String> windowComboBox;
    @FXML private ComboBox<String> demodulationComboBox;
    @FXML private Label frequencyLabel;
    @FXML private Label dynamicRangeLabel;
    @FXML private Label channelPowerLabel;
    @FXML private Label acprLabel;
    @FXML private Label signalTypeLabel;
    @FXML private Label windowInfoLabel;
    @FXML private TextField channelBandwidthField;
    @FXML private TextField channelCountField;
    @FXML private ToggleButton logScaleToggle;
    @FXML private Button exportButton;
    @FXML private Button resetButton;
    @FXML private Button resetZoomButton;
    @FXML private Button addMarkerButton;
    @FXML private Button clearMarkersButton;
    @FXML private ToggleButton gridToggle;
    @FXML private Slider gridSpacingSlider;
    @FXML private TextField annotationTextField;
    @FXML private Button addAnnotationButton;
    @FXML private TableView<Marker> metricsTable;
    @FXML private TableColumn<Marker, String> typeColumn;
    @FXML private TableColumn<Marker, Double> frequencyColumn;
    @FXML private TableColumn<Marker, Double> amplitudeColumn;
    @FXML private TableColumn<Marker, String> signalTypeColumn;
    private SpectrumCanvas spectrumCanvas;
    private DemodCanvas demodCanvas;
    private SignalProcessor signalProcessor;
    private double maxFrequency = 50_000_000_000.0;

    /**
     * Initializes the controller, setting up UI components and event handlers.
     */
    @FXML
    public void initialize() {
        signalProcessor = new SignalProcessor();
        spectrumCanvas = new SpectrumCanvas(1160, 400, signalProcessor);
        demodCanvas = new DemodCanvas(1160, 150, signalProcessor);
        canvasContainer.getChildren().add(spectrumCanvas);
        demodCanvasContainer.getChildren().add(demodCanvas);

        // Frequency Slider
        frequencySlider.setMin(20);
        frequencySlider.setMax(maxFrequency);
        frequencySlider.setValue(1000);
        frequencySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            signalProcessor.setFrequencyRange(20, newVal.doubleValue());
            frequencyLabel.setText(String.format("Max Freq: %.2f Hz", newVal.doubleValue()));
            spectrumCanvas.resetZoomPan();
        });

        // Dynamic Range Slider
        dynamicRangeSlider.setMin(100);
        dynamicRangeSlider.setMax(200);
        dynamicRangeSlider.setValue(160);
        dynamicRangeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            spectrumCanvas.setDynamicRange(newVal.doubleValue());
            dynamicRangeLabel.setText(String.format("Dynamic Range: %.0f dB", newVal.doubleValue()));
        });

        // Sweep Speed Slider
        sweepSpeedSlider.setMin(0.1);
        sweepSpeedSlider.setMax(10.0);
        sweepSpeedSlider.setValue(1.0);
        sweepSpeedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            signalProcessor.setSweepSpeed(newVal.doubleValue());
        });

        // Display Mode ComboBox
        modeComboBox.getItems().addAll("Spectrum", "Persistence");
        modeComboBox.setValue("Spectrum");
        modeComboBox.setOnAction(e -> spectrumCanvas.setDisplayMode(modeComboBox.getValue()));

        // Analysis Mode ComboBox
        analysisModeComboBox.getItems().addAll("FFT", "Swept-Tuned");
        analysisModeComboBox.setValue("FFT");
        analysisModeComboBox.setOnAction(e -> signalProcessor.setAnalysisMode(analysisModeComboBox.getValue()));

        // Window Function ComboBox
        windowComboBox.getItems().addAll("Hanning", "Blackman-Harris", "Kaiser", "Flat-Top", "Gaussian");
        windowComboBox.setValue("Hanning");
        windowComboBox.setOnAction(e -> {
            signalProcessor.setWindowFunction(windowComboBox.getValue());
            updateWindowInfo();
        });

        // Demodulation ComboBox
        demodulationComboBox.getItems().addAll("None", "AM", "FM", "PM", "QAM", "PSK", "OFDM");
        demodulationComboBox.setValue("None");
        demodulationComboBox.setOnAction(e -> signalProcessor.setDemodulationType(demodulationComboBox.getValue()));

        // Channel Bandwidth Field with Validation
        channelBandwidthField.setText("1000");
        channelBandwidthField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double bandwidth = Double.parseDouble(newVal);
                if (bandwidth <= 0) {
                    channelBandwidthField.setText(oldVal);
                    showAlert("Invalid Input", "Channel bandwidth must be positive.");
                } else {
                    spectrumCanvas.setChannelBandwidth(bandwidth);
                }
            } catch (NumberFormatException e) {
                if (!newVal.isEmpty()) {
                    channelBandwidthField.setText(oldVal);
                    showAlert("Invalid Input", "Channel bandwidth must be a number.");
                }
            }
        });

        // Channel Count Field with Validation
        channelCountField.setText("1");
        channelCountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int count = Integer.parseInt(newVal);
                if (count < 1 || count > 5) {
                    channelCountField.setText(oldVal);
                    showAlert("Invalid Input", "Channel count must be between 1 and 5.");
                } else {
                    spectrumCanvas.setChannelCount(count);
                }
            } catch (NumberFormatException e) {
                if (!newVal.isEmpty()) {
                    channelCountField.setText(oldVal);
                    showAlert("Invalid Input", "Channel count must be an integer.");
                }
            }
        });

        // Log Scale Toggle
        logScaleToggle.setOnAction(e -> spectrumCanvas.setLogScale(logScaleToggle.isSelected()));

        // Export Button
        exportButton.setOnAction(e -> exportPowerReport());

        // Reset Button
        resetButton.setOnAction(e -> resetSettings());

        // Zoom/Pan Reset Button
        resetZoomButton.setOnAction(e -> resetZoomPan());

        // Marker Buttons
        addMarkerButton.setOnAction(e -> addMarker());
        clearMarkersButton.setOnAction(e -> clearMarkers());

        // Grid Toggle
        gridToggle.setOnAction(e -> spectrumCanvas.setGridVisible(gridToggle.isSelected()));

        // Grid Spacing Slider
        gridSpacingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            spectrumCanvas.setGridSpacing(newVal.intValue());
        });

        // Annotation Button
        addAnnotationButton.setOnAction(e -> addAnnotation());

        // Metrics Table
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        frequencyColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFrequency()).asObject());
        amplitudeColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmplitude()).asObject());
        signalTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSignalType()));
        metricsTable.setItems(spectrumCanvas.getMetricsData());

        // Animation Timer for Updates
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                spectrumCanvas.update();
                demodCanvas.update();
                updateMetrics();
            }
        };
        timer.start();
        updateWindowInfo();
    }

    /**
     * Updates channel power, ACPR, and signal type labels.
     */
    private void updateMetrics() {
        double channelPower = spectrumCanvas.getChannelPower();
        double acpr = spectrumCanvas.getACPR();
        String signalType = signalProcessor.getSignalType();
        channelPowerLabel.setText(String.format("Channel Power: %.2f dBm", channelPower));
        acprLabel.setText(String.format("ACPR: %.2f dB", acpr));
        signalTypeLabel.setText("Signal Type: " + signalType);
        metricsTable.refresh();
    }

    /**
     * Updates window function information label.
     */
    private void updateWindowInfo() {
        String window = windowComboBox.getValue();
        String info = switch (window) {
            case "Hanning" -> "Sidelobe: -31 dB, Resolution: Moderate";
            case "Blackman-Harris" -> "Sidelobe: -92 dB, Resolution: Low";
            case "Kaiser" -> "Sidelobe: -70 dB, Resolution: Adjustable";
            case "Flat-Top" -> "Sidelobe: -90 dB, Resolution: Low";
            case "Gaussian" -> "Sidelobe: -60 dB, Resolution: High";
            default -> "Unknown";
        };
        windowInfoLabel.setText("Window Info: " + info);
    }

    /**
     * Exports channel power and ACPR to a CSV file.
     */
    @FXML
    private void exportPowerReport() {
        try (FileWriter writer = new FileWriter("power_report.csv")) {
            writer.write("Channel,Power (dBm),ACPR (dB)\n");
            double power = spectrumCanvas.getChannelPower();
            double acpr = spectrumCanvas.getACPR();
            writer.write(String.format("Main,%.2f,%.2f\n", power, acpr));
            showAlert("Success", "Power report exported to power_report.csv");
        } catch (IOException e) {
            showAlert("Error", "Failed to export report: " + e.getMessage());
        }
    }

    /**
     * Resets all settings to default values.
     */
    @FXML
    private void resetSettings() {
        frequencySlider.setValue(1000);
        dynamicRangeSlider.setValue(160);
        sweepSpeedSlider.setValue(1.0);
        modeComboBox.setValue("Spectrum");
        analysisModeComboBox.setValue("FFT");
        windowComboBox.setValue("Hanning");
        demodulationComboBox.setValue("None");
        channelBandwidthField.setText("1000");
        channelCountField.setText("1");
        logScaleToggle.setSelected(false);
        gridToggle.setSelected(true);
        gridSpacingSlider.setValue(10);
        annotationTextField.setText("");
        signalProcessor.setFrequencyRange(20, 1000);
        signalProcessor.setSweepSpeed(1.0);
        signalProcessor.setAnalysisMode("FFT");
        signalProcessor.setWindowFunction("Hanning");
        signalProcessor.setDemodulationType("None");
        spectrumCanvas.setChannelBandwidth(1000);
        spectrumCanvas.setChannelCount(1);
        spectrumCanvas.setDynamicRange(160);
        spectrumCanvas.setLogScale(false);
        spectrumCanvas.setGridVisible(true);
        spectrumCanvas.setGridSpacing(10);
        spectrumCanvas.resetZoomPan();
        spectrumCanvas.clearMarkers();
        spectrumCanvas.clearAnnotations();
        updateWindowInfo();
        showAlert("Success", "Settings reset to default.");
    }

    /**
     * Resets zoom and pan to default view.
     */
    @FXML
    private void resetZoomPan() {
        spectrumCanvas.resetZoomPan();
        showAlert("Success", "Zoom and pan reset to default.");
    }

    /**
     * Adds a marker at the center frequency.
     */
    @FXML
    private void addMarker() {
        double centerFreq = (signalProcessor.getSpectrumData().getMinFreq() + signalProcessor.getSpectrumData().getMaxFreq()) / 2;
        spectrumCanvas.addMarker(centerFreq);
    }

    /**
     * Clears all markers from the plot.
     */
    @FXML
    private void clearMarkers() {
        spectrumCanvas.clearMarkers();
    }

    /**
     * Adds an annotation to the plot.
     */
    @FXML
    private void addAnnotation() {
        String text = annotationTextField.getText().trim();
        if (!text.isEmpty()) {
            // Place annotation at center of canvas
            double x = spectrumCanvas.getWidth() / 2;
            double y = spectrumCanvas.getHeight() / 2;
            spectrumCanvas.addAnnotation(text, x, y);
            annotationTextField.setText("");
            showAlert("Success", "Annotation added.");
        } else {
            showAlert("Error", "Please enter annotation text.");
        }
    }

    /**
     * Shows an alert dialog with the specified title and message.
     * @param title The title of the alert.
     * @param message The message to display.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}