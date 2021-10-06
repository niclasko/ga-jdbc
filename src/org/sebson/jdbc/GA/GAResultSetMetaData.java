package org.sebson.jdbc.GA;

import java.sql.ResultSetMetaData;
import java.sql.Types;

public class GAResultSetMetaData implements ResultSetMetaData {
	
	private int columnCount;
	private String[] columnNames;
	
	public GAResultSetMetaData(String[] columnNames) {
		this.columnCount = columnNames.length;
		this.columnNames = columnNames;
	}
	
	public String getCatalogName(int column) {
		return null;
	}
	public String getColumnClassName(int column) {
		return "java.lang.String";
	}
	public int getColumnCount() {
		return this.columnCount;
	}
	public int getColumnDisplaySize(int column) {
		return -1;
	}
	public String getColumnLabel(int column) {
		return null;
	}
	public String getColumnName(int column) {
		return this.columnNames[column-1];
	}
	public int getColumnType(int column) {
		return Types.VARCHAR;
	}
	public String getColumnTypeName(int column) {
		return "VARCHAR";
	}
	public int getPrecision(int column) {
		return -1;
	}
	public int getScale(int column) {
		return -1;
	}
	public String getSchemaName(int column) {
		return null;
	}
	public String getTableName(int column) {
		return null;
	}
	public boolean isAutoIncrement(int column) {
		return false;
	}
	public boolean isCaseSensitive(int column) {
		return false;
	}
	public boolean isCurrency(int column) {
		return false;
	}
	public boolean isDefinitelyWritable(int column) {
		return false;
	}
	public int isNullable(int column) {
		return -1;
	}
	public boolean isReadOnly(int column) {
		return true;
	}
	public boolean isSearchable(int column) {
		return false;
	}
	public boolean isSigned(int column) {
		return false;
	}
	public boolean isWrapperFor(Class<?> iface) {
		return true;
	}
	public boolean isWritable(int column) {
		return false;
	}
	public <T> T unwrap(Class<T> iface) {
		return null;
	}
}