package org.sebson.SQL;

import java.util.Stack;
import java.util.Arrays;
import java.sql.SQLException;

public class ExpressionTreeBuilder {
	
	private Stack<ExpressionToken> output;
	private Stack<ExpressionToken> operators;
	private ExpressionTreeNode expressionTreeRoot;
	
	public ExpressionTreeBuilder() {
		
		this.output = new Stack<ExpressionToken>();
		this.operators = new Stack<ExpressionToken>();
		this.expressionTreeRoot = null;
		
	}
	
	private boolean precedenceConditionIsMet(ExpressionToken token1, ExpressionToken token2) {
		
		OperatorType o1 = token1.operatorType(), o2 = token2.operatorType();
		
		if(o1.leftAssociativity() && o1.precedence() <= o2.precedence()) {
			return true;
		} else if(o1.rightAssociativity() && o1.precedence() < o2.precedence()) {
			return true;
		}
		
		return false;
		
	}
	
	// Shunting Yard algorithm for converting an infix expression to postfix notation (Reverse Polish Notation)
	public void addToken(ExpressionToken token) {
		
		if(token.isOperator()) {
			
			if(operators.size() == 0) {
				
				operators.push(token);
				
			} else if(operators.size() > 0) {
				
				while(!operators.isEmpty() && operators.peek().isOperator() && precedenceConditionIsMet(token, operators.peek())) {
					
					output.push(operators.pop());
					
				}
				
				operators.push(token);
				
			}
			
		} else if(token.keyWord() == KeyWord.BEGIN_PARANTHESES) {
				
			operators.push(token);
				
		} else if(token.keyWord() == KeyWord.END_PARANTHESES) {
			
			while(!operators.isEmpty() && operators.peek().keyWord() != KeyWord.BEGIN_PARANTHESES) {
				
				output.push(operators.pop());
				
			}
			
			if(operators.isEmpty() || operators.peek().keyWord() != KeyWord.BEGIN_PARANTHESES) {
				
				System.out.println("Mismatched parantheses. Missing left parantheses.");
				
				return;
				
			}
			
			operators.pop(); // Remove the left paranthesis
			
		} else {
			
			output.push(token);
			
		}
		
	}
	
	public void finish() {
		
		while(!operators.isEmpty()) {
			
			if(operators.peek().keyWord() == KeyWord.BEGIN_PARANTHESES ||
				operators.peek().keyWord() == KeyWord.END_PARANTHESES) {
				
				System.out.println("Mismatched parantheses");
				
				return;
				
			}
			
			output.push(operators.pop());
			
		}
		
		this.expressionTreeRoot =
			this.buildExpressionTree(
				output.toArray(new ExpressionToken[output.size()])
			);
		
	}
	
	public ExpressionTreeNode expressionTreeRoot() {
		
		return this.expressionTreeRoot;
		
	}
	
	private ExpressionTreeNode buildExpressionTree(ExpressionToken[] postFixExpression) {
		
		Stack<ExpressionTreeNode> expressionTreeNodes =
			new Stack<ExpressionTreeNode>();
			
		for(int i=0; i<postFixExpression.length; i++) {
			
			if(postFixExpression[i].isOperator()) {
				
				expressionTreeNodes.push(
					new ExpressionTreeNode(
						postFixExpression[i],
						null,
						expressionTreeNodes.pop(),
						expressionTreeNodes.pop()
					)
				);
				
			} else {
				
				expressionTreeNodes.push(
					new ExpressionTreeNode(
						postFixExpression[i],
						null,
						null,
						null
					)
				);
				
			}
			
		}
		
		if(expressionTreeNodes.isEmpty()) {
			return null;
		}
		
		return expressionTreeNodes.pop();
		
	}
	
	public static ExpressionToken[] tokenizeExpression(String expression) throws SQLException {
		
		Tokenizer st =
			new Tokenizer(
				expression
			);
		
		Token[] sqlTokens =
			st.tokens();
		
		ExpressionToken[] expressionTokens =
			new ExpressionToken[sqlTokens.length];
		
		int i = 0;
		
		for(Token token : st.tokens()) {
			
			expressionTokens[i++] =
				new ExpressionToken(token);
			
		}
		
		return expressionTokens;
		
	}
	
	public static void main(String args[]) throws SQLException {
		
		String expression =
			"21+3*(4+5)/6*7 OR 1 AND 2^4 AND 1=1";
		
		String expression2 = "1 or 2";
		
		ExpressionToken[] expressionTokens =
			ExpressionTreeBuilder.tokenizeExpression(
				expression
			);
		
		ExpressionTreeBuilder etb = 
			new ExpressionTreeBuilder();
			
		for(ExpressionToken expressionToken : expressionTokens) {
			
			System.out.println(expressionToken);
			
			etb.addToken(expressionToken);
			
		}
		
		etb.finish();
		
		System.out.println(
			etb.expressionTreeRoot().toString()
		);
		
	}
}