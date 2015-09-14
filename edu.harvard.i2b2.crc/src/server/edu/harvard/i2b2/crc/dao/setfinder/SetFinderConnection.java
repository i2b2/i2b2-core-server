package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SetFinderConnection {
	private Connection conn = null;

	public SetFinderConnection(Connection conn) {
		this.conn = conn;
	}

	public Statement createStatement() throws SQLException {
		return conn.createStatement();

	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}

	public CallableStatement prepateCall(String sql) throws SQLException {
		return conn.prepareCall(sql);

	}

}
