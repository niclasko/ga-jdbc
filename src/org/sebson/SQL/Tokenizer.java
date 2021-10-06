package org.sebson.SQL;

import java.util.LinkedList;
import java.sql.SQLException;

public class Tokenizer {
	
	private String sql;
	private LinkedList<Token> tokens;
	
	public Tokenizer(String sql) throws SQLException {
		
		this.sql = sql;
		
		this.tokens =
			new LinkedList<Token>();
			
		this.tokenize(sql);
		
	}
	
	private Quotation sqlQuotation(boolean doubleQuoted, boolean singleQuoted) {
		
		if(doubleQuoted && !singleQuoted) {
			return Quotation.DOUBLE;
		} else if(singleQuoted && !doubleQuoted) {
			return Quotation.SINGLE;
		} else if(doubleQuoted && singleQuoted) {
			return Quotation.NONE;
		} else if(!doubleQuoted && !singleQuoted) {
			return Quotation.NONE;
		}
		
		return Quotation.NONE;
	}
	
	private Token getLastToken() {
		
		if(this.tokens.size() == 0) {
			return null;
		}
		
		return this.tokens.getLast();
	}

	private boolean isOperand(Token token) {
		return token.keyWord().keyWordType() == KeyWordType.ATOM;
	}
	
	private boolean mergeWithPrevious(String tokenText) {
		
		if(this.tokens.size() == 0) {
			return false;
		}
		
		Token lastToken =
			this.getLastToken();
			
		if(lastToken == null) {
			return false;
		}
		
		if(lastToken.quoted()) {
			return false;
		}
		
		String tokenTextUpperCase =
			tokenText.toUpperCase();
			
		boolean appendTokenText = false;
		
		if(tokenTextUpperCase.equals("OUTER")) {
			
			if(lastToken.tokenTextUpperCase().equals("FULL")) {
				
				appendTokenText = true;
				
			}
			
		} else if(tokenTextUpperCase.equals("JOIN")) {
			
			if(	lastToken.tokenTextUpperCase().equals("INNER") ||
				lastToken.tokenTextUpperCase().equals("LEFT") ||
				lastToken.tokenTextUpperCase().equals("RIGHT") ||
				lastToken.tokenTextUpperCase().equals("FULL OUTER")	) {
					
				appendTokenText = true;
				
			}
			
		} else if(tokenTextUpperCase.equals("BY")) {
			
			if(	lastToken.tokenTextUpperCase().equals("GROUP") ||
				lastToken.tokenTextUpperCase().equals("ORDER") ||
				lastToken.tokenTextUpperCase().equals("PARTITION")	) {
					
				appendTokenText = true;
					
			}
			
		} else if(tokenTextUpperCase.equals("ALL")) {
			
			if(lastToken.tokenTextUpperCase().equals("UNION")) {
					
				appendTokenText = true;
					
			}
			
		} else if(Token.isNumeric(tokenText)) { // Check for negated number

			if(lastToken.tokenText().equals("-") && lastToken.previousToken() != null && !this.isOperand(lastToken.previousToken())) {

				lastToken.append(tokenText);

				return true;
			}

		}
		
		if(appendTokenText) {
			
			lastToken.append(" " + tokenText);
			
			return true;
			
		}
		
		return false;
		
	}
	
	private void addToken(String tokenText, boolean doubleQuoted, boolean singleQuoted, int position, int lineNumber) {
		
		if(!doubleQuoted && !singleQuoted) {
			if(mergeWithPrevious(tokenText)) {
				return;
			}
		}
		
		int modifiedPosition = position;
		
		if(doubleQuoted || singleQuoted) {
			modifiedPosition =
				position - tokenText.length() - 1;
		} else if(tokenText.length() == 1) {
			modifiedPosition = position;
		} else {
			modifiedPosition =
				position - tokenText.length();
		}
		
		this.tokens.add(
			new Token(
				this,
				tokenText,
				sqlQuotation(
					doubleQuoted,
					singleQuoted
				),
				this.getLastToken(),
				modifiedPosition,
				lineNumber
			)
		);
	}
	
