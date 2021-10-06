package org.sebson.jdbc.GA;

import java.sql.ResultSet;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;
import java.net.URL;

import java.io.File;
import java.io.FileInputStream;

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
import java.util.Iterator;

import com.google.api.services.analytics.model.Column;

import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;

import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

import org.sebson.SQL.Parser;
import org.sebson.SQL.ParseTreeNode;
import org.sebson.SQL.KeyWord;

import java.sql.SQLException;

public class GAResultSet implements ResultSet {

	private int rowIndex = 0;
	private int rowCount = 0;
	private int columnCount = 0;

	private GAConnection gaConnection;
	private AnalyticsReporting gaService;

	private GAResultSetMetaData gaResultSetMetaData;

	private ArrayList<GAReportColumn> gaReportColumns;
	private String ga_view_id;
	private String dateFrom = "7DaysAgo";
	private String dateTo = "today";

	private ReportRequest gaReportRequest;
	private GetReportsRequest gaGetReportsRequest;
	private Report gaReport;
	private List<ReportRow> gaReportRows;
	private Iterator<ReportRow> gaReportRowsIterator;
	
	public GAResultSet(GAConnection gaConnection, String query) throws SQLException {
		this.gaConnection = gaConnection;
		this.gaService = gaConnection.getGAReportingService();
		this.parseAndProcessQuery(query);
		this.fetchGAReport();
	}

	private void parseAndProcessQuery(String query) throws SQLException {
		Parser sqlParser = new Parser(query);

		this.addGAReportColumnsFromQuery(
			sqlParser.getRootNode()
				.getChild(KeyWord.SELECT).getChildTokens()
		);

		this.ga_view_id =
			sqlParser.getRootNode().getChild(KeyWord.FROM)
				.getFirstChild().token().tokenText();

		ParseTreeNode where =
			sqlParser.getRootNode().getChild(KeyWord.WHERE);

		if(where != null) {
			String[] whereTokens =
				where.getChildTokens().toArray(new String[0]);
			this.dateFrom = whereTokens[2];
			this.dateTo = whereTokens[4];
		}

	}

