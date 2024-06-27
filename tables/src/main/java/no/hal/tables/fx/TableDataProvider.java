package no.hal.tables.fx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Objects;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.AbstractColumnParser;
import tech.tablesaw.columns.Column;
import tech.tablesaw.columns.numbers.IntColumnType;
import tech.tablesaw.io.ReadOptions;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.selection.Selection;

public class TableDataProvider implements TableViewModel {

	private Table table;
	private Selection selection = null;
	private Table tableView = null;

	public TableDataProvider(final Table table) {
		setTable(table);
	}

	public Table getTable() {
		return table;
	}

	public void setTable(final Table table) {
		this.table = table;
		tableView = null;
	}

	public boolean isRowFiltered() {
		return selection != null;
	}

	public void applyRowFilter(final Selection selection) {
		this.selection = selection;
		updateTableView();
	}

	private void updateTableView() {
		if (isRowFiltered()) {
			Selection viewSelection = isRowFiltered() ? selection : Selection.withRange(0, getRowCount());
			tableView = table.where(viewSelection);
		} else if (tableView != null) {
			tableView.clear();
			tableView = null;
		}
		fireProviderRowsChanged(-1, -1);
	}

	protected Table getDataTable() {
		return tableView != null ? tableView : table;
	}

	// column filter

	private List<String> columnFilter = null;

	public boolean hasColumn(String columnName) {
		return getColumnNames().contains(columnName);
	}

	public Collection<String> getColumnNames() {
		return columnFilter != null ? columnFilter : getDataTable().columnNames();
	}

	public Collection<String> getColumnNames(Iterable<Integer> indices) {
		List<String> allColumnNames = new ArrayList<String>(getColumnNames());
		List<String> columnNames = new ArrayList<String>();
		for (int index : indices) {
			columnNames.add(allColumnNames.get(index));
		}
		return columnNames;
	}

	public boolean isColumnFiltered() {
		return columnFilter != null;
	}

	public void clearColumnFilter() {
		columnFilter = null;
		fireProviderRowsChanged(-1, -1);
	}

	public void setColumnNames(Collection<String> columnNames) {
		this.columnFilter = new ArrayList<>(columnNames);
		fireProviderRowsChanged(-1, -1);
	}

	public void setColumnNames(String... columnNames) {
		this.columnFilter = Arrays.asList(columnNames);
		fireProviderRowsChanged(-1, -1);
	}

	public void addColumnNames(String... columnNames) {
		List<String> newColumnNames = new ArrayList<>(getColumnNames());
		for (int i = 0; i < columnNames.length; i++) {
			if (!newColumnNames.contains(columnNames[i])) {
				newColumnNames.add(columnNames[i]);
			}
		}
		this.columnFilter = newColumnNames;
		fireProviderRowsChanged(-1, -1);
	}

	public void removeColumnNames(String... columnNames) {
		if (isColumnFiltered()) {
			List<String> newColumnNames = new ArrayList<>(getColumnNames());
			newColumnNames.removeAll(Arrays.asList(columnNames));
			this.columnFilter = newColumnNames;
			fireProviderRowsChanged(-1, -1);
		}
	}

	public Column<?> getColumn(int columnIndex) {
		if (columnFilter != null) {
			return getDataTable().column(columnFilter.get(columnIndex));
		}
		return getDataTable().column(columnIndex);
	}

	@Override
	public int getColumnCount() {
		return getDataTable().columnCount();
	}

	//

	@Override
	public int getRowCount() {
		return getDataTable().rowCount();
	}

	ColumnType getColumnType(final Boolean mode, final int columnIndex) {
		return (table != null ? (Boolean.FALSE.equals(mode) ? IntColumnType.instance() : getColumn(columnIndex).type())
				: null);
	}

	@Override
	public String getColumnName(final int columnIndex) {
		return getColumn(columnIndex).name();
	}

	@Override
	public ColumnType getColumnType(final int columnIndex) {
		return getColumn(columnIndex).type();
	}

	@Override
	public Comparator<?> getColumnComparator(int columnIndex) {
		return getColumn(columnIndex);
	}

	private String columnHeaderDataValueFormat = "%s (%s)";

	protected String getColumnHeaderDataValue(final Column<?> column) {
		return String.format(columnHeaderDataValueFormat, column.name(), column.type());
	}

	protected int getRowHeaderDataValue(final int rowIndex) {
		return rowIndex + 1;
	}

	protected Object getColumnDataValue(final Column<?> column, final int rowIndex) {
		return column.get(rowIndex);
	}

	@Override
	public Object getDataValue(final int columnIndex, final int rowIndex) {
		if (table != null) {
			final Column<?> column = getColumn(columnIndex);
			return getColumnDataValue(column, rowIndex);
		}
		return null;
	}

	private ReadOptions options = CsvReadOptions.builderFromString("").build();

	@Override
	public void setDataValue(final int columnIndex, final int rowIndex, Object newValue) {
		if (table != null) {
			final Column<Object> column = (Column<Object>) getColumn(columnIndex);
			final Object oldValue = (column.isMissing(rowIndex) ? null : column.get(rowIndex));
			try {
				if (isMissingValue(column, newValue)) {
					column.setMissing(rowIndex);
					newValue = null;
				} else {
					column.set(rowIndex, newValue);
				}
			} catch (Exception e) {
				newValue = setDataValueUsingParser(column, rowIndex, String.valueOf(newValue));
			}
			if (! Objects.equal(oldValue, newValue)) {
				fireCellChanged(rowIndex, columnIndex, oldValue, newValue);
			}
		}
	}

	public boolean isMissingValue(Column<Object> column, Object newValue) {
		return newValue == null || newValue instanceof String && ((String) newValue).trim().length() == 0;
	}

	protected Object setDataValueUsingParser(final Column<Object> column, final int rowIndex, String s) {
		AbstractColumnParser<?> parser = column.type().customParser(options);
		if (parser.canParse(s)) {
			Object altValue = parser.parse(s);
			try {
				column.set(rowIndex, altValue);
				return altValue;
			} catch (Exception e1) {
				column.setMissing(rowIndex);
				return null;
			}
		} else {
			column.setMissing(rowIndex);
			return null;
		}
	}

	// listeners

	private Collection<Listener> listeners = null;

	@Override
	public void addTableChangeListener(final Listener listener) {
		if (listeners == null) {
			listeners = new ArrayList<TableViewModel.Listener>();
		}
		listeners.add(listener);
	}

	@Override
	public void removeTableChangeListener(final TableViewModel.Listener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	protected void fireProviderRowsChanged(final int startRow, final int endRow) {
		if (listeners != null) {
			// avoid ConcurrentModificationException
			for (final Listener listener : new ArrayList<>(listeners)) {
				listener.tableRowsChanged(startRow, endRow);
			}
		}
	}

	protected void fireCellChanged(final int row, final int column, final Object oldValue, final Object newValue) {
		if (listeners != null) {
			for (final Listener listener : listeners) {
				listener.tableCellChanged(row, column, oldValue, newValue);
			}
		}
	}
}