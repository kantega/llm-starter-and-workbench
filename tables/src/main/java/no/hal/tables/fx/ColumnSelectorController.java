package no.hal.tables.fx;

import javafx.scene.control.ComboBox;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

public class ColumnSelectorController {

    private final ComboBox<String> columnSelector;
    Class<? extends Column> columnClass;
    
    ColumnType columnType;
    
    public ColumnSelectorController(ComboBox<String> columnSelector, Class<? extends Column> columnClass, ColumnType columnType) {
        this.columnSelector = columnSelector;
        this.columnClass = columnClass;
        this.columnType = columnType;
    }

    public ColumnSelectorController(ComboBox<String> columnSelector, Class<? extends Column> columnClass, ColumnType columnType, Table table) {
        this(columnSelector, columnClass, columnType);
        setTable(table);
    }

    public void setTable(Table table) {
        if (table.columnNames().equals(columnSelector.getItems())) {
            return;
        }
        String columnName = columnSelector.getValue();
        var columnNames = table.columns().stream()
            .filter(col -> columnClass == null || columnClass.isInstance(col))
            .filter(col -> columnType == null || col.type() == columnType)
            .map(Column::name)
            .toList();
        columnSelector.getItems().setAll(columnNames);
        // try to preserve the selected column
        int pos = columnSelector.getItems().indexOf(columnName);
        if (pos >= 0 && pos != columnSelector.getSelectionModel().getSelectedIndex()) {
            columnSelector.setValue(columnName);
        }
    }
}
