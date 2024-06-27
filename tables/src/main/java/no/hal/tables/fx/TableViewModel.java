package no.hal.tables.fx;

import java.util.Comparator;

import tech.tablesaw.api.ColumnType;

public interface TableViewModel {
    public int getColumnCount();
    public int getRowCount();

    public String getColumnName(int columnIndex);
    public ColumnType getColumnType(int columnIndex);

    public Object getDataValue(final int columnIndex, final int rowIndex);
    public void setDataValue(final int columnIndex, final int rowIndex, Object newValue);

    public Comparator<?> getColumnComparator(int columnIndex);


    public static interface Listener {
		public void tableRowsChanged(int startRow, int endRow);
		public void tableCellChanged(int row, int column, Object oldValue, Object newValue);
	}

    public void addTableChangeListener(final Listener listener);
    public void removeTableChangeListener(final Listener listener);
}
