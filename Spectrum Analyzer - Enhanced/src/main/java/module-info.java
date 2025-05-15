module spectrum.analyzer.software.spectrumanalyzerenhanced {
    requires javafx.controls;
    requires javafx.fxml;


    opens spectrum.analyzer.software to javafx.fxml;
    exports spectrum.analyzer.software;
}