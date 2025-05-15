package spectrum.analyzer.software;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Canvas for rendering demodulated signal data.
 */
public class DemodCanvas extends Canvas {
    private final SignalProcessor signalProcessor;

    /**
     * Constructs a DemodCanvas with specified dimensions and processor.
     * @param width Canvas width.
     * @param height Canvas height.
     * @param processor The signal processor.
     */
    public DemodCanvas(double width, double height, SignalProcessor processor) {
        super(width, height);
        this.signalProcessor = processor;
    }

    /**
     * Updates and redraws the demodulated signal plot.
     */
    public void update() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

        SpectrumData data = signalProcessor.getSpectrumData();
        double[] amplitudes = data.getAmplitudes();
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1.0);
        double width = getWidth();
        double height = getHeight();
        double xScale = width / amplitudes.length;
        double yScale = height / 400.0;
        gc.beginPath();
        for (int i = 0; i < amplitudes.length; i++) {
            double x = i * xScale;
            double y = height / 2 - amplitudes[i] * yScale;
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(0.5);
        gc.strokeLine(0, height / 2, width, height / 2);
    }
}