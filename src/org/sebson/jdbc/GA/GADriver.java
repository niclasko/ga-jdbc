package org.sebson.jdbc.GA;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import java.lang.RuntimeException;

public class GADriver implements Driver {
	
	private static final String JDBC_URL_PREFIX = "jdbc:GA";
	
	// This static block initializes the driver when the class is loaded by the JVM.
	static {
		try {
			DriverManager.registerDriver(new GADriver());
		} catch (SQLException e) {
			throw new RuntimeException("Could not load GA driver: " + e.getMessage());
		}
	}
	
	public GADriver() {
		;
	}
	
	public boolean acceptsURL(String url) {
		return true;
	}
	public GAConnection connect(String url, Properties info) throws SQLException {

		Properties props = new Properties();
		String[] parts = url.split("\\?")[1].split("=");

		props.put(parts[0], parts[1]);

		if(url == null) {
			throw new SQLException("null is not a valid url");
		}
		
		if(url.startsWith(GADriver.JDBC_URL_PREFIX)) {
			return new GAConnection(props);
		} else {
			throw new SQLException("Invalid url prefix. Valid prefix is \"" + GADriver.JDBC_URL_PREFIX + "\".");
		}
		
	}
	public int getMajorVersion() {
		return 1;
	}
	public int getMinorVersion() {
		return 0;
	}
	public Logger getParentLogger() {
		return null;
	}
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
		return null;
	}
	public boolean jdbcCompliant() {
		return false;
	}
	
	public static void main(String args[]) throws Exception {

		Properties info = new Properties();

		String[] parts;

		for(int i=0; i<args.length-1; i++) {
			parts = args[i].split("=");
			info.setProperty(parts[0], parts[1]);
		}
		
		GADriver gaDriver = new GADriver();
		GAConnection gaConnection = gaDriver.connect(
			GADriver.JDBC_URL_PREFIX + "://?" + args[0],
			info
		);
		GAStatement gaStmt = gaConnection.createStatement();

		GAResultSet gaRs =
			gaStmt.executeQuery(args[args.length-1]);

		for(int i=0; i<gaRs.getMetaData().getColumnCount(); i++) {
			System.out.print(
				(i>0 ? ";" : "") + gaRs.getMetaData().getColumnName(i+1)
			);
		}
		System.out.println();
		
		while(gaRs.next()) {
			for(int i=0; i<gaRs.getMetaData().getColumnCount(); i++) {
				System.out.print(
					(i>0 ? ";" : "") + gaRs.getString(i+1)
				);
			}
			System.out.println();
		}

		gaRs.close();
		
	}
}