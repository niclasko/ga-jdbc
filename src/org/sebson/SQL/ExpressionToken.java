package org.sebson.SQL;

public class ExpressionToken {
	
	private Token token;
	private OperatorType operatorType;
	
	public ExpressionToken(Token token) {
		
		this.token = token;
		this.operatorType =
			OperatorType.findByDisplayName(
				token.keyWord().displayName()
			);
		
	}
	
	public OperatorType operatorType() {
		return this.operatorType;
	}
	
	public Token token() {
		return this.token;
	}
	
	public KeyWord keyWord() {
		return this.token.keyWord();
	}
	
	public boolean isOperator() {
		return this.operatorType != OperatorType.NONE;
	}
	
	public String toString() {
		return this.token.tokenText() + " (" + this.operatorType + ")";
	}
	
}