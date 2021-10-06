package org.sebson.SQL;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.ArrayList;

public class ParseTreeNode extends ExpressionToken {
	
	private Token token;
	private ParseTreeNode parent;
	private LinkedList<ParseTreeNode> children;
	private HashMap<String, ParseTreeNode> childLookup;
	
	private boolean addNewChildrentToExpression;
	
	private static ParseTreeNode nullNode =
		new ParseTreeNode(
			Token.createRootToken(),
			null
		);
	
	public ParseTreeNode(Token token, ParseTreeNode parent) {
		
		super(token);
		
		this.token = token;
		this.parent = parent;
			
		this.children = new LinkedList<ParseTreeNode>();
		this.childLookup = new HashMap<String, ParseTreeNode>();
		
		this.addNewChildrentToExpression = false;
		
	}
	
	public Token token() {
		return this.token;
	}
	
	public KeyWord keyWord() {
		return this.token.keyWord();
	}
	
	public ParseTreeNode getParent() {
		return this.parent;
	}

	public ParseTreeNode getChild(String key) {
		return this.childLookup.get(key);
	}

	public ParseTreeNode getChild(KeyWord keyWord) {
		return this.childLookup.get(keyWord.displayName());
	}
	
	public ParseTreeNode addChild(ParseTreeNode child) {
		
		this.children.add(child);
		this.childLookup.put(
			child.token().tokenText().toUpperCase(),
			child
		);
		
		return child;
	}
	
	public LinkedList<ParseTreeNode> getChildren() {
		return this.children;
	}

	public ArrayList<String> getChildTokens() {
		ArrayList<String> childTokens = new ArrayList<String>();
		for(ParseTreeNode child : this.getChildren()) {
			if(child.token().keyWord() == KeyWord.COMMA) {
				continue;
			}
			childTokens.add(child.token().tokenText());
		}
		return childTokens;
	}
	
	public ParseTreeNode getNthChild(int n) {
		if(n > this.childCount()) {
			return ParseTreeNode.nullNode;
		}
		return this.children.get(n);
	}
	
	public ParseTreeNode getFirstChild() {
		return this.children.peekFirst();
	}
	
	public ParseTreeNode getLastChild() {
		return this.children.peekLast();
	}
	
	public int childCount() {
		return this.children.size();
	}
	
	public void setAddNewChildrentToExpression(boolean addNewChildrentToExpression) {
		this.addNewChildrentToExpression = addNewChildrentToExpression;
	}
	
	public boolean addNewChildrentToExpression() {
		return this.addNewChildrentToExpression;
	}
	
	public String toString() {
		
		if(this.token == null) {
			
			return "[ROOT]";
			
		}
		
		if(this.keyWord() == KeyWord.SET_FUNCTION_NAME) {
			return this.token().toString() +
					" (" + this.keyWord().keyWordType() + ")";
		}
		
		return this.keyWord().toString() +
				" (" + this.keyWord().keyWordType() + ")";
	}
}