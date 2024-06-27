package no.hal.expressions.mvel;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;

import no.hal.expressions.JavaLikePreparedStringExpression;
import tech.tablesaw.api.ColumnType;

public class PreparedMvelExpression extends JavaLikePreparedStringExpression {

	private static Object[] imports = {
		"java.util.*",
		URI.class
	};

    private Object ce;

	public PreparedMvelExpression(String expr, Map<String, ColumnType> varTypes, String colVar) {
		super(expr, varTypes, colVar);
		var parserConfiguration = new ParserConfiguration();
		for (var imp : imports) {
			switch (imp) {
				case String importString -> {
					if (importString.endsWith(".*")) {
						importString = importString.substring(0, importString.length() - 2);
					}
					parserConfiguration.addPackageImport(importString);
				}
				case Class<?> importClass -> parserConfiguration.addImport(importClass);
				default -> throw new IllegalArgumentException("Unsupported import type: " + imp);
			}
		}
		parserConfiguration.addPackageImport("java.util");
		ParserContext context = new ParserContext(parserConfiguration);
		context.setStrictTypeEnforcement(true);
		for (int i = 0; i < getParamCount(); i++) {
			context.addInput(getParamName(i), getParamType(i));
		}

        try {
			ce = MVEL.compileExpression(expr, context);
		} catch (CompileException e) {
			addDiagnostics(e.getMessage());
		}
	}

	public Object eval(Map<String, Object> varValues) {
		var paramValues = new HashMap<String, Object>();
		for (var paramName : paramNames()) {
			paramValues.put(paramName, varValues.get(getVarName(paramName)));
		}
		return MVEL.executeExpression(ce, paramValues);
	}
}