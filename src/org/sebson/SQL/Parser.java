package org.sebson.SQL;

import java.lang.reflect.Method;
import java.util.Stack;
import java.util.Vector;
import java.util.Scanner;

import java.sql.SQLException;

public class Parser {
	
	private Stack<ParseTreeNode> parseTreeRootNodes;
	private Stack<ParseTreeNode> openingParantheses;
	private Stack<ParseTreeNode> functions;
	private Stack<ExpressionTreeBuilder> expressionTreeBuilders;
	
	private ParseTreeNode currentParseTreeRootNode;
	private ParseTreeNode currentParseTreeNode;
	
	private Token[] tokens;
	private int currentTokenIndex;
	private Token currentToken;

	public Parser(String sql) throws SQLException {

		Tokenizer tokenizer =
			new Tokenizer(
				sql
			);
		
		this.tokens =
			tokenizer.tokens();
			
		this.parseTreeRootNodes =
			new Stack<ParseTreeNode>();
			
		this.openingParantheses =
			new Stack<ParseTreeNode>();
			
		this.functions =
			new Stack<ParseTreeNode>();
		
		this.expressionTreeBuilders =
			new Stack<ExpressionTreeBuilder>();
			
		this.currentParseTreeRootNode =
			new ParseTreeNode(Token.createRootToken(), null);
		
		this.currentTokenIndex = -1;
		this.currentToken = null;
		
		this.BEGIN_SQL_STATEMENT();
		this.END_SQL_STATEMENT();
		
	}
	
	private Token currentToken() {
		return this.currentToken;
	}
	
	private Token nextToken() {
		
		if(this.currentToken == null) {
			
			if(this.tokens != null && this.tokens.length > 0) {
				return this.tokens[0];
			}
			
			return null;
		}
		
		return this.currentToken.nextToken();
	}
	
	private ParseTreeNode currentParseTreeNode() {
		return this.currentParseTreeNode;
	}
	
	private void consume() throws SQLException {
			
		this.consume(null);

	}
	
	private void consume(String methodName) throws SQLException {
		try {

			String _methodName = methodName;

			if(this.currentTokenIndex == this.tokens.length-1) {
				return;
			}

			this.currentToken =
				this.tokens[++this.currentTokenIndex];

			if(_methodName == null) {
				_methodName = this.currentToken.keyWord().toString();
			}

			Method method = null;

			method =
					this.getClass().getMethod(_methodName);

			method.invoke(
					this,
					new Object[]{}
			);
		} catch(Exception e) {
			throw new SQLException(e.getMessage());
		}
	}
	
	private String getCurrentMethodName() {
		
		String methodName;
		
		StackTraceElement stackTraceElements[] =
			(new Throwable()).getStackTrace();
		
		methodName =
			stackTraceElements[1].toString();
			
		methodName =
			methodName.substring(
				methodName.indexOf(".") + 1,
				methodName.indexOf("(")
			);
		
		return methodName;
	}
	
	private void setCurrentParseTreeRootNode(ParseTreeNode parseTreeNode) {
		
		this.parseTreeRootNodes.push(
			currentParseTreeRootNode
		);
		
		this.currentParseTreeRootNode = parseTreeNode;
		
	}
	
	private void setPreviousParseTreeRootNode() {
		
		this.currentParseTreeRootNode =
			this.parseTreeRootNodes.pop();
		
	}
	
	private void beginFunction() {
		
		this.addChildNode();
		
		this.functions.push(
			this.currentParseTreeNode()
		);
		
	}
	
	private void endFunction() {
		
		if(this.functions.size() > 0) {
			
			this.currentParseTreeNode =
				this.functions.pop().getParent();
			
		}
		
	}
	
	private void setOpeningParantheses() {
		
		if(this.currentParseTreeNode().keyWord() == KeyWord.BEGIN_PARANTHESES) {
			
			this.openingParantheses.push(
				this.currentParseTreeNode()
			);
			
		}
		
	}
	
	private void setClosingParantheses() {
		
		if(this.openingParantheses.size() > 0) {
			
			this.currentParseTreeNode =
				this.openingParantheses.pop().getParent();
			
		}
		
	}
	
	private void resetCurrentNode() {
		this.currentParseTreeNode =
			this.currentParseTreeRootNode;
	}
	
	private ParseTreeNode createSQLParseTreeNode() {
		
		return new ParseTreeNode(
				this.currentToken,
				this.currentParseTreeNode
			);
		
	}
	
	private void addChildNode() {
		
		ParseTreeNode childNode =
			this.createSQLParseTreeNode();
		
		if(this.currentParseTreeNode().addNewChildrentToExpression()) {
			
			this.expressionTreeBuilders.peek().addToken(
				childNode
			);
			
		}
		
		this.currentParseTreeNode = 
			this.currentParseTreeNode.addChild(
				childNode
			);
		
	}
	
