package no.hal.wb.logging;

import java.util.List;
import java.util.logging.LogRecord;

import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

class LogRecordsListViewController extends AbstractLogRecordsController<ListView<LogRecord>> {

    LogRecordsListViewController(ListView<LogRecord> control) {
        super(control);
        control.setCellFactory(listView -> new ListCell<LogRecord>() {
            @Override
            protected void updateItem(LogRecord item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : getFormatter().format(item));
            }
        });
        control.setItems(FXCollections.observableArrayList());
    }

    @Override
    public void addLogRecords(List<LogRecord> newLogRecords) {
        logRecordsControl.getItems().addAll(newLogRecords);
    }
}
