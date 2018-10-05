/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