	private void addSibling() {
		
		this.addSibling(
			this.createSQLParseTreeNode()
		);
		
	}
	
	private void addSibling(ParseTreeNode sibling) {
		
		this.currentParseTreeNode.addChild(
			sibling
		);
		
		if(this.currentParseTreeNode().addNewChildrentToExpression()) {
			
			this.expressionTreeBuilders.peek().addToken(
				sibling
			);
			
		}
		
	}
	
	private void addNumber() {
		
		this.addSibling(
			new ParseTreeNodeNumber(
				this.currentToken,
				this.currentParseTreeNode
			)
		);
		
	}
	
	private void addLiteral() {
		
		this.addSibling(
			new ParseTreeNodeLiteral(
				this.currentToken,
				this.currentParseTreeNode
			)
		);
		
	}
	
	/*
	** Process SQL Key Words below
	*/
	
	public void BEGIN_SQL_STATEMENT() throws SQLException {

		boolean with = false;

		if(this.with()) {
			
			this.consume();

			with = true;
			
			if(!this.parseWithList()) {
				
				this.error("Expected aliased parantheses-enclosed sub-Query, alias as (sub-Query).");
				
			}
			
		}
		
		if(this.select()) {

			this.currentParseTreeNode =
				this.currentParseTreeRootNode;
			
			this.consume();
			
			if(this.top()) {
				
				this.consume();
				
				if(this.number()) {
					
					this.consume();
					
				} else {
					
					this.error("Expected " + KeyWord.NUMBER);
					
				}
				
			}
			
			if(this.distinct()) {
				
				this.consume();
				
			}
			
			if(this.star()) {
				
				this.consume();
				
			} else if(!this.star()) {
				
				this.parseSelectElement();
				
			}
			
			if(this.from()) {
				
				this.consume();
				
				this.parseFromElement();
				
				if(this.where()) {
					
					this.consume();
					
					this.parseWhereClause();
					
				}
				
				if(this.groupBy()) {

					this.consume();

					this.parseField();

				}

				if(this.orderBy()) {

					this.consume();

					this.parseOrderByField();

				}
				
			}

			if(this.setOperation()) {

				this.consume();

				this.BEGIN_SQL_STATEMENT();

			}

			this.parseToFile();
			
		} else {
			
			this.error("Expected " + KeyWord.SELECT);
			
		}
		
	}
	
	public void SELECT() { this.resetCurrentNode(); this.addChildNode(); }
	
	public void TOP() { this.addSibling(); }
	
	public void DISTINCT() { this.addSibling(); }
	
	public void STAR() { this.addLiteral(); }
	
	public void FROM() { this.resetCurrentNode(); this.addChildNode(); }
	public void JOIN() {
		if(this.currentToken().previousToken().keyWord() == KeyWord.JSON_MEMBER) {
			this.beginFunction();
			return;
		}
		this.addSibling();
	}
	public void INNER_JOIN() { this.addSibling(); }
	public void LEFT_JOIN() { this.addSibling(); }
	public void RIGHT_JOIN() { this.addSibling(); }
	public void FULL_OTER_JOIN() { this.addSibling(); }
	public void ON() { this.addSibling(); }
	public void WITH() { this.resetCurrentNode(); this.addChildNode(); }
	public void WHERE() { this.resetCurrentNode(); this.addChildNode(); }
	public void GROUP_BY() { this.resetCurrentNode(); this.addChildNode(); }
	public void HAVING() { this.resetCurrentNode(); this.addChildNode(); }
	public void ORDER_BY() { this.resetCurrentNode(); this.addChildNode(); }
	
	public void UNION() { this.resetCurrentNode(); this.addLiteral(); }
	public void UNION_ALL() { this.resetCurrentNode(); this.addLiteral(); }
	public void INTERSECT() { this.resetCurrentNode(); this.addLiteral(); }
	
	public void BEGIN_PARANTHESES() {
		/*this.addChildNode();
		this.setOpeningParantheses();*/
		this.addSibling();
	}
	
	public void END_PARANTHESES() {
		//this.setClosingParantheses();
		this.addSibling();
	}
	
	public void BEGIN_FUNCTION_PARANTHESES() {
		this.addChildNode();
		this.setOpeningParantheses();
	}
	
	public void END_FUNCTION_PARANTHESES() {
		this.setClosingParantheses();
		this.addSibling();
	}
	
	public void BEGIN_SUBSET_PARANTHESES() {
		this.addChildNode();
		this.setOpeningParantheses();
	}
	
	public void END_SUBSET_PARANTHESES() {
		this.setClosingParantheses();
		this.addSibling();
	}
	
