## Spektrumanalysator-Software (Spectrum Analyzer Software)

_Diese Software ist eine verbesserte Version des Spektrumanalysator-Software, einer Software, die ich habe zuvor entwickelt. In dieser Version habe ich mehr Steuerelemente integriert und die Benutzeroberfläche sowie das Layout der Software verbessert._

_This software is an enhanced version of Spectrum Analyzer, a software I wrote previously. In this version, I incorporated more controls and improved UI and layout of the software._

---

- `MainClass` starts the software, loads `spectrum_analyzer.fxml`, applies `style.css`, sets stage title to "Spectrum Analyzer", and displays 1200x800 scene
- `SignalProcessor` generates simulated spectrum data with 16384 bins, default frequency range 20 Hz to 1000 Hz
- `SignalProcessor` initializes `SpectrumData`, `SignalClassifier`, `Demodulator`, and uses `ExecutorService` for asynchronous data generation
- `SignalProcessor` sets frequency range via `setFrequencyRange`, updates `minFreq`, `maxFreq`, and regenerates data
- `SignalProcessor` sets analysis mode (FFT or Swept-Tuned) via `setAnalysisMode` and regenerates data
- `SignalProcessor` sets window function (Hanning, Blackman-Harris, Kaiser, Flat-Top, Gaussian) via `setWindowFunction` and regenerates data
- `SignalProcessor` sets demodulation type (None, AM, FM, PM, QAM, PSK, OFDM) via `setDemodulationType` and regenerates data
- `SignalProcessor` sets sweep speed via `setSweepSpeed`, scales amplitudes in Swept-Tuned mode, and regenerates data
- `SignalProcessor` generates simulated data with noise floor (-160 - 20 * log10(`maxFreq` / 1000)) and Gaussian peaks for Wi-Fi (150 Hz, +100 dB), Bluetooth (2440 MHz, +80 dB), LTE (1800 MHz, +90 dB), 5G NR (3600 MHz, +85 dB), Zigbee (2425 MHz, +75 dB)
- `SignalProcessor` applies window function to amplitudes, generates random phases (0 to 2π), and demodulates if enabled
- `SignalProcessor` generates window function array in parallel using `IntStream` for Hanning, Blackman-Harris, Kaiser, Flat-Top, or Gaussian
- `SignalProcessor` computes modified Bessel function (`besselI0`) for Kaiser window
- `SignalProcessor` classifies signal type via `SignalClassifier` and provides frequency-specific classification
- `SpectrumData` stores amplitudes, phases, `minFreq`, `maxFreq`, and size (16384)
- `SpectrumData` updates data via `update` method and provides getters for amplitudes, phases, frequency range, and size
- `Demodulator` demodulates amplitudes based on type: AM (`abs(amp) * cos(phase)), FM ((phase[i] - phase[i-1]) / 2π`), PM (`phase / 2π`), QAM (`sqrt(I^2 + Q^2)`), PSK (quantized phase to `π/4`), OFDM (`abs(amp) * cos(phase + random offset)`)
- `Demodulator` generates random phase offset (0 to π/8) for OFDM
- `SignalClassifier` identifies signal type based on peak frequency and amplitude: Bluetooth (2400–2480 MHz, >-100 dBm), Wi-Fi (100–200 Hz, >-80 dBm), LTE (700–2700 MHz, >-90 dBm), 5G NR (3500–3700 MHz, >-95 dBm), Zigbee (2400–2450 MHz, >-105 dBm), else Unknown
- `SignalClassifier` calculates confidence (90% if peak count < 5, else 70%) and returns signal type with percentage
- `SpectrumCanvas` renders spectrum plot with 1160x400 resolution, supports zoom (scroll), pan (drag), and marker addition (double-click)
- `SpectrumCanvas` sets dynamic range (default 160 dB), channel bandwidth (default 1000 Hz), channel count (default 1), log scale, display mode (Spectrum), grid visibility, and grid spacing
- `SpectrumCanvas` adds annotations at specified coordinates and clears them via `clearAnnotations`
- `SpectrumCanvas` resets zoom (`zoomFactor` = 1.0) and pan (`offsetX` = 0.0) via `resetZoomPan`
- `SpectrumCanvas` adds markers at specified frequency with amplitude and signal type, clears user-added markers
- `SpectrumCanvas` updates canvas by drawing spectrum or persistence plot, grid, channels, markers, and annotations
- `SpectrumCanvas` draws spectrum as yellow line plot, scaling x-axis (`width` / `amplitudes.length`) and y-axis (`height` / `dynamicRange`)
- `SpectrumCanvas` draws persistence plot as green heatmap, decaying buffer values (* 0.95) and adding amplitude contributions
- `SpectrumCanvas` draws grid with frequency and amplitude labels if `gridVisible`, using `gridSpacing` lines
- `SpectrumCanvas` draws red channel markers based on `channelBandwidth` and `channelCount` around center frequency
- `SpectrumCanvas` draws cyan markers with frequency and amplitude labels
- `SpectrumCanvas` draws white text annotations at specified coordinates
- `SpectrumCanvas` detects peaks (amplitude > neighbors and >-100 dBm) and adds them as `Marker` objects to `metricsData`
- `SpectrumCanvas` converts pixel x-coordinate to frequency based on zoom and offset
- `SpectrumCanvas` computes channel power as average power (dBm) within main channel bandwidth
- `SpectrumCanvas` computes ACPR as difference between main and adjacent channel power
- `DemodCanvas` renders demodulated signal as yellow waveform on 1160x150 canvas, scaling x-axis (`width` / `amplitudes.length`) and y-axis (`height` / 400)
- `DemodCanvas` draws dark gray center line
- `Marker` represents marker or peak with type, frequency, amplitude, and signal type
- `Annotation` represents text annotation with text and x, y coordinates
- `SpectrumAnalyzerController` initializes GUI, `SignalProcessor`, `SpectrumCanvas`, and `DemodCanvas`
- `SpectrumAnalyzerController` sets up UI listeners for frequency slider (20 Hz to 50 GHz), dynamic range slider (100–200 dB), sweep speed slider (0.1–10), display mode combo box (Spectrum), analysis mode combo box (FFT, Swept-Tuned), window combo box, demodulation combo box, channel bandwidth field, channel count field (1–5), log scale toggle, export button, reset button, zoom/pan reset button, marker buttons, grid toggle, grid spacing slider, and annotation button
- `SpectrumAnalyzerController` validates channel bandwidth (>0) and channel count (1–5) inputs
- `SpectrumAnalyzerController` updates channel power, ACPR, and signal type labels in real-time
- `SpectrumAnalyzerController` updates window info label with sidelobe and resolution details for selected window function
- `SpectrumAnalyzerController` exports channel power and ACPR to `power_report.csv`
- `SpectrumAnalyzerController` resets settings to defaults: frequency 1000 Hz, dynamic range 160 dB, sweep speed 1.0, FFT mode, Hanning window, no demodulation, channel bandwidth 1000 Hz, channel count 1, log scale off, grid on, grid spacing 10
- `SpectrumAnalyzerController` resets zoom and pan via `resetZoomPan`
- `SpectrumAnalyzerController` adds marker at center frequency
- `SpectrumAnalyzerController` clears all markers
- `SpectrumAnalyzerController` adds annotation at canvas center if text is provided
- `SpectrumAnalyzerController` displays alerts for success or error messages
- `SpectrumAnalyzerController` uses `AnimationTimer` to continuously update `SpectrumCanvas`, `DemodCanvas`, and metrics labels

---

| ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(1).png) | ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(2).png) | ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(3).png) |
|-----|-----|------|
| ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(4).png) | ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(5).png) | ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(6).png) |
| ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(7).png) | ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(8).png) | ![](https://github.com/KMORaza/Spectrum_Analyzer_Software--enhanced/blob/main/Spectrum%20Analyzer%20-%20Enhanced/src/screenshots/screen%20(9).png) |


---
