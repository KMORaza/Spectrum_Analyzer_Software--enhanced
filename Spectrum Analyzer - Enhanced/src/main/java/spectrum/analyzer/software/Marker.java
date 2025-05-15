package spectrum.analyzer.software;

/**
 * Represents a marker or peak on the spectrum plot.
 */
public class Marker {
    private final String type;
    private final double frequency;
    private final double amplitude;
    private final String signalType;

    /**
     * Constructs a Marker with the specified properties.
     * @param type The type (e.g., "Marker" or "Peak").
     * @param frequency The frequency in Hz.
     * @param amplitude The amplitude in dBm.
     * @param signalType The classified signal type.
     */
    public Marker(String type, double frequency, double amplitude, String signalType) {
        this.type = type;
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.signalType = signalType;
    }

    /**
     * Gets the type of the marker.
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the frequency of the marker.
     * @return The frequency in Hz.
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Gets the amplitude of the marker.
     * @return The amplitude in dBm.
     */
    public double getAmplitude() {
        return amplitude;
    }

    /**
     * Gets the signal type at the marker's frequency.
     * @return The signal type.
     */
    public String getSignalType() {
        return signalType;
    }
}