	public void AS() { this.addSibling(); }
	public void LITERAL() { this.addLiteral(); }
	public void SINGLE_QUOTED_LITERAL() { this.addLiteral(); }
	public void DOUBLE_QUOTED_LITERAL() { this.addLiteral(); }
	public void NUMBER_ATOM() { this.addNumber(); }
	public void JSON_DATA_STRUCTURE() { this.addLiteral();}
	public void COMMA() { this.addSibling(); }
	
	public void JSON_MEMBER() { this.addSibling(); }
	
	// Special Fuse function to allow for cross-system
	// sub-queries
	public void Q() { this.beginFunction(); }
	// Special Fuse function to allow for named queries
	public void NQ() { this.beginFunction(); }
	// Special Fuse function to allow for querying CSV
	// files from file system or web URL
	public void CSV() { this.beginFunction(); }
	// Special Fuse function to allow for querying Excel
	// files from file system or web URL
	public void XL() { this.beginFunction(); }
	// Special Fuse function to allow for querying JSON
	// files from file system or web URL
	public void JSON() { this.beginFunction(); }
	
	public void GEOCODE() { this.beginFunction(); }
	
	public void CAST() { this.beginFunction(); }
	
	public void NUMBER() { this.addLiteral(); }
	
	public void TRIM() { this.beginFunction(); }
	
	public void SET_FUNCTION_NAME() {
		this.currentToken.overrideKeyWord(KeyWord.SET_FUNCTION_NAME);
		this.currentToken.makeUpperCase();
		this.beginFunction(); 
	}
	
	public void PLUS() { this.addSibling(); }
	public void MINUS() { this.addSibling(); }
	public void MULTIPLY() { this.addSibling(); }
	public void DIVIDE() { this.addSibling(); }
	public void POWER() { this.addSibling(); }
	public void GREATER_THAN() { this.addSibling(); }
	public void LESS_THAN() { this.addSibling(); }
	public void GREATER_THAN_OR_EQUALS() { this.addSibling(); }
	public void LESS_THAN_OR_EQUALS() { this.addSibling(); }
	public void EQUALS() { this.addSibling(); }
	public void NOT_EQUALS() { this.addSibling(); }
	public void IS() { this.addSibling(); }
	public void NOT() { this.addSibling(); }
	public void NULL() { this.addSibling(); }
	
	public void COUNT() { this.beginFunction(); }
	public void SUM() { this.beginFunction(); }
	public void MIN() { this.beginFunction(); }
	public void MAX() { this.beginFunction(); }
	public void AVG() { this.beginFunction(); }
	public void STDDEV() { this.beginFunction(); }
	public void NTILE() { this.beginFunction(); }
	public void LAG() { this.beginFunction(); }
	public void LEAD() { this.beginFunction(); }
	public void OVER() { this.beginFunction(); }
	public void PARTITION_BY() { this.addChildNode(); }
	
	public void ASC() { this.addLiteral(); }
	public void DESC() { this.addLiteral(); }
	
	public void CASE() { this.addSibling(); }
	public void WHEN() { this.addSibling(); }
	public void THEN() { this.addSibling(); }
	public void ELSE() { this.addSibling(); }
	public void END() { this.addSibling(); }
	
	public void AND() { this.addSibling(); }
	public void OR() { this.addSibling(); }

	public void BETWEEN() { this.addSibling(); }

	public void TOFILE() { this.addSibling(); }
	
	public void END_SQL_STATEMENT() {  }
	
	/*
	** Parser helper methods
	*/
		
	private boolean parseWithList() throws SQLException {
		
		boolean found = false;
		
		if(this.literal()) {
			
			this.consume();

			String subQueryAlias = this.currentToken.tokenText();
			
			if(this.as()) {
				
				this.consume();
				
				if(this.beginParantheses()) {
		
					this.consume("BEGIN_SUBSET_PARANTHESES");
		
					if(this.begin_query()) {
			
						this.setCurrentParseTreeRootNode(
							this.currentParseTreeNode()
						);
			
						this.BEGIN_SQL_STATEMENT();
			
						this.setPreviousParseTreeRootNode();
			
					}
		
					if(this.endParantheses()) {
			
						this.consume("END_SUBSET_PARANTHESES");
			
					} else {
			
						this.error("Expected " + KeyWord.END_PARANTHESES);
			
					}
		
				}
				
			} else {
				
				this.error("Expected " + KeyWord.AS);
				
			}
			
			if(this.comma()) {
				
				this.consume();
				
				if(!this.parseWithList()) {
					
					this.error("Expected another aliased sub-Query.");
					
				}
				
			}
			
			found = true;
			
		} else {
			
			this.error("Expected alias for with sub-Query.");
			
		}
		
		return found;
		
	}

