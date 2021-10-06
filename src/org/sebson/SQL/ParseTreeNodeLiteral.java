package org.sebson.SQL;

public class ParseTreeNodeLiteral extends ParseTreeNode {
	private String literal;
	
	public ParseTreeNodeLiteral(Token token, ParseTreeNode parent) {
		super(token, parent);
		
		this.literal = token.tokenText();
	}
	
	public String getLiteral() {
		return this.literal;
	}
	
	public String toString() {
		return super.toString() + ": " + this.literal;
	}
}