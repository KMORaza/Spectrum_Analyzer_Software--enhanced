package spectrum.analyzer.software;

/**
 * Represents a text annotation on the spectrum plot.
 */
public class Annotation {
    private final String text;
    private final double x;
    private final double y;

    /**
     * Constructs an Annotation with the specified text and coordinates.
     * @param text The annotation text.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public Annotation(String text, double x, double y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the annotation text.
     * @return The text.
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the x-coordinate.
     * @return The x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate.
     * @return The y-coordinate.
     */
    public double getY() {
        return y;
    }
}