package org.sebson.SQL;

public class ParseTreeNodeNumber extends ParseTreeNode {
	private double number;
	
	public ParseTreeNodeNumber(Token token, ParseTreeNode parent) {
		super(token, parent);
		
		this.number = Double.parseDouble(token.tokenText());
	}
	
	public double getNumber() {
		return this.number;
	}
	
	public String toString() {
		return super.toString() + ": " + this.number;
	}
}