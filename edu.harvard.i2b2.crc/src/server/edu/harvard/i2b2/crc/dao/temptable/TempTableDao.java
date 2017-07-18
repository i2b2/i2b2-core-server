package edu.harvard.i2b2.crc.dao.temptable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TempTableDao {

	public void deleteTempTable(Connection conn, String tempTableName)
			throws SQLException {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();
			//conn.createStatement().executeUpdate("drop table " + tempTableName);
			deleteStmt.executeUpdate("drop table " + tempTableName);
			
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			try {
				deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void createTable() {

	}
}
