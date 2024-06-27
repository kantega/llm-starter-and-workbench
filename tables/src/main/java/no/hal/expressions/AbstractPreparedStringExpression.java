package no.hal.expressions;

import java.util.ArrayList;
import java.util.Collection;

import tech.tablesaw.api.ColumnType;

public abstract class AbstractPreparedStringExpression implements PreparedExpression {

	private String expr;
	private Collection<String> diagnostics = new ArrayList<String>();

	public AbstractPreparedStringExpression(String expr) {
		this.expr = expr;
	}

	@Override
	public String toString() {
		var buffer = new StringBuilder();
		buffer.append("[Prepared expression: ");
		buffer.append(getExpression());
		if (! diagnostics.isEmpty()) {
			buffer.append(",  ");
			buffer.append(getDiagnostics());
		}
		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public String getExpression() {
		return expr;
	}

	@Override
	public ColumnType getColumnType() {
		return null;
	}

	@Override
	public Collection<String> getDiagnostics() {
		return new ArrayList<String>(diagnostics);
	}
	
	public void clearDiagnostics() {
		diagnostics.clear();
	}

	public void addDiagnostics(String diagnostic) {
		if (diagnostic != null) {
			diagnostics.add(diagnostic);
		}
	}
}