	private boolean dataSetStar() throws SQLException {

		if(this.literal()) {

			FieldReference fieldReference =
				new FieldReference(
					this.nextToken().tokenText()
				);

			if(fieldReference.star()) {

				this.consume();

				return true;

			}

		}

		return false;

	}
	
	private void parseSelectElement() throws SQLException {
		
		if(!dataSetStar() && !this.parseExpressionAndBuildTree()) {
			
			this.error("Expected expression or *.");
			
		}
		
		this.parseAlias();

		if(this.comma()) {

			this.consume();

			this.parseSelectElement();

		}
	
	}
	
	private boolean parseAlias() throws SQLException {
		
		boolean foundAlias = false;
		
		// Alias with AS
		if(this.as()) {
			
			this.consume();
			
			if(this.literal()) {
				
				this.consume();
				
			} else {
				
				this.error("Expected literal.");
				
			}
			
			foundAlias = true;
			
		// Alias without as
		} else if(this.literal()) {
			
			this.consume();

			foundAlias = true;
			
		}
		
		return foundAlias;
		
	}
	
	private void parseFromElement() throws SQLException {
		this.parseFromElement(true);
	}
	
	private void parseFromElement(boolean required) throws SQLException {
		
		if(!this.parseFromElementSet() && required) {
				
			this.error("Expected data set.");
				
		}
		
		if(this.comma()) {
			
			this.consume();
			
			this.parseFromElement(true);
			
		} else if(this.join()) {
			
			this.consume();
			
			if(!this.parseFromElementSet()) {
				
				this.error("Expected data set.");
				
			}
			
			if(this.on()) {

				this.consume();
				
				this.parseJoinCondition();

			} else {

				this.error("Expected ON-keyword.");

			}
			
			this.parseFromElement(false);
			
		}
		
	}
	
	private boolean parseFromElementSet() throws SQLException {
		
		boolean found = false;
		
		if(this.beginParantheses()) {
			
			this.consume("BEGIN_SUBSET_PARANTHESES");
			
			if(this.begin_query()) {
				
				this.setCurrentParseTreeRootNode(
					this.currentParseTreeNode()
				);
				
				this.BEGIN_SQL_STATEMENT();
				
				this.setPreviousParseTreeRootNode();
				
			}
			
			if(this.endParantheses()) {
				
				this.consume("END_SUBSET_PARANTHESES");
				
			} else {
				
				this.error("Expected " + KeyWord.END_PARANTHESES);
				
			}

			this.parseAlias();

			found = true;
			
		} else if(this.q()) { 
			
			this.consume();
			
			this.parseQ();

			if(!this.parseAlias()) {

				this.error("Expected alias for sub-Query.");

			}
			
			found = true;
			
		} else if(this.nq()) {
		
			this.consume();
			
			this.parseNQ();

			if(!this.parseAlias()) {

				this.error("Expected alias for sub-Query.");

			}
			
			found = true;
			
		} else if(this.csv()) {
		
			this.consume();
			
			this.parseCSV();

			if(!this.parseAlias()) {

				this.error("Expected alias for sub-Query.");

			}
			
			found = true;
			
		} else if(this.xl()) {
		
			this.consume();
			
			this.parseXL();

			this.parseAlias();
			
			found = true;
			
		} else if(this.json()) {
			
			this.consume();
			
			this.parseJSON();

			if(!this.parseAlias()) {

				this.error("Expected alias for sub-Query.");

			}
			
			found = true;
			
		} else if(this.literal()) {

			this.consume();

			this.parseAlias();

			found = true;

		}/* else if(this.set_function_name()) {

			found = this.parseFunction();

			if(!this.parseAlias()) {

				this.error("Expected alias for function call.");

			}

			if(found) {
				found = true;
			}

		}*/
		
		return found;
		
	}
	
