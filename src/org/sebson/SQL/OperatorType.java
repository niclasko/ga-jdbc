package org.sebson.SQL;

import java.util.HashMap;

public enum OperatorType {
	
	POWER("^", 11, false),
		
	MULTIPLY("*", 10, true),
	DIVIDE("/", 10, true),
	MODULO("%", 10, true),
	
	PLUS("+", 9, true),
	MINUS("-", 9, true),
	CONCATENATE("||", 9, true),
	
	GREATER_THAN(">", 8, true),
	LESS_THAN("<", 8, true),
	GREATER_THAN_OR_EQUALS(">=", 8, true),
	LESS_THAN_OR_EQUALS("<=", 8, true),
		
	EQUALS("=", 7, true),
	NOT_EQUALS("!=", 7, true),
	IS("IS", 7, true),
	
	AND("AND", 6, true),
	OR("OR", 5, true),

	FUNCTION("FUNCTION", -1, true),

	NONE("NONE", -1, true);
	
	private static final HashMap<String, OperatorType> displayNameMap;
	
	static {
		
		displayNameMap = new HashMap<String, OperatorType>();
		
		for (OperatorType ot : OperatorType.values()) {
			displayNameMap.put(ot.displayName(), ot);
		}
	}
	
	public static OperatorType findByDisplayName(String displayName) {
		
		OperatorType operatorType =
			displayNameMap.get(displayName);
			
		if(operatorType == null) {
			operatorType = OperatorType.NONE;
		}
		
		return operatorType;
	}
	
	private String displayName;
	private int precedence;
	private boolean leftAssociativity;

	OperatorType(String displayName, int precedence, boolean leftAssociativity) {
		this.displayName = displayName;
		this.precedence = precedence;
		this.leftAssociativity = leftAssociativity;
	}
	
	public String displayName() {
		return displayName;
	}
	
	public int precedence() {
		return this.precedence;
	}
	
	public boolean leftAssociativity() {
		return this.leftAssociativity;
	}
	
	public boolean rightAssociativity() {
		return !this.leftAssociativity;
	}
	
}