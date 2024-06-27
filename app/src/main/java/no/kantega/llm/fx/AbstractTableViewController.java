package no.kantega.llm.fx;

import javafx.scene.control.TableView;
import no.hal.expressions.ExpressionSupport;
import no.hal.fx.bindings.BindingSource;
import no.hal.tables.fx.TableController;
import no.hal.tables.fx.TableController.TableUpdate;
import tech.tablesaw.api.Table;

public abstract class AbstractTableViewController {

    protected abstract TableView<Integer> getTableView();
    protected abstract ExpressionSupport getExpressionSupport();
    
    private TableController tableController;

    protected void setTable(Table table) {
        tableController.setTable(table);
        getTableView().getParent().requestLayout();
    }
    
    protected BindingSource<TableUpdate> createTableBindingSource() {
        return new BindingSource<TableUpdate>(getTableView(), TableUpdate.class, tableController.tableProperty());
    }

    void initialize() {
        tableController = new TableController(getTableView());
        tableController.setExpressionSupport(getExpressionSupport());
    }
}
