package no.hal.wb.logging;

import java.util.List;
import java.util.logging.LogRecord;

import javafx.collections.FXCollections;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

class LogRecordsSpinnerController extends AbstractLogRecordsController<Spinner<LogRecord>> {

    LogRecordsSpinnerController(Spinner<LogRecord> control) {
        super(control);
        var valueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<LogRecord>(FXCollections.observableArrayList());
        valueFactory.setConverter(new StringConverter<LogRecord>() {
            @Override
            public String toString(LogRecord logRecord) {
                return getFormatter().format(logRecord);
            }
            @Override
            public LogRecord fromString(String string) {
                return null;
            }
        });
        control.setValueFactory(valueFactory);
        control.setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    public void addLogRecords(List<LogRecord> newLogRecords) {
        var valueFactory = (SpinnerValueFactory.ListSpinnerValueFactory<LogRecord>) logRecordsControl.getValueFactory();
        valueFactory.getItems().addAll(0, newLogRecords.reversed());
    }
}
