package org.sebson.SQL;

public enum Quotation {
	NONE('\0'),
	SINGLE('\''),
	DOUBLE('"');
	
	private char quotationSymbol;
	
	Quotation(char quotationSymbol) {
		this.quotationSymbol = quotationSymbol;
	}
	
	public String toString() {
		return (this.quotationSymbol != '\0' ? Character.toString(quotationSymbol) : "");
	}
}