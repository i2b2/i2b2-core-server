package edu.harvard.i2b2.crc.dao.pdo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

public class PdoTempTableUtil {

	public void clearTempTable(String serverType, Connection conn, String tableName) { 
		if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ||
				serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) { 
			this.deleteTempTableSqlServer(conn, tableName);
		} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) { 
			this.deleteTempTableOracle(conn, tableName);
		}
	}
	
	public void deleteTempTableSqlServer(Connection conn, String tableName) {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();
			conn.createStatement().executeUpdate("drop table " + tableName);
			deleteStmt.executeUpdate("drop table " + tableName);

		} catch (SQLException sqle) {
			;
		} finally {
			try {
				if(deleteStmt != null)
					deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	public void deleteTempTableOracle(Connection conn, String tableName) { 
		
		
		Statement clearTempStmt = null;
		try {
			clearTempStmt = conn.createStatement();
			clearTempStmt.executeUpdate("delete from " + tableName);
			
		} catch (SQLException dEx) {
			;
		} finally {
			try {
				if(clearTempStmt != null)
				clearTempStmt.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	
	
}
