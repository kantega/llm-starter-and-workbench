package no.hal.wb.logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import jakarta.enterprise.context.Dependent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TreeView;

@Dependent
public class LogRecordsController {

    private List<LogRecord> newLogRecords = new ArrayList<>();

    public void addLogRecord(LogRecord logRecord) {
        synchronized (newLogRecords) {
            newLogRecords.add(logRecord);
        }
        logRecordsUpdated();
    }

    public void addLogRecords(Collection<LogRecord> logRecords, Formatter formatter) {
        synchronized (newLogRecords) {
            newLogRecords.addAll(logRecords);
        }
        logRecordsUpdated();
    }

    @FXML
    Control logRecordsControl;

    @FXML
    Spinner<LogRecord> logRecordsSpinner;

    @FXML
    ListView<LogRecord> logRecordsListView;

    @FXML
    TreeView<Object> logRecordsTreeView;

    public void setFormatter(Formatter formatter) {
        for (var logRecordsController : logRecordsControllers) {
            logRecordsController.setFormatter(formatter);
        }
    }

    private List<AbstractLogRecordsController<?>> logRecordsControllers = new ArrayList<>();

    @FXML
    void initialize() {
        if (logRecordsSpinner != null) {
            logRecordsControllers.add(new LogRecordsSpinnerController(logRecordsSpinner));
        }
        if (logRecordsListView != null) {
            logRecordsControllers.add(new LogRecordsListViewController(logRecordsListView));
        }
        if (logRecordsTreeView != null) {
            logRecordsControllers.add(new LogRecordsTreeViewController(logRecordsTreeView));
        }
        if (logRecordsControl != null) {
            switch (logRecordsControl) {
                case Spinner<?> spinner -> logRecordsControllers.add(new LogRecordsSpinnerController((Spinner<LogRecord>) logRecordsControl));
                case ListView<?> listView -> logRecordsControllers.add(new LogRecordsListViewController((ListView<LogRecord>) logRecordsControl));
                case TreeView<?> treeView -> logRecordsControllers.add(new LogRecordsTreeViewController((TreeView<Object>) logRecordsControl));
                case null, default -> {}
            }
        }
    }

    private void logRecordsUpdated() {
        if (Platform.isFxApplicationThread()) {
            updateLogRecordsControls();
        } else {
            Platform.runLater(this::updateLogRecordsControls);
        }
    }

    private void updateLogRecordsControls() {
        synchronized (newLogRecords) {
            for (var logRecordsController : logRecordsControllers) {
                logRecordsController.addLogRecords(newLogRecords);
            }
            newLogRecords.clear();
        }
    }
}
