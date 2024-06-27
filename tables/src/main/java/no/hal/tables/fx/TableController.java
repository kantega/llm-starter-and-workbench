package no.hal.tables.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import no.hal.expressions.ExpressionSupport;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.selection.BitmapBackedSelection;
import tech.tablesaw.selection.Selection;

public class TableController {

    private final TableView<Integer> tableView;
    
    public TableController(TableView<Integer> tableView) {
        this.tableView = tableView;
    }

    public TableController(TableView<Integer> tableView, Table table) {
        this(tableView);
        setTable(table);
    }

    private TableDataProvider tableViewModel;
    private List<TextInputControl> columnFilters;

    private ExpressionSupport expressionSupport;
    
    public void setExpressionSupport(ExpressionSupport expressionSupport) {
        this.expressionSupport = expressionSupport;
    }

    public record TableUpdate(Table table, Object updateKey) {
        public TableUpdate(Table table) {
            this(table, System.currentTimeMillis());
        }
    }

    private Property<TableUpdate> tableProperty = new SimpleObjectProperty<TableUpdate>();

    public Property<TableUpdate> tableProperty() {
        return tableProperty;
    }

    public void setTable(Table table) {
        this.tableViewModel = new TableDataProvider(table);
        if (tableView instanceof TableView2<?>) {
            columnFilters = new ArrayList<>();
        }
        var viewColumns = new ArrayList<TableColumn<Integer, Object>>();
        var rowNumColumn = new TableColumn<Integer, Object>("#");
        rowNumColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<Object>(cellData.getValue() + 1));
        viewColumns.add(rowNumColumn);
        for (int i = 0; i < table.columnCount(); i++) {
            var column = table.column(i);
            viewColumns.add(createTableColumn(column, i));
        }
        tableView.getColumns().setAll(viewColumns);
        tableView.getItems().setAll(IntStream.range(0, table.rowCount()).mapToObj(Integer::valueOf).toList());
        tableProperty.setValue(new TableUpdate(table));
    }

    protected TableColumn<Integer, Object> createTableColumn(Column<?> column, int columnIndex) {
        TableColumn<Integer, Object> tableColumn = switch (tableView) {
           case TableView2<?> tableView2 -> {
                var tc = new TableColumn2<Integer, Object>(column.name());
                var filterText = new TextField();
                filterText.setPromptText("<filter for %s>".formatted(column.name()));
                columnFilters.add(filterText);
                filterText.setOnAction(actionEvent -> applyFilters());
                tc.setSouthNode(filterText);
                yield tc;
           }
           default -> new TableColumn<Integer, Object>(column.name());
        };
        tableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<Object>(tableViewModel.getDataValue(columnIndex, cellData.getValue())));
        return tableColumn;
    }

    private Selection filterSelection;
    private ExpressionSupportHelper expressionSupportHelper;

    private void applyFilters() {
        filterSelection = null;
        if (expressionSupport != null && expressionSupportHelper == null) {
            expressionSupportHelper = new ExpressionSupportHelper(tableViewModel, expressionSupport) {
                @Override
                protected String getColumnExpression(int columnIndex) {
                    return columnFilters.get(columnIndex).getText();
                }
                @Override
                protected boolean handleCellResult(int rowIndex, int columnIndex, Object result) {
                    if (filterSelection == null) {
                        filterSelection = new BitmapBackedSelection(dataProvider.getTable().rowCount());
                    }
                    if (! Boolean.TRUE.equals(result)) {
                        filterSelection.removeRange(rowIndex, rowIndex + 1);
                        // no need to check other filters in row
                        return false;
                    }
                    return true;
                }
            };
        }
        if (expressionSupportHelper != null) {
            var preparedExprs = expressionSupportHelper.applyExpressions(IntStream.range(0, columnFilters.size()).mapToObj(Integer::valueOf).toList());
            for (int colNum = 0; colNum < preparedExprs.size(); colNum++) {
                var preparedExpr = preparedExprs.get(colNum);
                if (preparedExpr != null) {
                    var textControl = columnFilters.get(colNum);
                    var toolTipBuffer = new StringBuilder(preparedExpr.getExpression());
                    if (! preparedExpr.getDiagnostics().isEmpty()) {
                        toolTipBuffer.append("\nProblems:\n");
                        for (String diag : preparedExpr.getDiagnostics()) {
                            toolTipBuffer.append(" - ");
                            toolTipBuffer.append(diag.toString());
                            toolTipBuffer.append("\n");
                        }
                    }
                    var toolTipString = toolTipBuffer.toString();
                    var toolTip = textControl.getTooltip();
                    if (toolTip == null || (! toolTip.getText().equals(toolTipString))) {
                        toolTip = new Tooltip();
                        toolTip.setAutoHide(false);
                        textControl.setTooltip(toolTip);
                    }
                    toolTip.setText(toolTipString);
                    if (! preparedExpr.getDiagnostics().isEmpty()) {
                        var pos = textControl.localToScreen(5 , 5);
                        System.out.println("Showing " + toolTip.getText() + " at " + pos);
                        textControl.getTooltip().show(textControl.getScene().getWindow(), pos.getX(), pos.getY());
                    }
                }
            }
        }
        var itemIndices = (filterSelection != null ? filterSelection.toArray() : IntStream.range(0, tableViewModel.getRowCount()).toArray());
        tableView.getItems().setAll(IntStream.of(itemIndices).mapToObj(Integer::valueOf).toList());
        var viewTable = tableViewModel.getTable();
        if (filterSelection != null) {
            viewTable = viewTable.where(filterSelection);
        }
        tableProperty.setValue(new TableUpdate(viewTable));
    }
}