	private void parseQ() throws SQLException { // Fuse cross-system sub-Query
		
		if(this.beginParantheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The FastBase connection Query

				if(this.comma()) {

					this.consume();

					if(this.literal()) {

						this.consume(); // The FastBase connection name

					}
					
					if(this.endParantheses()) {
						
						this.consume("END_FUNCTION_PARANTHESES");
						
					} else {
						
						this.error("Expected " + KeyWord.END_PARANTHESES);
						
					}
					

				} else {
					
					this.error("Expected FastBase connection name.");
					
				}

			} else {
				
				this.error("Expected single- or double-quoted Query for a FastBase connection.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + KeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseNQ() throws SQLException { // Fuse Named Query
		
		if(this.beginParantheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The Query name

				if(this.endParantheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected " + KeyWord.END_PARANTHESES);
					
				}

			} else {
				
				this.error("Expected single- or double-quoted Query name.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + KeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseCSV() throws SQLException { // Fuse CSV file reader
		
		if(this.beginParantheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The file URL

				if(this.comma()) {

					this.consume();

					if(this.literal()) {

						this.consume(); // The field delimiter

					}
					
					if(this.endParantheses()) {
						
						this.consume("END_FUNCTION_PARANTHESES");
						
					} else {
						
						this.error("Expected " + KeyWord.END_PARANTHESES);
						
					}
					

				} else {
					
					this.error("Expected single- or double-quoted file URL.");
					
				}

			} else {
				
				this.error("Expected single- or double-quoted field delimiter.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + KeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseXL() throws SQLException { // Fuse Excel file reader
		
		if(this.beginParantheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The Excel file URL

				if(this.endParantheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected " + KeyWord.END_PARANTHESES);
					
				}

			} else {
				
				this.error("Expected single- or double-quoted URL or file name.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + KeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseJSON() throws SQLException { // Fuse Excel file reader
		
		if(this.beginParantheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The URL, typically a REST API returning JSON
				
				if(this.comma()) {

					this.consume();

					if(this.json_data_structure()) {

						this.consume(); // The HTTP GET request headers as a JSON associative array

					} else {
						
						this.error("Expected single- or double-quoted  URL or file name.");
						
					}

				}

				if(this.endParantheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected " + KeyWord.END_PARANTHESES);
					
				}

			} else {
				
				this.error("Expected single- or double-quoted  URL or file name.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + KeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseJoinCondition() throws SQLException {
		
		if(this.beginParantheses()) {

			this.consume();

			if(!this.parseExpression(false)) {

				this.error("Expected expression.");

			}
			
			if(this.logicalGate()) {
				
				this.consume();

				this.parseJoinCondition();
				
			}

			if(this.endParantheses()) {

				this.consume();

			} else {

				this.error("Expected closing parantheses.");

			}

		} else {
			
			if(!this.parseExpression()) {

				this.error("Expected expression.");

			}
			
			if(this.logicalGate()) {
				
				this.consume();
				
				this.parseJoinCondition();
				
			}
			
		}
		
	}
	
	private void parseWhereClause() throws SQLException {
		
		if(this.beginParantheses()) {

			this.consume();

			this.parseWhereClauseExpressionAndOptionalLogicalGate();

			if(this.endParantheses()) {

				this.consume();

			} else {

				this.error("Expected closing parantheses.");

			}

		} else {
			
			this.parseWhereClauseExpressionAndOptionalLogicalGate();
			
		}
		
	}

	private void parseWhereClauseExpressionAndOptionalLogicalGate() throws SQLException {
		if(!this.parseExpression()) {

			this.error("Expected expression.");

		}

		if(this.logicalGate()) {

			this.consume();

			this.parseWhereClause();

		} else if(this.between()) {

			this.consume();

			if(!this.parseExpression()) {

				this.error("Expected expression.");

			}

		}
	}
	
	private void parseField() throws SQLException {
		
		/*ParseTreeNode currentExpressionParseTreeNode =
			this.currentParseTreeNode();*/
		
		if(!this.parseExpression()) {
			
			this.error("Expected expression.");
			
		}
		
		if(this.comma()) {
			
			this.consume();
			
			this.parseField();
			
		}
		
	}
	
	private void parseOrderByField() throws SQLException {
		
		if(!this.parseExpression()) {
			
			this.error("Expected expression.");
			
		}
		
		if(this.sortOrder()) {
			
			this.consume();
			
		}
		
		if(this.comma()) {
			
			this.consume();
			
			this.parseOrderByField();
			
		}
		
	}
	
	private boolean parseFunction() throws SQLException {
		
		if(this.cast()) {
			
			this.consume();
			
			if(this.beginParantheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(this.parseExpression(false)) {

					if(this.as()) {

						this.consume();

						if(this.datatype()) {

							this.consume();

						} else {

							this.error("Expected datatype.");

						}

					} else {

						this.error("Expected AS-keyword.");

					}

				} else {

					this.error("Expected expression.");

				}
				
				if(this.endParantheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");

				} else {
					
					this.error("Expected closing parantheses.");
					
				}
				
			} else {
				
				this.error("Expected opening parantheses.");
				
			}
			
			this.endFunction();
			
			return true;
			
		} else if(this.trim()) {
			
			this.consume();
			
			if(this.beginParantheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(!this.parseExpression()) {

					this.error("Expected expression.");

				}
				
				if(this.endParantheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected closing parantheses.");
					
				}
				
			} else {
				
				this.error("Expected opening parantheses.");
				
			}
			
			this.endFunction();
			
			return true;
			
		} else if(this.geocode()) {
			
			this.consume();
			
			if(this.beginParantheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(this.parseExpression()) {
					
					if(this.endParantheses()) {
					
						this.consume("END_FUNCTION_PARANTHESES");
					
					} else {
					
						this.error("Expected closing parantheses.");
					
					}
					
				} else {
					
					this.error("Expected address input.");
					
				}
				
			} else {
				
				this.error("Expected opening parantheses.");
				
			}
			
			this.endFunction();
			
			return true;
			
		} else if(this.set_function_name()) {
			
			this.consume("SET_FUNCTION_NAME");
			
			if(this.beginParantheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");

				this.parseFunctionParameters();
				
				if(this.endParantheses()) {
				
					this.consume("END_FUNCTION_PARANTHESES");
				
				} else {
				
					this.error("Expected closing parantheses.");
				
				}
				
			} else {
				
				this.error("Expected opening parantheses.");
				
			}
			
			this.endFunction();
			
			return true;
			
		}
		
		return false;
		
	}
	
	private void parseFunctionParameters() throws SQLException {
		
		if(this.parseExpression()) {
			
			if(this.comma()) {
				
				this.consume();
				
				this.parseFunctionParameters();
				
			}
			
		}
		
	}

	private boolean parseExpressionAndBuildTree() throws SQLException {
		return this.parseExpressionAndBuildTree(true);
	}
	
	private boolean parseExpressionAndBuildTree(boolean startOfExpression) throws SQLException {
		
		this.currentParseTreeNode().setAddNewChildrentToExpression(true);
		
		this.expressionTreeBuilders.push(
			new ExpressionTreeBuilder()
		);
		
		boolean foundExpression =
			this.parseExpression(startOfExpression);
		
		this.currentParseTreeNode().setAddNewChildrentToExpression(false);
		
		if(foundExpression) {
			
			ExpressionTreeBuilder expressionTreeBuilder =
				this.expressionTreeBuilders.pop();
			
			expressionTreeBuilder.finish();
			
			/*System.out.println(
				expressionTreeBuilder.expressionTreeRoot().toString()
			);*/
			
		}
		
		/*if(foundExpression) {
			
			ParseTreeNode currentExpressionParseTreeNode =
				this.currentParseTreeNode().getFirstChild();
			
			ExpressionTreeBuilder expressionTreeBuilder =
				new ExpressionTreeBuilder();
		
			while(currentExpressionParseTreeNode != null &&
					!currentExpressionParseTreeNode.endOfExpression()) {
			
				expressionTreeBuilder.addToken(
					currentExpressionParseTreeNode
				);
			
				currentExpressionParseTreeNode =
					currentExpressionParseTreeNode.next();
			
			}
		
			expressionTreeBuilder.finish();
		
			System.out.println(
				expressionTreeBuilder.expressionTreeRoot().toString()
			);
			
		}*/
		
		return foundExpression;
		
	}

	private boolean parseExpression() throws SQLException {
		return this.parseExpression(true);
	}

	private boolean parseExpression(boolean startOfExpression) throws SQLException {

		boolean foundExpression = false;

		if(this.beginParantheses()) {

			this.consume();

			if(!this.parseExpression(false)) {

				this.error("Expected expression.");

			}

			if(this.endParantheses()) {

				this.consume();

			} else {

				this.error("Expected closing parantheses.");

			}

			foundExpression = true;

		} else if(this.json_lookup()) {

			this.parseJsonLookup();

			foundExpression = true;

		} else if(this.function() || this.set_function_name()) {

			this.parseFunction();

			foundExpression = true;

		} else if(this.field()) {

			this.consume();

			foundExpression = true;

		} else if(this.string()) {

			this.consume();

			foundExpression = true;

		} else if(this.number()) {

			this.consume();

			foundExpression = true;

		} else if(this.aggregateFunction()) {
		
			this.consume();
			
			if(this.beginParantheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				this.parseExpressionAndBuildTree(false);
				
				if(this.binaryOperator()) {

					this.consume();

					this.parseExpression(false);

				}
				
				if(this.endParantheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
					// Window function specifier
					if(this.over()) {
						
						this.consume();
						
						if(this.beginParantheses()) {
							
							this.consume("BEGIN_FUNCTION_PARANTHESES");
							
							if(this.partitionBy()) {
								
								this.consume();
								
								this.parseField();
								
							}
							
							if(this.orderBy()) {
								
								this.consume();
								
								this.parseOrderByField();
								
							}
							
							if(this.endParantheses()) {
								
								this.consume("END_FUNCTION_PARANTHESES");
								
							} else {
								
								this.error("Expected closing parantheses");
								
							}
							
						} else {
							
							this.error("Expected opening parantheses.");
							
						}
						
						this.endFunction();
						
					}
					
				} else {
					
					this.error("Expected closing parantheses");
					
				}
				
				
			} else {
				
				this.error("Expected opening parantheses.");
				
			}
			
			this.endFunction();
			
			foundExpression = true;
		
		}
		
		if(this.binaryOperator()) {

			this.consume();

			this.parseExpression(false);

		}
		
		return foundExpression;
		
	}
	
	private boolean parseJsonLookup() throws SQLException {
		
		boolean foundMember = false;
		
		if(this.jsonArrayJoin()) { // JSON array join
			
			this.consume();
			
			if(this.beginParantheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(this.literal()) {
					
					this.consume();
					
				} else {
					
					this.error("Expected single- or double-quoted field delimiter for joining JSON array.");
					
				}
				
				if(this.endParantheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected closing parantheses.");
					
				}
				
			} else {
				
				this.error("Expected opening parantheses.");
				
			}
			
			this.endFunction();
			
			foundMember = true;
			
		} else if(this.literal()) {
			
			this.consume();
			
			foundMember = true;
			
		} else if(this.beginParantheses()) {
			
			this.consume();
			
			if(this.number() || this.literal()) {
				
				this.consume();
				
				if(this.endParantheses()) {
					
					this.consume();
					
					foundMember = true;
					
				} else {
					
					this.error("Expected closing parantheses.");
					
				}
				
			} else {
				
				this.error("Expected number.");
				
			}
				
		}
		
		if(this.json_member()) {
			
			this.consume();
			
			if(this.json_member() || !this.parseJsonLookup()) {
				
				this.error("Expected literal or parantheses-enclosed number.");
				
			}
			
		}
		
		return foundMember;
		
	}

	private void parseToFile() throws SQLException {
		if(this.toFile()) {
			this.consume();
			if(this.string()) {
				this.consume();
			} else {
				this.error("Expected filename as string.");
			}
		}
	}
	
	private boolean select() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.SELECT);
	}
	
	private boolean with() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.WITH);
	}
	
	private boolean begin_query() {
		return (!this.end_of_statement() &&
			this.nextToken().keyWord() == KeyWord.SELECT ||
			this.nextToken().keyWord() == KeyWord.WITH
		);
	}
	
	private boolean top() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.TOP);
	}
	
	private boolean distinct() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.DISTINCT);
	}
	
	private boolean multiply() { // Used to check for select all, i.e. select * from
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.MULTIPLY);
	}

	private boolean star() { // Used to check for select all, i.e. select * from
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.MULTIPLY);
	}
	
