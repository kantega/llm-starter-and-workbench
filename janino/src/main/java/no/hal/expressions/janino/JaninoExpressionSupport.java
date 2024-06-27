package no.hal.expressions.janino;

import java.util.Map;

import jakarta.enterprise.context.Dependent;
import no.hal.expressions.JavaLikeExpressionSupport;
import no.hal.expressions.PreparedExpression;
import tech.tablesaw.api.ColumnType;

@Dependent
public class JaninoExpressionSupport extends JavaLikeExpressionSupport {

	@Override
	public PreparedExpression prepareExpression(String expr, Map<String, ColumnType> varTypes, String colVar) {
		return new PreparedJaninoExpression(getPossiblyRewrittenExpr(expr), varTypes, colVar);
	}

	@Override
	public Object evaluateExpression(PreparedExpression expr, Map<String, Object> varValues) {
		return ((PreparedJaninoExpression) expr).eval(varValues);
	}
}