	private void tokenize(String sql) throws SQLException {
		
		String token = "";
		char c = '\0', pc, nc;
		boolean doubleQuoted = false, 
				singleQuoted = false,
				backTickQuoted = false,
				escape = false;
		int lineNumber = 0;
		
		for(int i=0; i<sql.length(); i++) {
			
			pc = c;
			c = sql.charAt(i);
			
			if(c == '\\') {
				continue;
			}
			
			if(pc == '\\' && (c != 'n' || c == 't')) {
				escape = true;
			} else {
				escape = false;
			}

			if(pc == '\\' && c == 'n') {
				c = '\n';
			}

			if(pc == '\\' && c == 't') {
				c = '\t';
			}
			
			if(i<sql.length()-1) {
				nc = sql.charAt(i+1);
			} else {
				nc = '\0';
			}
			
			if(c == '\'' && singleQuoted && !escape) { // End single quote
				this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				token = "";
				singleQuoted = false;
			} else if(c == '\'' && !singleQuoted && !escape && !doubleQuoted && !backTickQuoted) { // Begin single quote
				singleQuoted = true;
			} else if(c != '\'' && singleQuoted && !escape) { // Accumulate text within single quote
				token += c;
			} else if(c == '"' && doubleQuoted && !escape) { // End double quote
				this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				token = "";
				doubleQuoted = false;
			} else if(c == '"' && !doubleQuoted && !escape && !singleQuoted && !backTickQuoted) { // Begin double quote
				doubleQuoted = true;
			} else if(c != '"' && doubleQuoted && !escape) {// Accumulate text within double quote
				token += c;
			} else if(c == '`' && !backTickQuoted && !singleQuoted && !doubleQuoted) {
				backTickQuoted = true;
				token += c;
			}  else if(c != '`' && backTickQuoted && !escape) {// Accumulate text within back-tick quote
				token += c;
			} else if(c == '`' && backTickQuoted && !singleQuoted && !doubleQuoted) {
				backTickQuoted = false;
				token += c;
				this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				token = "";
			} else if(c == '=' && nc == '=') {
				if(!token.equals("")) {
					this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				}
				this.addToken("==", false, false, i, lineNumber);
				token = "";
				i++;
			} else if(c == '<' && nc == '=') {
				if(!token.equals("")) {
					this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				}
				this.addToken("<=", false, false, i, lineNumber);
				token = "";
				i++;
			} else if(c == '>' && nc == '=') {
				if(!token.equals("")) {
					this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				}
				this.addToken(">=", false, false, i, lineNumber);
				token = "";
				i++;
			} else if(c == '!' && nc == '=') {
				if(!token.equals("")) {
					this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				}
				this.addToken("!=", false, false, i, lineNumber);
				token = "";
				i++;
			} else if(c == '-' && nc == '>') { // JSON object/array member
				if(!token.equals("")) {
					this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				}
				this.addToken("->", false, false, i, lineNumber);
				token = "";
				i++;
			} else if((c == ',' ||
				c == '(' ||
				c == ')' ||
				c == '-' ||
				c == '/' ||
				c == '*' ||
				c == '=' ||
				c == '+' ||
				c == '^' ||
				c == '>' ||
				c == '<') &&
				!(c == '-' && // Make sure we support negative values
					(pc == '-' ||
					pc == '+' ||
					pc == '/' ||
					pc == '*' ||
					pc == '^' ||
					pc == '\0'
					)) &&
					!(c == '*' && pc == '.')) {
				if(!token.equals("")) {
					this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				}
				this.addToken(Character.toString(c), false, false, i, lineNumber);
				token = "";
			// Blank or TAB
			} else if (c == ' ' || c == '\t') {
				
				if(!token.equals("")) {
					this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				}
				token = "";
				
			// CRLF or new line
			} else if((c == '\r' && nc == '\n') || (pc != '\r' && c == '\n') || (pc == '\\' && c == 'n')) {
				
				if(!token.equals("")) {
					this.addToken(token, doubleQuoted, singleQuoted, i, lineNumber);
				}
				token = "";
				
				lineNumber++;
				
			} else {
				token += c;
			}
		}
		if(!token.equals("")) {
			this.addToken(token, doubleQuoted, singleQuoted, sql.length(), lineNumber);
		}
	}
	
	public String sql() {
		return this.sql;
	}
	
	public Token[] tokens() {
		
		Token[] tokens =
			this.tokens.toArray(
				new Token[this.tokens.size()]
			);
		
		return tokens;
	}
	