	private boolean from() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.FROM);
	}
	
	private boolean comma() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.COMMA);
	}
	
	private boolean beginParantheses() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.BEGIN_PARANTHESES);
	}
	
	private boolean endParantheses() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.END_PARANTHESES);
	}
	
	private boolean as() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.AS);
	}
	
	private boolean join() {
		return (!this.end_of_statement() && this.nextToken().keyWord().keyWordType() == KeyWordType.JOIN);
	}
	
	private boolean jsonArrayJoin() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.JOIN);
	}
	
	private boolean setOperation() {
		return (!this.end_of_statement() && this.nextToken().keyWord().keyWordType() == KeyWordType.SET_OPERATION);
	}
	
	private boolean on() {
		return (this.nextToken().keyWord() == KeyWord.ON);
	}
	
	private boolean q() { // Cross-system Query
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.Q);
	}
	
	private boolean nq() { // Named Query
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.NQ);
	}
	
	private boolean csv() { // CSV file
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.CSV);
	}
	
	private boolean xl() { // Excel file
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.XL);
	}
	
	private boolean json() { // JSON file
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.JSON);
	}
	
	private boolean geocode() { // Geocode function
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.GEOCODE);
	}
	
	private boolean cast() {
		return (this.nextToken().keyWord() == KeyWord.CAST);
	}
	
	private boolean trim() {
		return (this.nextToken().keyWord() == KeyWord.TRIM);
	}
	
	private boolean set_function_name() {
		return (
			!this.end_of_statement() &&
				this.nextToken().keyWord() == KeyWord.LITERAL &&
				this.nextToken().nextToken() != null &&
				this.nextToken().nextToken().keyWord() == KeyWord.BEGIN_PARANTHESES
		);
	}
	
	private boolean datatype() {
		return (this.nextToken().keyWord().keyWordType() == KeyWordType.DATATYPE);
	}
	
	private boolean literal() {
		return (
			!this.end_of_statement() &&
				(this.nextToken().keyWord() == KeyWord.LITERAL ||
				this.nextToken().keyWord() == KeyWord.SINGLE_QUOTED_LITERAL ||
				this.nextToken().keyWord() == KeyWord.DOUBLE_QUOTED_LITERAL)
		);
	}

	private boolean string() {
		return (
			!this.end_of_statement() &&
			this.nextToken().keyWord() == KeyWord.SINGLE_QUOTED_LITERAL
		);
	}
	
	private boolean logicalGate() {
		return (
			!this.end_of_statement() &&
			(this.nextToken().keyWord() == KeyWord.AND ||
			this.nextToken().keyWord() == KeyWord.OR)
		);
	}

	private boolean and() {
		return this.nextToken().keyWord() == KeyWord.AND;
	}

	private boolean between() {
		return this.nextToken().keyWord() == KeyWord.BETWEEN;
	}
	
	private boolean field() {
		return (
			this.nextToken().keyWord() == KeyWord.LITERAL ||
			this.nextToken().keyWord() == KeyWord.DOUBLE_QUOTED_LITERAL
		);
	}
	
	private boolean json_lookup() {
		return (
			!this.end_of_statement() &&
			(this.nextToken().keyWord() == KeyWord.LITERAL ||
			this.nextToken().keyWord() == KeyWord.DOUBLE_QUOTED_LITERAL ||
			this.nextToken().keyWord() == KeyWord.SINGLE_QUOTED_LITERAL) &&
			this.nextToken().nextToken() != null &&
				this.nextToken().nextToken().keyWord() == KeyWord.JSON_MEMBER
		);
	}
	
	private boolean json_member() {
		return this.nextToken().keyWord() == KeyWord.JSON_MEMBER;
	}
	
	private boolean number() {
		return (
			this.nextToken().keyWord() == KeyWord.NUMBER_ATOM
		);
	}
	
	private boolean json_data_structure() {
		return (
			this.nextToken().keyWord() == KeyWord.JSON_DATA_STRUCTURE
		);
	}
	
	private boolean aggregateFunction() {
		return (this.nextToken().keyWord().keyWordType() == KeyWordType.AGGREGATE_OPERATOR);
	}
	
	private boolean function() {
		return (this.nextToken().keyWord().keyWordType() == KeyWordType.FUNCTION);
	}
	
	private boolean binaryOperator() {
		return (!this.end_of_statement() && this.nextToken().keyWord().keyWordType() == KeyWordType.BINARY_OPERATOR);
	}
	
	private boolean over() {
		return (this.nextToken().keyWord() == KeyWord.OVER);
	}
	
	private boolean partitionBy() {
		return (this.nextToken().keyWord() == KeyWord.PARTITION_BY);
	}
	
	private boolean where() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.WHERE);
	}
	
	private boolean groupBy() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.GROUP_BY);
	}
	
	private boolean orderBy() {
		return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.ORDER_BY);
	}
	
	private boolean sortOrder() {
		return (!this.end_of_statement() && this.nextToken().keyWord().keyWordType()  == KeyWordType.SORT_ORDER);
	}

	public boolean toFile() { return (!this.end_of_statement() && this.nextToken().keyWord() == KeyWord.TOFILE); }
	
	private boolean end_of_statement() {
		return this.nextToken() == null;
	}
	
	private void error(String error) throws SQLException {
		throw new SQLException(error + "\n" + (this.nextToken() != null ? this.nextToken().tokenInSQL() : "Got EOF"));
	}
	
	/*
	** Print methods
	*/
	
	private void print(ParseTreeNode parseTreeNode, int depth) {
		
		if(parseTreeNode == null) {
			return;
		}
		
		for(int i=0; i<=depth; i++) {
			System.out.print("-");
		}
		
		System.out.println(
			" " + parseTreeNode.toString()
		);
		
		for(ParseTreeNode childParseTreeNode : parseTreeNode.getChildren()) {
			
			this.print(
				childParseTreeNode,
				depth + 1
			);
			
		}
		
	}
	
	public void print() {
		this.print(this.currentParseTreeRootNode, 0);
	}
	
	/*
	** Helper methods
	*/
	private void getSQLParseTreeNodesByKeyWord(
		ParseTreeNode parseTreeNode,
		Vector<ParseTreeNode> nodeList,
		KeyWord keyWord
	) {
		
		if(parseTreeNode == null) {
			return;
		}
		
		for(ParseTreeNode childParseTreeNode : parseTreeNode.getChildren()) {
			
			if(childParseTreeNode.keyWord() == keyWord) {
				
				nodeList.add(
					childParseTreeNode
				);
				
			}
			
			this.getSQLParseTreeNodesByKeyWord(
				childParseTreeNode,
				nodeList,
				keyWord
			);
			
		}
		
	}
	
	public ParseTreeNode[] getSQLParseTreeNodesByKeyWord(KeyWord keyWord) {
		
		Vector<ParseTreeNode> nodeList =
			new Vector<ParseTreeNode>();
		
		this.getSQLParseTreeNodesByKeyWord(
			this.currentParseTreeRootNode,
			nodeList,
			keyWord
		);
		
		return nodeList.toArray(new ParseTreeNode[]{});
		
	}
	
	public ParseTreeNode getRootNode() {
		
		return this.currentParseTreeRootNode;
		
	}
	
}