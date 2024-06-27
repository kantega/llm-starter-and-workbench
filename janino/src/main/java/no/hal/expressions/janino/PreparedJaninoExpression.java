package no.hal.expressions.janino;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import no.hal.expressions.JavaLikePreparedStringExpression;
import tech.tablesaw.api.ColumnType;

public class PreparedJaninoExpression extends JavaLikePreparedStringExpression {

    private final ExpressionEvaluator ee;

	public PreparedJaninoExpression(String expr, Map<String, ColumnType> varTypes, String colVar) {
		super(expr, varTypes, colVar);

		ee = new ExpressionEvaluator();
		var paramNames = new String[getParamCount()];
		var paramTypes = new Class[getParamCount()];
		for (int i = 0; i < getParamCount(); i++) {
			paramNames[i] = getParamName(i);
			paramTypes[i] = getParamType(i);
		
		}
        ee.setParameters(paramNames, paramTypes);
        ee.setExpressionType(Object.class);
 
        try {
			ee.cook(expr);
		} catch (CompileException e) {
			addDiagnostics(e.getMessage());
		}
	}

	public Object eval(Map<String, Object> varValues) {
		Object[] args = new Object[getParamCount()];
		for (int argNum = 0; argNum < args.length; argNum++) {
			args[argNum] = varValues.get(getVarName(getParamName(argNum)));
		}
		try {
			return ee.evaluate(args);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}