package org.sebson.SQL;

import java.lang.Math;

public class Token {
	private Tokenizer tokenizer;
	private String tokenText;
	private String tokenTextUpperCase;
	private Quotation quotation;
	private Token previousToken;
	private Token nextToken;
	private KeyWord keyWord;
	
	private int position;
	private int lineNumber;
	
	public Token(Tokenizer tokenizer, String tokenText, Quotation quotation, Token previousToken, int position, int lineNumber) {
		
		this.tokenizer = tokenizer;
		this.tokenText = tokenText;
		this.tokenTextUpperCase = tokenText.toUpperCase();
		this.quotation = quotation;
		this.previousToken = previousToken;
		this.nextToken = null;
		
		if(this.previousToken != null) {
			this.previousToken.setNextToken(this);
		}
		
		this.setKeyWord();
		
		this.position = position;
		this.lineNumber = lineNumber;
		
	}
	
	public static Token createRootToken() {
		return new Token(
			null,
			"",
			Quotation.NONE,
			null,
			-1,
			-1
		);
	}
	
	public void overrideKeyWord(KeyWord keyWord) {
		this.keyWord = keyWord;
	}
	
	public void makeUpperCase() {
		this.tokenText = this.tokenTextUpperCase;
	}
	
	private void setKeyWord() {
		
		this.keyWord =
			KeyWord.findByDisplayName(
				this.tokenTextUpperCase
			);
		
		if(this.keyWord == KeyWord.UNKNOWN) {
			if(this.quotation == Quotation.SINGLE) {
				this.keyWord = KeyWord.SINGLE_QUOTED_LITERAL;
			} else if(this.quotation == Quotation.DOUBLE) {
				this.keyWord = KeyWord.DOUBLE_QUOTED_LITERAL;
			} else if(this.quotation == Quotation.NONE) {
				if(Token.isNumeric(this.tokenText)) {
					this.keyWord = KeyWord.NUMBER_ATOM;
				} else {
					this.keyWord = KeyWord.LITERAL;
				}
			}
		} else if(this.keyWord == KeyWord.COMMA) {
			if(this.quotation == Quotation.SINGLE) {
				this.keyWord = KeyWord.SINGLE_QUOTED_LITERAL;
			} else if(this.quotation == Quotation.DOUBLE) {
				this.keyWord = KeyWord.DOUBLE_QUOTED_LITERAL;
			}
		}
		
	}
	
	public String tokenInSQL() {
		return "\"" + this.sqlTokenizer().sql().substring(0, Math.max(this.position()-1,0)) + "\".\nGot: " + this.toString();
	}
	
	public Tokenizer sqlTokenizer() {
		return this.tokenizer;
	}
	
	public String tokenText() {
		return this.tokenText;
	}
	
	public String tokenTextUpperCase() {
		return this.tokenTextUpperCase;
	}
	
	public void append(String tokenText) {
		
		this.tokenText += tokenText;
		this.tokenTextUpperCase = this.tokenText.toUpperCase();
		
		this.setKeyWord();
		
	}
	
	public Quotation sqlQuotation() {
		return this.quotation;
	}
	
	public boolean quoted() {
		return (this.quotation != Quotation.NONE);
	}
	
	public boolean singleQuoted() {
		return (this.quotation == Quotation.SINGLE);
	}
	
	public boolean doubleQuoted() {
		return (this.quotation == Quotation.DOUBLE);
	}
	
	public Token previousToken() {
		return this.previousToken;
	}
	
	public Token nextToken() {
		return this.nextToken;
	}
	
	public void setNextToken(Token nextToken) {
		this.nextToken = nextToken;
	}
	
	public KeyWord keyWord() {
		return this.keyWord;
	}
	
	public int position() {
		return this.position;
	}
	
	public int lineNumber() {
		return this.lineNumber;
	}
	
	public String toString() {
		return
			this.quotation.toString() +
			this.tokenText +
			this.quotation.toString();
	}
	
	public static boolean isNumeric(String s) {  
		try {  
			double d = Double.parseDouble(s);  
		} catch(Exception e) {  
			return false;  
		}  
		return true;  
	}
}