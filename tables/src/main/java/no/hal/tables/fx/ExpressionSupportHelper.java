package no.hal.tables.fx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.hal.expressions.ExpressionSupport;
import no.hal.expressions.PreparedExpression;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

public abstract class ExpressionSupportHelper {

	protected final TableDataProvider dataProvider;
	protected final ExpressionSupport exprSupport;

	public ExpressionSupportHelper(final TableDataProvider dataProvider, final ExpressionSupport exprSupport) {
		super();
		this.dataProvider = dataProvider;
		this.exprSupport = exprSupport;
	}

	protected abstract String getColumnExpression(int columnIndex);
	
	/**
	 * Handle the result of evaluating an expression.
	 * Result may be either the value or an exception that occurred during evaluation.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param result
	 * @return If evaluation of row should continue
	 */
	protected abstract boolean handleCellResult(int rowIndex, int columnIndex, Object result);

	private Collection<Integer> tableIndices(Collection<Integer> indices) {
	    Collection<String> columnNames = dataProvider.getColumnNames(indices);
        Collection<Integer> tableIndices = new ArrayList<Integer>();
        final Table table = dataProvider.getTable();
        for (String columnName : columnNames) {
            tableIndices.add(table.columnIndex(columnName));
        }
        return tableIndices;
	}

	protected List<PreparedExpression> applyExpressions(Collection<Integer> columnIndices) {
	    final Table table = dataProvider.getTable();
		final Map<String,  ColumnType> varTypes = exprSupport.getVarTypes(table);
		final List<PreparedExpression> preparedExprs = new ArrayList<PreparedExpression>();
		Iterator<Integer> tableIndices = tableIndices(columnIndices).iterator();
		for (int columnIndex : columnIndices) {
			int tableColumnIndex = tableIndices.next();
			final String exprString = getColumnExpression(columnIndex);
			if (exprString != null && (! exprString.isBlank())) {
                Column<?> column = table.column(tableColumnIndex);
				final PreparedExpression expr = exprSupport.prepareExpression(exprString, varTypes, column.name());
				while (preparedExprs.size() <= tableColumnIndex) {
					preparedExprs.add(null);
				}
				preparedExprs.set(tableColumnIndex, expr);
			}
		}
		if (! preparedExprs.isEmpty()) {
			final int rowCount = table.rowCount();
			final Map<String, Object> varValues = new HashMap<String, Object>();
			outer: for (int rowNum = 0; rowNum < rowCount; rowNum++) {
				exprSupport.getVarValues(table, rowNum, varValues);
				for (int colNum = 0; colNum < preparedExprs.size(); colNum++) {
					final PreparedExpression expr = preparedExprs.get(colNum);
					if (expr != null && expr.getDiagnostics().isEmpty()) {
						Object result;
                        try {
							result = exprSupport.evaluateExpression(expr, varValues);
                            if (! handleCellResult(rowNum, colNum, result)) {
                                continue outer;
                            }
                        } catch (Exception e) {
                            if (! handleCellResult(rowNum, colNum, e)) {
                                continue outer;
                            }
                        }
					}
				}
			}
		}
		return preparedExprs;
	}
}