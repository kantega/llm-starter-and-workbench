package no.kantega.llm.fx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.Dependent;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import no.hal.fx.bindings.BindingTarget;
import no.hal.fx.bindings.BindingsTarget;
import no.hal.tables.fx.ColumnSelectorController;
import no.hal.tables.fx.TableController.TableUpdate;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.CategoricalColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

@Dependent
public class CategorySummaryXYChartViewController implements BindingsTarget {

    @FXML
    XYChart xyChart;

    @FXML
    CategoryAxis categoryAxis;

    @FXML
    NumberAxis numberAxis;

    @FXML
    ComboBox<String> categoryColumnSelector;

    private ColumnSelectorController categoryColumnSelectorController;

    @FXML
    ComboBox<String> mainAxisColumnSelector;
    
    private ColumnSelectorController mainAxisColumnSelectorController;

    private Property<TableUpdate> tableProperty = new SimpleObjectProperty<TableUpdate>();

    private List<BindingTarget<?>> bindingTargets;

    @Override
    public List<BindingTarget<?>> getBindingTargets() {
        return this.bindingTargets;
    }

    @FXML
    void initialize() {
        categoryColumnSelectorController = new ColumnSelectorController(categoryColumnSelector, CategoricalColumn.class, null);
        mainAxisColumnSelectorController = new ColumnSelectorController(mainAxisColumnSelector, null, null);
        tableProperty.subscribe(this::tableUpdated);
        this.bindingTargets = List.of(
            new BindingTarget<TableUpdate>(xyChart, TableUpdate.class, tableProperty)
        );
    }

    private void tableUpdated() {
        Table table = tableProperty.getValue().table();
        categoryColumnSelectorController.setTable(table);
        mainAxisColumnSelectorController.setTable(table);
        updateChart();
    }

    @FXML
    void updateChart() {
        String categoryColumn = categoryColumnSelector.getValue();
        String mainAxisColumn = mainAxisColumnSelector.getValue();
        if (categoryColumn == null || categoryColumn.isBlank() || mainAxisColumn == null || mainAxisColumn.isBlank()) {
            return;
        }
        Table table = tableProperty.getValue().table();
        xyChart.setTitle(table.name());
        categoryAxis.setLabel(mainAxisColumn);
        numberAxis.setLabel(categoryColumn);
        
        // gives table with two key columns, xColumn and categoryColumn, and one count aggregate column
        var summary = table.summarize(categoryColumn, AggregateFunctions.count).by(mainAxisColumn, categoryColumn);
        
        // for each category value we create a series giving the count for the corresponding value on the numberAxis
        Map<String, XYChart.Series> seriesMap = new HashMap<>();
        var categories = table.column(categoryColumn).unique().asList().stream().map(Object::toString).toList();
        for (var category : categories) {
            XYChart.Series<?, ?> series = new XYChart.Series<>();
            series.setName(category);
            seriesMap.put(category, series);
        }
        Row row = summary.row(0);
        while (row.hasNext()) {
            var category = row.getString(categoryColumn);
            var mainAxisValue = row.getString(mainAxisColumn);
            var aggregateValue = row.getDouble(2);
            var data = (xyChart.getXAxis() == categoryAxis ? new XYChart.Data(mainAxisValue, aggregateValue) : new XYChart.Data(aggregateValue, mainAxisValue));
            seriesMap.get(category).getData().add(data);
            row = row.next();
        }
        xyChart.getData().setAll(seriesMap.values());
    }
}
