package no.hal.expressions;

import java.util.Collection;

import tech.tablesaw.api.ColumnType;

public interface PreparedExpression {
	public String getExpression();
	public ColumnType getColumnType();
	public Collection<String> getDiagnostics();
}