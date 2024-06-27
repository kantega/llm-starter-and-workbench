package no.hal.wb.logging;

import java.util.List;
import java.util.logging.LogRecord;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

class LogRecordsTreeViewController extends AbstractLogRecordsController<TreeView<Object>> {

    LogRecordsTreeViewController(TreeView<Object> control) {
        super(control);
        control.setShowRoot(false);
        control.setRoot(new TreeItem<Object>());
    }

    @Override
    public void addLogRecords(List<LogRecord> newLogRecords) {
        var levelItems = logRecordsControl.getRoot().getChildren();
        for (var logRecord : newLogRecords) {
            var levelItem = levelItems.stream().filter(item -> item.getValue() == logRecord.getLevel()).findFirst().orElse(null);
            if (levelItem == null) {
                levelItem = new TreeItem<Object>(logRecord.getLevel());
                levelItems.add(levelItem);
            }
            levelItem.getChildren().add(new TreeItem<Object>(logRecord));
        }
    }
}
