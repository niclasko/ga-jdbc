package org.sebson.SQL;

public class ExpressionTreeNode {
	
	private Object element;
	private ExpressionTreeNode parent;
	private ExpressionTreeNode leftChild;
	private ExpressionTreeNode rightChild;
	
	public ExpressionTreeNode(Object element, ExpressionTreeNode parent, ExpressionTreeNode rightChild, ExpressionTreeNode leftChild) {
		
		this.element = element;
		this.parent = parent;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		
		if(this.leftChild != null) {
			this.leftChild.setParent(this);
		}
		
		if(this.rightChild != null) {
			this.rightChild.setParent(this);
		}
		
	}
	
	public Object element() {
		return this.element;
	}
	
	public ExpressionTreeNode parent() {
		return this.parent;
	}
	
	public void setParent(ExpressionTreeNode parent) {
		this.parent = parent;
	}
	
	public ExpressionTreeNode leftChild() {
		return this.leftChild;
	}
	
	public void setLeftChild(ExpressionTreeNode leftChild) {
		this.leftChild = leftChild;
	}
	
	public ExpressionTreeNode rightChild() {
		return this.rightChild;
	}
	
	public void setRightChild(ExpressionTreeNode rightChild) {
		this.rightChild = rightChild;
	}
	
	public String toString() {
		
		StringBuilder treeStringBuilder =
			new StringBuilder();
		
		ExpressionTreeNode.print(
			treeStringBuilder,
			this,
			0
		);
		
		return treeStringBuilder.toString();
		
	}
	
	private static String rightPad(String stringToPad, int paddedLength, char padding) {
		
		StringBuilder paddedString = new StringBuilder(stringToPad);
		
		while(paddedString.length() < paddedLength) {
			
			paddedString.append(padding);
			
		}
		
		return paddedString.toString();
		
	}
	
	private static String leftPad(String stringToPad, int paddedLength, char padding) {
		
		StringBuilder paddedString = new StringBuilder(stringToPad);
		
		while(paddedString.length() < paddedLength) {
			
			paddedString.insert(0, padding);
			
		}
		
		return paddedString.toString();
		
	}
	
	private static void print(StringBuilder treeStringBuilder, ExpressionTreeNode node, int depth) {
		
		if(node == null) {
			
			return;
			
		}
		
		if(node.parent() != null) {
		
			treeStringBuilder.append("\n");
		
		}
		
		treeStringBuilder.append(
			ExpressionTreeNode.leftPad(
				" " + node.element(),
				node.element().toString().length() + 1 + depth + 1,
				'-'
			)
		);
		
		ExpressionTreeNode.print(
			treeStringBuilder,
			node.leftChild(),
			depth + 1
		);
		
		ExpressionTreeNode.print(
			treeStringBuilder,
			node.rightChild(),
			depth + 1
		);
		
	}
	
}