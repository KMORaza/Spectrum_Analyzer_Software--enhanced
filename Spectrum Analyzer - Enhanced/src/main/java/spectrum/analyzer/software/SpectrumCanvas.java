package spectrum.analyzer.software;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Canvas for rendering the signal spectrum with zoom, pan, markers, peaks, grid, and annotations.
 */
public class SpectrumCanvas extends Canvas {
    private final SignalProcessor signalProcessor;
    private double dynamicRange = 160.0;
    private double channelBandwidth = 1000.0;
    private int channelCount = 1;
    private boolean logScale = false;
    private String displayMode = "Spectrum";
    private final double[][] persistenceBuffer;
    private int persistenceIndex = 0;
    private final double[] lastAmplitudes;
    private double zoomFactor = 1.0;
    private double offsetX = 0.0;
    private boolean gridVisible = true;
    private int gridSpacing = 10;
    private final List<Marker> markers = new ArrayList<>();
    private final List<Annotation> annotations = new ArrayList<>();
    private final ObservableList<Marker> metricsData = FXCollections.observableArrayList();

    /**
     * Constructs a SpectrumCanvas with specified dimensions and processor.
     * @param width Canvas width.
     * @param height Canvas height.
     * @param processor The signal processor.
     */
    public SpectrumCanvas(double width, double height, SignalProcessor processor) {
        super(width, height);
        this.signalProcessor = processor;
        this.persistenceBuffer = new double[400][16384];
        this.lastAmplitudes = new double[16384];
        // Zoom and Pan event handlers
        setOnScroll(event -> {
            double delta = event.getDeltaY() > 0 ? 1.1 : 0.9;
            zoomFactor *= delta;
            zoomFactor = Math.max(1.0, Math.min(zoomFactor, 100.0));
            update();
        });
        setOnMouseDragged(event -> {
            offsetX += event.getX() / getWidth() * 0.1 / zoomFactor;
            offsetX = Math.max(-0.5, Math.min(offsetX, 0.5));
            update();
        });
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                double freq = pixelToFrequency(event.getX());
                addMarker(freq);
            }
        });
    }

    /**
     * Sets the dynamic range for the plot.
     * @param range The dynamic range in dB.
     */
    public void setDynamicRange(double range) {
        this.dynamicRange = range;
        update();
    }

    /**
     * Sets the channel bandwidth.
     * @param bandwidth The channel bandwidth in Hz.
     */
    public void setChannelBandwidth(double bandwidth) {
        this.channelBandwidth = bandwidth;
        update();
    }

    /**
     * Sets the number of channels.
     * @param count The number of channels.
     */
    public void setChannelCount(int count) {
        this.channelCount = count;
        update();
    }

    /**
     * Sets the logarithmic scale.
     * @param logScale True for logarithmic scale, false for linear.
     */
    public void setLogScale(boolean logScale) {
        this.logScale = logScale;
        update();
    }

    /**
     * Sets the display mode (Spectrum or Persistence).
     * @param mode The display mode.
     */
    public void setDisplayMode(String mode) {
        this.displayMode = mode;
        update();
    }

    /**
     * Sets the grid visibility.
     * @param visible True to show grid, false to hide.
     */
    public void setGridVisible(boolean visible) {
        this.gridVisible = visible;
        update();
    }

    /**
     * Sets the grid spacing (number of lines).
     * @param spacing The number of grid lines.
     */
    public void setGridSpacing(int spacing) {
        this.gridSpacing = spacing;
        update();
    }

    /**
     * Adds an annotation at the specified coordinates.
     * @param text The annotation text.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void addAnnotation(String text, double x, double y) {
        annotations.add(new Annotation(text, x, y));
        update();
    }

    /**
     * Clears all annotations.
     */
    public void clearAnnotations() {
        annotations.clear();
        update();
    }

    /**
     * Resets zoom and pan to default.
     */
    public void resetZoomPan() {
        zoomFactor = 1.0;
        offsetX = 0.0;
        update();
    }

    /**
     * Adds a marker at the specified frequency.
     * @param frequency The frequency in Hz.
     */
    public void addMarker(double frequency) {
        SpectrumData data = signalProcessor.getSpectrumData();
        double[] amplitudes = data.getAmplitudes();
        double freqStep = (data.getMaxFreq() - data.getMinFreq()) / amplitudes.length;
        int index = (int) ((frequency - data.getMinFreq()) / freqStep);
        if (index >= 0 && index < amplitudes.length) {
            String signalType = signalProcessor.getSignalTypeAtFrequency(frequency);
            Marker marker = new Marker("Marker", frequency, amplitudes[index], signalType);
            markers.add(marker);
            metricsData.add(marker);
            update();
        }
    }

    /**
     * Clears all markers.
     */
    public void clearMarkers() {
        markers.clear();
        metricsData.removeIf(m -> m.getType().equals("Marker"));
        update();
    }

    /**
     * Gets the metrics data for the table.
     * @return Observable list of markers and peaks.
     */
    public ObservableList<Marker> getMetricsData() {
        return metricsData;
    }

    /**
     * Updates and redraws the spectrum plot.
     */
    public void update() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());
        SpectrumData data = signalProcessor.getSpectrumData();

        if (displayMode.equals("Persistence")) {
            drawPersistence(gc, data);
        } else {
            drawSpectrum(gc, data);
        }
        drawGrid(gc, data);
        drawChannels(gc, data);
        drawMarkers(gc, data);
        drawAnnotations(gc);
    }

    /**
     * Draws the spectrum plot.
     * @param gc The graphics context.
     * @param data The spectrum data.
     */
    private void drawSpectrum(GraphicsContext gc, SpectrumData data) {
        double[] amplitudes = data.getAmplitudes();
        System.arraycopy(amplitudes, 0, lastAmplitudes, 0, amplitudes.length);
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1.0);
        double width = getWidth();
        double height = getHeight();
        double xScale = width / amplitudes.length;
        double yScale = height / dynamicRange;
        gc.beginPath();
        for (int i = 0; i < amplitudes.length; i++) {
            double x = i * xScale;
            double y = height - (amplitudes[i] + dynamicRange) * yScale;
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
        detectPeaks(data);
    }

    /**
     * Draws the persistence plot.
     * @param gc The graphics context.
     * @param data The spectrum data.
     */
    private void drawPersistence(GraphicsContext gc, SpectrumData data) {
        double[] amplitudes = data.getAmplitudes();
        for (int y = 0; y < 400; y++) {
            for (int x = 0; x < 16384; x++) {
                persistenceBuffer[y][x] *= 0.95;
            }
        }
        for (int i = 0; i < amplitudes.length; i++) {
            int yIndex = (int) ((amplitudes[i] + dynamicRange) / dynamicRange * 399);
            if (yIndex >= 0 && yIndex < 400) {
                persistenceBuffer[yIndex][i] += 0.1;
            }
        }
        for (int y = 0; y < 400; y++) {
            for (int x = 0; x < 16384; x++) {
                double intensity = Math.min(persistenceBuffer[y][x], 1.0);
                gc.setFill(Color.rgb(0, (int) (255 * intensity), 0));
                gc.fillRect(x * getWidth() / 16384, y * getHeight() / 400, getWidth() / 16384, getHeight() / 400);
            }
        }
    }

    /**
     * Draws the grid and labels.
     * @param gc The graphics context.
     * @param data The spectrum data.
     */
    private void drawGrid(GraphicsContext gc, SpectrumData data) {
        if (!gridVisible) return;
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(0.5);
        double width = getWidth();
        double height = getHeight();
        double minFreq = data.getMinFreq() + offsetX * (data.getMaxFreq() - data.getMinFreq());
        double maxFreq = minFreq + (data.getMaxFreq() - data.getMinFreq()) / zoomFactor;
        for (int i = 0; i <= gridSpacing; i++) {
            double freq = minFreq + i * (maxFreq - minFreq) / gridSpacing;
            double x = i * width / gridSpacing;
            gc.strokeLine(x, 0, x, height);
            gc.setFill(Color.YELLOW);
            gc.fillText(String.format("%.2f Hz", freq), x, height - 10);
        }
        for (int i = 0; i <= gridSpacing / 2; i++) {
            double amp = -dynamicRange + i * dynamicRange / (gridSpacing / 2);
            double y = height - i * height / (gridSpacing / 2);
            gc.strokeLine(0, y, width, y);
            gc.setFill(Color.YELLOW);
            gc.fillText(String.format("%.0f dBm", amp), 10, y - 5);
        }
    }

    /**
     * Draws channel markers.
     * @param gc The graphics context.
     * @param data The spectrum data.
     */
    private void drawChannels(GraphicsContext gc, SpectrumData data) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(1.0);
        double width = getWidth();
        double freqRange = (data.getMaxFreq() - data.getMinFreq()) / zoomFactor;
        double centerFreq = data.getMinFreq() + offsetX * (data.getMaxFreq() - data.getMinFreq()) + freqRange / 2;
        for (int i = 0; i < channelCount; i++) {
            double channelCenter = centerFreq + (i - channelCount / 2.0) * channelBandwidth;
            double x1 = ((channelCenter - channelBandwidth / 2 - data.getMinFreq()) / freqRange) * width;
            double x2 = ((channelCenter + channelBandwidth / 2 - data.getMinFreq()) / freqRange) * width;
            gc.strokeLine(x1, 0, x1, getHeight());
            gc.strokeLine(x2, 0, x2, getHeight());
        }
    }

    /**
     * Draws markers on the plot.
     * @param gc The graphics context.
     * @param data The spectrum data.
     */
    private void drawMarkers(GraphicsContext gc, SpectrumData data) {
        gc.setStroke(Color.CYAN);
        gc.setFill(Color.CYAN);
        gc.setLineWidth(1.0);
        double width = getWidth();
        double freqRange = (data.getMaxFreq() - data.getMinFreq()) / zoomFactor;
        for (Marker marker : markers) {
            double x = ((marker.getFrequency() - data.getMinFreq()) / freqRange) * width;
            gc.strokeLine(x, 0, x, getHeight());
            gc.fillText(String.format("%.2f Hz, %.2f dBm", marker.getFrequency(), marker.getAmplitude()), x + 5, 20);
        }
    }

    /**
     * Draws annotations on the plot.
     * @param gc The graphics context.
     */
    private void drawAnnotations(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setLineWidth(1.0);
        for (Annotation annotation : annotations) {
            gc.fillText(annotation.getText(), annotation.getX(), annotation.getY());
        }
    }

    /**
     * Detects peaks in the spectrum data and updates metrics table.
     * @param data The spectrum data.
     */
    private void detectPeaks(SpectrumData data) {
        double[] amplitudes = data.getAmplitudes();
        double freqStep = (data.getMaxFreq() - data.getMinFreq()) / amplitudes.length;
        List<Marker> peaks = new ArrayList<>();
        for (int i = 1; i < amplitudes.length - 1; i++) {
            if (amplitudes[i] > amplitudes[i - 1] && amplitudes[i] > amplitudes[i + 1] && amplitudes[i] > -100) {
                double frequency = data.getMinFreq() + i * freqStep;
                String signalType = signalProcessor.getSignalTypeAtFrequency(frequency);
                peaks.add(new Marker("Peak", frequency, amplitudes[i], signalType));
            }
        }
        metricsData.removeIf(m -> m.getType().equals("Peak"));
        metricsData.addAll(peaks);
    }

    /**
     * Converts pixel x-coordinate to frequency.
     * @param x The x-coordinate.
     * @return The corresponding frequency.
     */
    private double pixelToFrequency(double x) {
        SpectrumData data = signalProcessor.getSpectrumData();
        double freqRange = (data.getMaxFreq() - data.getMinFreq()) / zoomFactor;
        double minFreq = data.getMinFreq() + offsetX * (data.getMaxFreq() - data.getMinFreq());
        return minFreq + (x / getWidth()) * freqRange;
    }

    /**
     * Computes the channel power.
     * @return The channel power in dBm.
     */
    public double getChannelPower() {
        SpectrumData data = signalProcessor.getSpectrumData();
        double[] amplitudes = data.getAmplitudes();
        double freqStep = (data.getMaxFreq() - data.getMinFreq()) / amplitudes.length;
        double centerFreq = (data.getMinFreq() + data.getMaxFreq()) / 2;
        int startIndex = (int) ((centerFreq - channelBandwidth / 2 - data.getMinFreq()) / freqStep);
        int endIndex = (int) ((centerFreq + channelBandwidth / 2 - data.getMinFreq()) / freqStep);
        double sum = 0;
        int count = 0;
        for (int i = Math.max(0, startIndex); i < Math.min(amplitudes.length, endIndex); i++) {
            sum += Math.pow(10, amplitudes[i] / 10);
            count++;
        }
        return count > 0 ? 10 * Math.log10(sum / count) : 0;
    }

    /**
     * Computes the Adjacent Channel Power Ratio (ACPR).
     * @return The ACPR in dB.
     */
    public double getACPR() {
        SpectrumData data = signalProcessor.getSpectrumData();
        double[] amplitudes = data.getAmplitudes();
        double freqStep = (data.getMaxFreq() - data.getMinFreq()) / amplitudes.length;
        double centerFreq = (data.getMinFreq() + data.getMaxFreq()) / 2;
        int mainStart = (int) ((centerFreq - channelBandwidth / 2 - data.getMinFreq()) / freqStep);
        int mainEnd = (int) ((centerFreq + channelBandwidth / 2 - data.getMinFreq()) / freqStep);
        int adjStart = (int) ((centerFreq + channelBandwidth / 2 - data.getMinFreq()) / freqStep);
        int adjEnd = (int) ((centerFreq + channelBandwidth * 3 / 2 - data.getMinFreq()) / freqStep);
        double mainPower = 0, adjPower = 0;
        int mainCount = 0, adjCount = 0;
        for (int i = Math.max(0, mainStart); i < Math.min(amplitudes.length, mainEnd); i++) {
            mainPower += Math.pow(10, amplitudes[i] / 10);
            mainCount++;
        }
        for (int i = Math.max(0, adjStart); i < Math.min(amplitudes.length, adjEnd); i++) {
            adjPower += Math.pow(10, amplitudes[i] / 10);
            adjCount++;
        }
        mainPower = mainCount > 0 ? 10 * Math.log10(mainPower / mainCount) : 0;
        adjPower = adjCount > 0 ? 10 * Math.log10(adjPower / adjCount) : 0;
        return mainPower - adjPower;
    }
}