	private void addGAReportColumnsFromQuery(ArrayList<String> selectList) throws SQLException {
		try {
			this.gaReportColumns = new ArrayList<GAReportColumn>();
			for(String columnId : selectList) {
				this.gaReportColumns.add(
					new GAReportColumn(this.gaConnection.getGAColumn(columnId))
				);
			}
			this.gaResultSetMetaData = new GAResultSetMetaData(
				selectList.toArray(new String[0])
			);
			this.columnCount = selectList.size();
		} catch(Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	private void fetchGAReport() throws SQLException {
		try {
			// Create the DateRange object.
			DateRange dateRange = new DateRange();
			dateRange.setStartDate(this.dateFrom);
			dateRange.setEndDate(this.dateTo);

			ArrayList metrics = new ArrayList<Metric>();
			ArrayList dimensions = new ArrayList<Dimension>();

			for(GAReportColumn gaReportColumn : this.gaReportColumns) {
				switch(gaReportColumn.getType()) {
					case "DIMENSION":
						gaReportColumn.setValueIndex(dimensions.size());
						dimensions.add(
							new Dimension().setName(gaReportColumn.getColumnId())
						);
						break;
					case "METRIC":
						gaReportColumn.setValueIndex(metrics.size());
						metrics.add(
							new Metric().setExpression(gaReportColumn.getColumnId())
						);
						break;
					default:
						break;
				}
			}

			// Create the ReportRequest object.
			this.gaReportRequest = new ReportRequest();

			this.gaReportRequest
				.setViewId(this.ga_view_id)
				.setDateRanges(Arrays.asList(dateRange))
				.setPageSize(100000);

			if(metrics.size() > 0) {
				this.gaReportRequest.setMetrics(metrics);
			}

			if(dimensions.size() > 0) {
				this.gaReportRequest.setDimensions(dimensions);
			}

			ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
			requests.add(this.gaReportRequest);

			// Create the GetReportsRequest object.
			this.gaGetReportsRequest = new GetReportsRequest()
					.setReportRequests(requests);

			// Call the batchGet method.
			GetReportsResponse response =
				this.gaService.reports().batchGet(this.gaGetReportsRequest).execute();

			this.gaReport = response.getReports().get(0);
			this.gaReportRows = this.gaReport.getData().getRows();

			this.gaReportRowsIterator = this.gaReportRows.iterator();
			this.rowCount = this.gaReport.getData().getRowCount();
		} catch(Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	private void setGAReportColumnValues(List<String> dimensions, List<DateRangeValues> metrics) {
		for(GAReportColumn gaReportColumn : this.gaReportColumns) {
			switch(gaReportColumn.getType()) {
				case "DIMENSION":
					gaReportColumn.setValue(
						dimensions.get(gaReportColumn.getValueIndex())
					);
					break;
				case "METRIC":
					gaReportColumn.setValue(
						metrics.get(0).getValues().get(gaReportColumn.getValueIndex())
					);
					break;
				default:
					break;
			}
		}
	}

	public int getRowCount() {
		return this.rowCount;
	}

	public int getRowIndex() {
		return this.rowIndex;
	}

	private InputStream getFileInputStream(String filePath) throws Exception {
		return null;
	}
	
	private String getRowCellValue(int columnIndex) {

		if(columnIndex >= this.columnCount || columnIndex < 0) {
			return null;
		}

		return this.gaReportColumns.get(columnIndex).getValue();

	}
	
	public boolean absolute(int row) {
		return false;
	}
	public void afterLast() {
		;
	}
	public void beforeFirst() {
		this.rowIndex = 0;
	}
	public void cancelRowUpdates() {
		;
	}
	public void clearWarnings() {
		;
	}
	public void close() {
		;
	}
	public void deleteRow() {
		;
	}
	public int findColumn(String columnLabel) {
		return -1;
	}
	public boolean first() {
		this.rowIndex = 1;
		return true;
	}
	public Array getArray(int columnIndex) {
		return null;
	}
	public Array getArray(String columnLabel) {
		return null;
	}
	public InputStream getAsciiStream(int columnIndex) {
		return null;
	}
	public InputStream getAsciiStream(String columnLabel) {
		return null;
	}
	public BigDecimal getBigDecimal(int columnIndex) {
		return null;
	}
	public BigDecimal getBigDecimal(int columnIndex, int scale) {
		return null;
	}
	public BigDecimal getBigDecimal(String columnLabel) {
		return null;
	}
	public BigDecimal getBigDecimal(String columnLabel, int scale) {
		return null;
	}
	public InputStream getBinaryStream(int columnIndex) {
		return null;
	}
	public InputStream getBinaryStream(String columnLabel) {
		return null;
	}
	public Blob getBlob(int columnIndex) {
		return null;
	}
	public Blob getBlob(String columnLabel) {
		return null;
	}
	public boolean getBoolean(int columnIndex) {
		return false;
	}
	public boolean getBoolean(String columnLabel) {
		return false;
	}
	public byte getByte(int columnIndex) {
		return 0xf;
	}
	public byte getByte(String columnLabel) {
		return 0xf;
	}
	public byte[] getBytes(int columnIndex) {
		return null;
	}
	public byte[] getBytes(String columnLabel) {
		return null;
	}
	public Reader getCharacterStream(int columnIndex) {
		return null;
	}
	public Reader getCharacterStream(String columnLabel) {
		return null;
	}
	public Clob getClob(int columnIndex) {
		return null;
	}
	public Clob getClob(String columnLabel) {
		return null;
	}
	public int getConcurrency() {
		return -1;
	}
	public String getCursorName() {
		return null;
	}
	public Date getDate(int columnIndex) {
		return null;
	}
	public Date getDate(int columnIndex, Calendar cal) {
		return null;
	}
	public Date getDate(String columnLabel) {
		return null;
	}
	public Date getDate(String columnLabel, Calendar cal) {
		return null;
	}
	public double getDouble(int columnIndex) {
		return 0.0;
	}
	public double getDouble(String columnLabel) {
		return 0.0;
	}
	public int getFetchDirection() {
		return -1;
	}
	public int getFetchSize() {
		return -1;
	}
	public float getFloat(int columnIndex) {
		return 0;
	}
	public float getFloat(String columnLabel) {
		return 0;
	}
	public int getHoldability() {
		return -1;
	}
	public int getInt(int columnIndex) {
		return -1;
	}
	public int getInt(String columnLabel) {
		return -1;
	}
	public long getLong(int columnIndex) {
		return -1;
	}
	public long getLong(String columnLabel) {
		return -1;
	}
	public GAResultSetMetaData getMetaData() {
		return this.gaResultSetMetaData;
	}
	public Reader getNCharacterStream(int columnIndex) {
		return null;
	}
	public Reader getNCharacterStream(String columnLabel) {
		return null;
	}
	public NClob getNClob(int columnIndex) {
		return null;
	}
	public NClob getNClob(String columnLabel) {
		return null;
	}
	public String getNString(int columnIndex) {
		return null;
	}
	public String getNString(String columnLabel) {
		return null;
	}
	public Object getObject(int columnIndex) {
		return this.getString(columnIndex);
	}
	public <T> T getObject(int columnIndex, Class<T> type) {
		return null;
	}
	public Object getObject(int columnIndex, Map<String,Class<?>> map) {
		return null;
	}
	public Object getObject(String columnLabel) {
		return null;
	}
	public <T> T getObject(String columnLabel, Class<T> type) {
		return null;
	}
	public Object getObject(String columnLabel, Map<String,Class<?>> map) {
		return null;
	}
	public Ref getRef(int columnIndex) {
		return null;
	}
	public Ref getRef(String columnLabel) {
		return null;
	}
	public int getRow() {
		return -1;
	}
	public RowId getRowId(int columnIndex) {
		return null;
	}
	public RowId getRowId(String columnLabel) {
		return null;
	}
	public short getShort(int columnIndex) {
		return -1;
	}
	public short getShort(String columnLabel) {
		return -1;
	}
	public SQLXML getSQLXML(int columnIndex) {
		return null;
	}
	public SQLXML getSQLXML(String columnLabel) {
		return null;
	}
	public Statement getStatement() {
		return null;
	}
	public String getString(int columnIndex) {
		return this.getRowCellValue(columnIndex-1);
	}
	public String getString(String columnLabel) {
		return null;
	}
	public Time getTime(int columnIndex) {
		return null;
	}
	public Time getTime(int columnIndex, Calendar cal) {
		return null;
	}
	public Time getTime(String columnLabel) {
		return null;
	}
	public Time getTime(String columnLabel, Calendar cal) {
		return null;
	}
	public Timestamp getTimestamp(int columnIndex) {
		return null;
	}
	public Timestamp getTimestamp(int columnIndex, Calendar cal) {
		return null;
	}
	public Timestamp getTimestamp(String columnLabel) {
		return null;
	}
	public Timestamp getTimestamp(String columnLabel, Calendar cal) {
		return null;
	}
	public int getType() {
		return -1;
	}
	public InputStream getUnicodeStream(int columnIndex) {
		return null;
	}
	public InputStream getUnicodeStream(String columnLabel) {
		return null;
	}
	public URL getURL(int columnIndex) {
		return null;
	}
	public URL getURL(String columnLabel) {
		return null;
	}
	public SQLWarning getWarnings() {
		return null;
	}
	public void insertRow() {
		;
	}
	public boolean isAfterLast() {
		return false;
	}
	public boolean isBeforeFirst() {
		return (this.rowIndex == 0);
	}
	public boolean isClosed() {
		return false;
	}
	public boolean isFirst() {
		return false;
	}
	public boolean isLast() {
		return false;
	}
	public boolean isWrapperFor(Class<?> iface) {
		return true;
	}
	public boolean last() {
		return false;
	}
	public void moveToCurrentRow() {
		;
	}
	public void moveToInsertRow() {
		;
	}
	private boolean getNextGAReportPage() throws IOException {
		String nextPageToken = this.gaReport.getNextPageToken();
		if(nextPageToken != null) {
			this.gaReportRequest.setPageToken(nextPageToken);

			GetReportsResponse response =
					this.gaService.reports().batchGet(this.gaGetReportsRequest).execute();

			this.gaReport = response.getReports().get(0);
			this.gaReportRows = this.gaReport.getData().getRows();
			this.gaReportRowsIterator = this.gaReportRows.iterator();

			return true;
		}
		return false;
	}
	public boolean next() throws SQLException {
		boolean hasNext = this.gaReportRowsIterator.hasNext();
		if(hasNext) {
			ReportRow row = this.gaReportRowsIterator.next();
			this.setGAReportColumnValues(
				row.getDimensions(),
				row.getMetrics()
			);
			this.rowIndex++;
			return true;
		} else if(!hasNext) {
			try {
				if(this.getNextGAReportPage()) {
					return this.next();
				}
			} catch(Exception e) {
				throw new SQLException(e.getMessage());
			}
		}
		return false;
	}
	public boolean previous() {
		return false;
	}
	public void refreshRow() {
		;
	}
	public boolean relative(int rows) {
		return false;
	}
	public boolean rowDeleted() {
		return false;
	}
	public boolean rowInserted() {
		return false;
	}
	public boolean rowUpdated() {
		return false;
	}
	public void setFetchDirection(int direction) {
		;
	}
	public void setFetchSize(int rows) {
		;
	}
	public <T> T unwrap(Class<T> iface) {
		return null;
	}
	public void updateArray(int columnIndex, Array x) {
		;
	}
	public void updateArray(String columnLabel, Array x) {
		;
	}
	public void updateAsciiStream(int columnIndex, InputStream x) {
		;
	}
	public void updateAsciiStream(int columnIndex, InputStream x, int length) {
		;
	}
	public void updateAsciiStream(int columnIndex, InputStream x, long length) {
		;
	}
	public void updateAsciiStream(String columnLabel, InputStream x) {
		;
	}
	public void updateAsciiStream(String columnLabel, InputStream x, int length) {
		;
	}
	public void updateAsciiStream(String columnLabel, InputStream x, long length) {
		;
	}
	public void updateBigDecimal(int columnIndex, BigDecimal x) {
		;
	}
	public void updateBigDecimal(String columnLabel, BigDecimal x) {
		;
	}
	public void updateBinaryStream(int columnIndex, InputStream x) {
		;
	}
	public void updateBinaryStream(int columnIndex, InputStream x, int length) {
		;
	}
	public void updateBinaryStream(int columnIndex, InputStream x, long length) {
		;
	}
	public void updateBinaryStream(String columnLabel, InputStream x) {
		;
	}
	public void updateBinaryStream(String columnLabel, InputStream x, int length) {
		;
	}
	public void updateBinaryStream(String columnLabel, InputStream x, long length) {
		;
	}
	public void updateBlob(int columnIndex, Blob x) {
		;
	}
	public void updateBlob(int columnIndex, InputStream inputStream) {
		;
	}
	public void updateBlob(int columnIndex, InputStream inputStream, long length) {
		;
	}
	public void updateBlob(String columnLabel, Blob x) {
		;
	}
	public void updateBlob(String columnLabel, InputStream inputStream) {
		;
	}
	public void updateBlob(String columnLabel, InputStream inputStream, long length) {
		;
	}
	public void updateBoolean(int columnIndex, boolean x) {
		;
	}
	public void updateBoolean(String columnLabel, boolean x) {
		;
	}
	public void updateByte(int columnIndex, byte x) {
		;
	}
	public void updateByte(String columnLabel, byte x) {
		;
	}
	public void updateBytes(int columnIndex, byte[] x) {
		;
	}
	public void updateBytes(String columnLabel, byte[] x) {
		;
	}
	public void updateCharacterStream(int columnIndex, Reader x) {
		;
	}
	public void updateCharacterStream(int columnIndex, Reader x, int length) {
		;
	}
	public void updateCharacterStream(int columnIndex, Reader x, long length) {
		;
	}
	public void updateCharacterStream(String columnLabel, Reader reader) {
		;
	}
	public void updateCharacterStream(String columnLabel, Reader reader, int length) {
		;
	}
	public void updateCharacterStream(String columnLabel, Reader reader, long length) {
		;
	}
	public void updateClob(int columnIndex, Clob x) {
		;
	}
	public void updateClob(int columnIndex, Reader reader) {
		;
	}
	public void updateClob(int columnIndex, Reader reader, long length) {
		;
	}
	public void updateClob(String columnLabel, Clob x) {
		;
	}
	public void updateClob(String columnLabel, Reader reader) {
		;
	}
	public void updateClob(String columnLabel, Reader reader, long length) {
		;
	}
	public void updateDate(int columnIndex, Date x) {
		;
	}
	public void updateDate(String columnLabel, Date x) {
		;
	}
	public void updateDouble(int columnIndex, double x) {
		;
	}
	public void updateDouble(String columnLabel, double x) {
		;
	}
	public void updateFloat(int columnIndex, float x) {
		;
	}
	public void updateFloat(String columnLabel, float x) {
		;
	}
	public void updateInt(int columnIndex, int x) {
		;
	}
	public void updateInt(String columnLabel, int x) {
		;
	}
	public void updateLong(int columnIndex, long x) {
		;
	}
	public void updateLong(String columnLabel, long x) {
		;
	}
	public void updateNCharacterStream(int columnIndex, Reader x) {
		;
	}
	public void updateNCharacterStream(int columnIndex, Reader x, long length) {
		;
	}
	public void updateNCharacterStream(String columnLabel, Reader reader) {
		;
	}
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) {
		;
	}
	public void updateNClob(int columnIndex, NClob nClob) {
		;
	}
	public void updateNClob(int columnIndex, Reader reader) {
		;
	}
	public void updateNClob(int columnIndex, Reader reader, long length) {
		;
	}
	public void updateNClob(String columnLabel, NClob nClob) {
		;
	}
	public void updateNClob(String columnLabel, Reader reader) {
		;
	}
	public void updateNClob(String columnLabel, Reader reader, long length) {
		;
	}
	public void updateNString(int columnIndex, String nString) {
		;
	}
	public void updateNString(String columnLabel, String nString) {
		;
	}
	public void updateNull(int columnIndex) {
		;
	}
	public void updateNull(String columnLabel) {
		;
	}
	public void updateObject(int columnIndex, Object x) {
		;
	}
	public void updateObject(int columnIndex, Object x, int scaleOrLength) {
		;
	}
	public void updateObject(String columnLabel, Object x) {
		;
	}
	public void updateObject(String columnLabel, Object x, int scaleOrLength) {
		;
	}
	public void updateRef(int columnIndex, Ref x) {
		;
	}
	public void updateRef(String columnLabel, Ref x) {
		;
	}
	public void updateRow() {
		;
	}
	public void updateRowId(int columnIndex, RowId x) {
		;
	}
	public void updateRowId(String columnLabel, RowId x) {
		;
	}
	public void updateShort(int columnIndex, short x) {
		;
	}
	public void updateShort(String columnLabel, short x) {
		;
	}
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) {
		;
	}
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) {
		;
	}
	public void updateString(int columnIndex, String x) {
		;
	}
	public void updateString(String columnLabel, String x) {
		;
	}
	public void updateTime(int columnIndex, Time x) {
		;
	}
	public void updateTime(String columnLabel, Time x) {
		;
	}
	public void updateTimestamp(int columnIndex, Timestamp x) {
		;
	}
	public void updateTimestamp(String columnLabel, Timestamp x) {
		;
	}
	public boolean wasNull() {
		return false;
	}
	
	public static void main(String args[]) {
		System.out.println("Hello");
	}
}