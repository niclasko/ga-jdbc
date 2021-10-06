package org.sebson.SQL;

import java.util.HashMap;

public enum KeyWord {
	
	SELECT("SELECT", KeyWordType.CLAUSE),
	TOP("TOP", KeyWordType.MODIFIER),
	DISTINCT("DISTINCT", KeyWordType.MODIFIER),
	FROM("FROM", KeyWordType.CLAUSE),
	JOIN("JOIN", KeyWordType.JOIN),
	INNER_JOIN("INNER JOIN", KeyWordType.JOIN),
	LEFT_JOIN("LEFT JOIN", KeyWordType.JOIN),
	RIGHT_JOIN("RIGHT JOIN", KeyWordType.JOIN),
	FULL_OUTER_JOIN("FULL OUTER JOIN", KeyWordType.JOIN),
	UNION("UNION", KeyWordType.SET_OPERATION),
	UNION_ALL("UNION ALL", KeyWordType.SET_OPERATION),
	INTERSECT("INTERSECT", KeyWordType.SET_OPERATION),
	Q("Q", KeyWordType.CLAUSE),
	NQ("NQ", KeyWordType.CLAUSE),
	CSV("CSV", KeyWordType.CLAUSE),
	XL("XL", KeyWordType.CLAUSE),
	JSON("JSON", KeyWordType.CLAUSE),
	GEOCODE("GEOCODE", KeyWordType.FUNCTION),
	FUNCTION_NAME("FUNCTION_NAME", KeyWordType.FUNCTION),
	SET_FUNCTION_NAME("SET_FUNCTION_NAME", KeyWordType.FUNCTION),
	ON("ON", KeyWordType.JOIN_FILTER),
	WITH("WITH", KeyWordType.CLAUSE),
	WHERE("WHERE", KeyWordType.CLAUSE),
	GROUP_BY("GROUP BY", KeyWordType.CLAUSE),
	HAVING("HAVING", KeyWordType.CLAUSE),
	ORDER_BY("ORDER BY", KeyWordType.CLAUSE),
	BEGIN_PARANTHESES("(", KeyWordType.PARANTHESES),
	END_PARANTHESES(")", KeyWordType.PARANTHESES),
	SUB("SUB", KeyWordType.SUB),
	AS("AS", KeyWordType.ALIAS_PREFIX),
	LITERAL("LITERAL", KeyWordType.ATOM),
	SINGLE_QUOTED_LITERAL("SINGLE_QUOTED_LITERAL", KeyWordType.ATOM),
	DOUBLE_QUOTED_LITERAL("SINGLE_QUOTED_LITERAL", KeyWordType.ATOM),
	NUMBER_ATOM("NUMBER_ATOM", KeyWordType.ATOM),
	JSON_DATA_STRUCTURE("JSON_DATA_STRUCTURE", KeyWordType.ATOM),
	COMMA(",", KeyWordType.LIST_DELIMITER),
	PLUS("+", KeyWordType.BINARY_OPERATOR),
	CONCATENATE("||", KeyWordType.BINARY_OPERATOR),
	MINUS("-", KeyWordType.BINARY_OPERATOR),
	MULTIPLY("*", KeyWordType.BINARY_OPERATOR),
	DIVIDE("/", KeyWordType.BINARY_OPERATOR),
	POWER("^", KeyWordType.BINARY_OPERATOR),
	GREATER_THAN(">", KeyWordType.BINARY_OPERATOR),
	LESS_THAN("<", KeyWordType.BINARY_OPERATOR),
	GREATER_THAN_OR_EQUALS(">=", KeyWordType.BINARY_OPERATOR),
	LESS_THAN_OR_EQUALS("<=", KeyWordType.BINARY_OPERATOR),
	EQUALS("=", KeyWordType.BINARY_OPERATOR),
	NOT_EQUALS("!=", KeyWordType.BINARY_OPERATOR),
	JSON_MEMBER("->", KeyWordType.JSON_OPERATOR),
	IS("IS", KeyWordType.BINARY_OPERATOR),
	AND("AND", KeyWordType.BINARY_OPERATOR),
	OR("OR", KeyWordType.BINARY_OPERATOR),
	IN("IN", KeyWordType.LIST_OPERATOR),
	NOT("NOT", KeyWordType.NEGATOR),
	NULL("NULL", KeyWordType.NULL),
	COUNT("COUNT", KeyWordType.AGGREGATE_OPERATOR),
	SUM("SUM", KeyWordType.AGGREGATE_OPERATOR),
	MIN("MIN", KeyWordType.AGGREGATE_OPERATOR),
	MAX("MAX", KeyWordType.AGGREGATE_OPERATOR),
	AVG("AVG", KeyWordType.AGGREGATE_OPERATOR),
	STDDEV("STDDEV", KeyWordType.AGGREGATE_OPERATOR),
	NTILE("NTILE", KeyWordType.AGGREGATE_OPERATOR),
	LAG("LAG", KeyWordType.AGGREGATE_OPERATOR),
	LEAD("LEAD", KeyWordType.AGGREGATE_OPERATOR),
	OVER("OVER", KeyWordType.WINDOW_SPECIFIER),
	PARTITION_BY("PARTITION BY", KeyWordType.WINDOW_SPECIFIER),
	ASC("ASC", KeyWordType.SORT_ORDER),
	DESC("DESC", KeyWordType.SORT_ORDER),
	CASE("CASE", KeyWordType.LOGIC),
	WHEN("WHEN", KeyWordType.LOGIC),
	THEN("THEN", KeyWordType.LOGIC),
	ELSE("ELSE", KeyWordType.LOGIC),
	BETWEEN("BETWEEN", KeyWordType.LOGIC),
	END("END", KeyWordType.LOGIC),
	CAST("CAST", KeyWordType.FUNCTION),
	NUMBER("NUMBER", KeyWordType.DATATYPE),
	TRIM("TRIM", KeyWordType.FUNCTION),
	TOFILE("TOFILE", KeyWordType.SPECIAL),
	UNKNOWN("UNKNOWN", KeyWordType.UNKNOWN);
	
	private static final HashMap<String, KeyWord> displayNameMap;
	
	static {
		
		displayNameMap = new HashMap<String, KeyWord>();
		
		for (KeyWord kw : KeyWord.values()) {
			displayNameMap.put(kw.displayName, kw);
		}
	}
	
	public static KeyWord findByDisplayName(String displayName) {
		
		KeyWord sqlKeyWord =
			displayNameMap.get(displayName);
			
		if(sqlKeyWord == null) {
			sqlKeyWord = KeyWord.UNKNOWN;
		}
		
		return sqlKeyWord;
	}
	
	private String displayName;
	private KeyWordType keyWordType;

	KeyWord(String displayName, KeyWordType keyWordType) {
		this.displayName = displayName;
		this.keyWordType = keyWordType;
	}

	public String displayName() {
		return displayName;
	}
	
	public KeyWordType keyWordType() {
		return this.keyWordType;
	}
	
}