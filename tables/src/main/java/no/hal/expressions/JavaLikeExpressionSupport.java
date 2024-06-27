package no.hal.expressions;

public abstract class JavaLikeExpressionSupport extends ExpressionSupport {

	private static String INT_REGEX = "\\d+"; 
	private static String STRING_REGEX = "\".*\"";
	private static String ANY_REGEX = ".+";

	private static String[] REWRITE_PATTERNS = {
		// numeric comparison
		"([<>]=?)(" + INT_REGEX + ")", 	"it %s %s",
		"==(" + INT_REGEX + ")", 		"it == %s",
		// equals
		"==(" + ANY_REGEX + ")", 		"Objects.equals(it, %s)",
		"!=(" + ANY_REGEX + ")", 		"! Objects.equals(it, %s)",
		// compareTo
		"<>(" + ANY_REGEX + ")", 		"it.compareTo(%s) != 0",
		"<(" + ANY_REGEX + ")", 		"it.compareTo(%s) < 0",
		">(" + ANY_REGEX + ")", 		"it.compareTo(%s) > 0",
		"<=(" + ANY_REGEX + ")",		 "it.compareTo(%s) <= 0",
		">=(" + ANY_REGEX + ")", 		"it.compareTo(%s) >= 0",
		// regex match
		"~=/(" + ANY_REGEX + ")/",		"it.matches(\"%s\")",
		"~~/(" + ANY_REGEX + ")/", 		"it.matches(\"(?:.*)%s(?:.*)\")"
	};
	
	public JavaLikeExpressionSupport() {
		addRewritePatterns(REWRITE_PATTERNS);
	}
}