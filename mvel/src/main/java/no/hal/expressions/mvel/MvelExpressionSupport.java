package no.hal.expressions.mvel;

import java.util.Map;

import jakarta.enterprise.context.Dependent;
import no.hal.expressions.JavaLikeExpressionSupport;
import no.hal.expressions.PreparedExpression;
import tech.tablesaw.api.ColumnType;

@Dependent
public class MvelExpressionSupport extends JavaLikeExpressionSupport {
	
	@Override
	public PreparedExpression prepareExpression(String expr, Map<String, ColumnType> varTypes, String colVar) {
		var actualExpr = getPossiblyRewrittenExpr(expr);
		return new PreparedMvelExpression(actualExpr, varTypes, colVar);
	}

	@Override
	public Object evaluateExpression(PreparedExpression expr, Map<String, Object> varValues) {
		Object result = null;
		try {
			result = ((PreparedMvelExpression) expr).eval(varValues);
		} catch (Exception exc) {
			result = exc;
		}
		return result;
	}
}