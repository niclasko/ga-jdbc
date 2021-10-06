package org.sebson.jdbc.GA;

import java.sql.Connection;
import java.sql.Struct;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.DatabaseMetaData;
import java.sql.SQLWarning;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.util.concurrent.Executor;
import java.util.Properties;
import java.util.Map;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.auth.http.HttpCredentialsAdapter;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.Columns;
import com.google.api.services.analytics.model.Column;

import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;

public class GAConnection implements Connection {

	private final String APPLICATION_NAME = "Google Analytics JDBC Driver";
	private final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	private AnalyticsReporting gaReportingService;
	private Analytics gaMetadataService;

	private HashMap<String, Column> gaColumns;

	public GAConnection(Properties info) {
		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			GoogleCredentials credentials = GoogleCredentials
					.fromStream(new FileInputStream(info.getProperty("KEY_FILE_LOCATION")))
					.createScoped(AnalyticsReportingScopes.all());

			HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

			// Construct the Analytics Reporting service object.
			this.gaReportingService = new AnalyticsReporting.Builder(
					httpTransport, JSON_FACTORY, requestInitializer)
					.setApplicationName(APPLICATION_NAME).build();

			this.gaMetadataService = new Analytics.Builder(
					httpTransport, JSON_FACTORY, requestInitializer)
					.setApplicationName(APPLICATION_NAME).build();

			this.addAllGAColumns(this.getMetadata().getItems());

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private Columns getMetadata() throws IOException {
		String reportType = "ga";
		return this.gaMetadataService.metadata()
				.columns()
				.list(reportType)
				.execute();
	}

	private void addAllGAColumns(List<Column> columns) {
		this.gaColumns = new HashMap<String, Column>();
		for (Column column : columns) {
			this.gaColumns.put(column.getId(), column);

			/*System.out.println();
			System.out.println("Column ID: " + column.getId());
			System.out.println("Kind: " + column.getKind());

			Map<String, String> columnAttributes = column.getAttributes();

			for (Map.Entry<String, String> attribute: columnAttributes.entrySet()) {
				System.out.println(attribute.getKey() + ": " + attribute.getValue());
			}*/

		}
	}

	public Column getGAColumn(String columnId) throws Exception {
		Column gaColumn = this.gaColumns.get(columnId);
		if(gaColumn == null) {
			throw new Exception("Column " + columnId + " is not available in Google Analytics.");
		}
		return gaColumn;
	}

	public AnalyticsReporting getGAReportingService()  {
		return gaReportingService;
	}
	
	public void abort(Executor executor) {
		;
	}
	public void clearWarnings() {
		;
	}
	public void close() {
		;
	}
	public void commit() {
		;
	}
	public Array createArrayOf(String typeName, Object[] elements) {
		return null;
	}
	public Blob createBlob() {
		return null;
	}
	public Clob createClob() {
		return null;
	}
	public NClob createNClob() {
		return null;
	}
	public SQLXML createSQLXML() {
		return null;
	}
	public GAStatement createStatement() {
		return new GAStatement(this);
	}
	public GAStatement createStatement(int resultSetType, int resultSetConcurrency) {
		return new GAStatement(this);
	}
	public GAStatement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		return new GAStatement(this);
	}
	public Struct createStruct(String typeName, Object[] attributes) {
		return null;
	}
	public boolean getAutoCommit() {
		return false;
	}
	public String getCatalog() {
		return null;
	}
	public Properties getClientInfo() {
		return null;
	}
	public String getClientInfo(String name) {
		return null;
	}
	public int getHoldability() {
		return -1;
	}
	public DatabaseMetaData getMetaData() {
		return null;
	}
	public int getNetworkTimeout() {
		return -1;
	}
	public String getSchema() {
		return null;
	}
	public int getTransactionIsolation() {
		return -1;
	}
	public Map<String,Class<?>> getTypeMap() {
		return null;
	}
	public SQLWarning getWarnings() {
		return null;
	}
	public boolean isClosed() {
		return true;
	}
	public boolean isReadOnly() {
		return true;
	}
	public boolean isValid(int timeout) {
		return true;
	}
	public boolean isWrapperFor(Class<?> iface) {
		return true;
	}
	public String nativeSQL(String sql) {
		return null;
	}
	public CallableStatement prepareCall(String sql) {
		return null;
	}
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) {
		return null;
	}
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		return null;
	}
	public PreparedStatement prepareStatement(String sql) {
		return null;
	}
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) {
		return null;
	}
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) {
		return null;
	}
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) {
		return null;
	}
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		return null;
	}
	public PreparedStatement prepareStatement(String sql, String[] columnNames) {
		return null;
	}
	public void releaseSavepoint(Savepoint savepoint) {
		;
	}
	public void rollback() {
		;
	}
	public void rollback(Savepoint savepoint) {
		;
	}
	public void setAutoCommit(boolean autoCommit) {
		;
	}
	public void setCatalog(String catalog) {
		;
	}
	public void setClientInfo(Properties properties) {
		;
	}
	public void setClientInfo(String name, String value) {
		;
	}
	public void setHoldability(int holdability) {
		;
	}
	public void setNetworkTimeout(Executor executor, int milliseconds) {
		;
	}
	public void setReadOnly(boolean readOnly) {
		;
	}
	public Savepoint setSavepoint() {
		return null;
	}
	public Savepoint setSavepoint(String name) {
		return null;
	}
	public void setSchema(String schema) {
		;
	}
	public void setTransactionIsolation(int level) {
		;
	}
	public void setTypeMap(Map<String,Class<?>> map) {
		;
	}
	public <T> T unwrap(Class<T> iface) {
		return null;
	}
	
	public static void main(String args[]) {
		System.out.println("Hello");
	}
}