	public void print() {
		for(Token token : this.tokens) {
			System.out.println(token.toString() + " (" + token.keyWord() + "). Position: " + token.position());
		}
	}
	
	public static void main(String[] args) {
		
		String sql =
			"select\n" +
			"	name,\n" +
			"	1+1+1*2,\n" +
			"	1/4+1*2,\n" +
			"	sum(value/(23+3)^((2))) over (partition by name)*10^2 sum_value,\n" +
			"	'hello' as text\n" +
			"from\n" +
			"	q('select \"customer_id\" from test', 'ERP') t1\n" +
			"	inner join\n" +
			"	table2 t2\n" +
			"	on (t1.customer_id = t2.id)\n" +
			"group by\n" +
			"	name\n" +
			"order by\n" +
			"	sum_value asc\n" +
			"union " +
			"select\n" +
			"	name,\n" +
			"	1+1+1*2,\n" +
			"	1/4+1*2,\n" +
			"	sum(value/(23+3)^((2))) over (partition by name)*10^2 sum_value,\n" +
			"	'hello' as text\n" +
			"from\n" +
			"	q('select \"customer_id\" from test', 'ERP') t1\n" +
			"	inner join\n" +
			"	table2 t2\n" +
			"	on (t1.customer_id = t2.id)\n" +
			"group by\n" +
			"	name\n" +
			"order by\n" +
			"	sum_value asc";
			
		String sql2 =
			"select * from table";
			
		String sql3 =
			"select\n" +
			"	sq.ID, sq.R\n" +
			"from\n" +
			"	q('select * from (select \\'HANA\\' id, round(rand(),1) r from dummy)', 'UOVDEV') sq\n" +
			"union all\n" +
			"select\n" +
			"	sq.ID, sq.R\n" +
			"from\n" +
			"	q('select \\'ORACLE\\' id, round(dbms_random.value(),1) r from dual', 'ERP_STAGE') sq\n" +
			"union all\n" +
			"select\n" +
			"	sq.ID, sq.R\n" +
			"from\n" +
			"	csv('http://fuse.cisco.com:4444/apps/datacafe/files/test.csv', ',') sq";
		
		String sql4 =
			"select 1 as ID, 'Niclas' as NAME, 'Test' as DESCRIPTION " +
			"UNION ALL select 1 from csv('test', ',') as sq " +
			"UNION ALL select 1 from nq(\"Supply Chain Test\") as sq";
		
		String sql5 =
			"21+3*(4+5)/6*7 OR 1 AND 2^4";
		
		String sql6 =
			"select a->join(',') from json('url', {\"Authorization\": \"Basic\"})";
		
		String sql7 =
			"select seq(1,2,3,4) from dual";
		
		String sql11 =
			"select " +
				"sq.json_array, " +
				"geocode('oslo') as gc, " +
				"sq.json_array_length " +
			"from " +
				"(select " +
					"sq.`1`->`getBom.getBOMResult`->`getBom.row`->(t.idx) as json_array, " +
					"sq.`1`->`getBom.getBOMResult`->`getBom.row`->length as json_array_length " +
				"from " +
					"json( " +
						"'http://dv-tst-02:9400/json/DV_POC_ENV16/getBom/getBOM?item_id=73-13652-02', " +
						"{\"Authorization\": \"Basic ZHZfcG9jX2VudjE2QGNvbXBvc2l0ZTpkdl9wb2NfZW52MTY=\"} " +
					") sq) sq " +
				"inner join " +
				"(select index from iterator(0,sq.json_array_length,1) i) t " +
				"on (1=1)";
						
		String sql12 =
			"with a as (select * from xl('/Users/nkjalloh/Dropbox/Programming/Java/DB/testdata/companies.xlsx')), b as (select * from xl('/Users/nkjalloh/Dropbox/Programming/Java/DB/testdata/companies_extra_info.xlsx')) select * from a inner join b (a.ID=b.ID)";

		String sql13 =
			"select replace(sq.FEATURE_REQ, '\\n', ' ') fr from xl('/Users/nkjalloh/Box Sync/Work/CVC IT/projects/Development Supply Chain/machine_learning/product_requirements/product_requirements.xls') as sq toFile '/test'";

		try {
			
			Tokenizer st =
				new Tokenizer(sql13);
		
			st.print();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}