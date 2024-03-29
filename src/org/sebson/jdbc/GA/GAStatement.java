package org.sebson.jdbc.GA;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLWarning;
import java.sql.SQLException;

public class GAStatement implements Statement {
	
	private GAResultSet GAResultSet;
	private GAConnection gaConnection;
	
	public GAStatement(GAConnection gaConnection) {
		this.GAResultSet = null;
		this.gaConnection = gaConnection;
	}
	
	public void addBatch(String sql) {
		;
	}
	public void cancel() {
		;
	}
	public void clearBatch() {
		;
	}
	public void clearWarnings() {
		;
	}
	public void close() {
		;
	}
	public void closeOnCompletion() {
		;
	}

	public boolean execute(String query) throws SQLException {
		this.GAResultSet =
			new GAResultSet(
				this.gaConnection,
				query
			);
		
		return true;
	}
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		return this.execute(sql);
	}
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		return this.execute(sql);
	}
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		return this.execute(sql);
	}
	public int[] executeBatch() {
		return null;
	}
	public GAResultSet executeQuery(String sql) throws SQLException {
		this.execute(sql);
		return this.GAResultSet;
	}
	public int executeUpdate(String sql) {
		return -1;
	}
	public int executeUpdate(String sql, int autoGeneratedKeys) {
		return -1;
	}
	public int executeUpdate(String sql, int[] columnIndexes) {
		return -1;
	}
	public int executeUpdate(String sql, String[] columnNames) {
		return -1;
	}
	public Connection getConnection() {
		return null;
	}
	public int getFetchDirection() {
		return -1;
	}
	public int getFetchSize() {
		return -1;
	}
	public ResultSet getGeneratedKeys() {
		return null;
	}
	public int getMaxFieldSize() {
		return -1;
	}
	public int getMaxRows() {
		return -1;
	}
	public boolean getMoreResults() {
		return false;
	}
	public boolean getMoreResults(int current) {
		return false;
	}
	public int getQueryTimeout() {
		return -1;
	}
	public ResultSet getResultSet() {
		return null;
	}
	public int getResultSetConcurrency() {
		return -1;
	}
	public int getResultSetHoldability() {
		return -1;
	}
	public int getResultSetType() {
		return -1;
	}
	public int getUpdateCount() {
		return -1;
	}
	public SQLWarning getWarnings() {
		return null;
	}
	public boolean isClosed() {
		return false;
	}
	public boolean isCloseOnCompletion() {
		return false;
	}
	public boolean isPoolable() {
		return false;
	}
	public boolean isWrapperFor(Class<?> iface) {
		return true;
	}
	public void setCursorName(String name) {
		;
	}
	public void setEscapeProcessing(boolean enable) {
		;
	}
	public void setFetchDirection(int direction) {
		;
	}
	public void setFetchSize(int rows) {
		;
	}
	public void setMaxFieldSize(int max) {
		;
	}
	public void setMaxRows(int max) {
		;
	}
	public void setPoolable(boolean poolable) {
		;
	}
	public void setQueryTimeout(int seconds) {
		;
	}
	public <T> T unwrap(Class<T> iface) {
		return null;
	}
	
	public static void main(String args[]) {
		System.out.println("Hello");
	}
}