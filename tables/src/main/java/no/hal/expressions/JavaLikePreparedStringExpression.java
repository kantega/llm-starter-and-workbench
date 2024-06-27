package no.hal.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.tablesaw.api.ColumnType;

public abstract class JavaLikePreparedStringExpression extends AbstractPreparedStringExpression {

	private List<String> paramNames;
	private List<Class<?>> paramTypes;
    private Map<String, String> varNameMap;

	public JavaLikePreparedStringExpression(String expr, Map<String, ColumnType> varTypes, String colVar) {
		super(expr);
		prepareVariables(expr, varTypes, colVar);
	}

	public int getParamCount() {
		return paramNames.size();
	}

	public String getParamName(int index) {
		return paramNames.get(index);
	}

	public Class<?> getParamType(int index) {
		return paramTypes.get(index);
	}

	public Iterable<String> paramNames() {
		return paramNames;
	}

	public String getVarName(String name) {
		return varNameMap.getOrDefault(name, name);
	}

	protected void prepareVariables(String expr, Map<String, ColumnType> varTypes, String colVar) {
		paramNames = new ArrayList<>();
		paramTypes = new ArrayList<>();
		varNameMap = new HashMap<>();
		for (String name : varTypes.keySet()) {
			Class<?> paramClass = ExpressionSupport.getClassForColumnType(varTypes.get(name));
			if (paramClass == null) {
				addDiagnostics(name + " has type " + varTypes.get(name) + ", which is not supported");
				paramClass = Object.class;
			}
			if (name.equals(colVar)) {
				varNameMap.put(name, "it");
				varNameMap.put("it", name);
				name = "it";
			} else {
				String altName = fixName(name.toUpperCase());
				if (altName == null) {
					altName = name.toUpperCase();
				}
				if (altName != null && (! altName.equals(name))) {
					varNameMap.put(name, altName);
					varNameMap.put(altName, name);
					name = altName;
				}				
			}
			paramNames.add(name);
			paramTypes.add(paramClass);
		}
	}

	private final static char SPECIAL_CHAR_REPLACEMENT = '_';

	private int fixedNameCount = 0;

	protected String fixName(String varName) {
        StringBuilder fixedName = new StringBuilder();
        boolean fixed = false;
        if (! Character.isJavaIdentifierStart(varName.charAt(0))) {
            fixedName.append(SPECIAL_CHAR_REPLACEMENT);
            fixed = true;
        }
        for (int i = 0; i < varName.length(); i++) {
            char c = varName.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                fixedName.append(c);
            } else {
                fixedName.append(SPECIAL_CHAR_REPLACEMENT);
                fixed = true;
            }
        }
        if (fixed) {
            if (fixedNameCount > 0) {
                fixedName.append(fixedNameCount);
            }
            fixedNameCount++;
        }
        return fixedName.toString();
    }
}