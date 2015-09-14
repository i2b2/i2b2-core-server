package edu.harvard.i2b2.common.util.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.RowSet;

public class JDBCUtil {

	/**
	 * Function to convert clob to string
	 * @param clob
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static String  getClobString(Clob clob) throws SQLException, IOException { 
		BufferedReader stringReader = new BufferedReader(clob.getCharacterStream());
		String singleLine = null;
		StringBuffer strBuff = new StringBuffer();
			while ((singleLine = stringReader.readLine()) != null) { 
				strBuff.append(singleLine);
			}
		return strBuff.toString();
	}
	
	/**
	 * Function to convert clob to string
	 * @param clob
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static String  getClobStringWithLinebreak(Clob clob) throws SQLException, IOException { 
		Reader read = new InputStreamReader(clob.getAsciiStream());
		StringWriter write = new StringWriter();

		int c = -1;
		while ((c = read.read()) != -1)
		{
			write.write(c);
		}
		write.flush();
		return write.toString();
	}
	
	/**
	 * Use this function to escape single quote string
	 * For example: Hi' Hello --> Hi'' Hello
	 * @param value string 
	 * @return single quote escaped string
	 */
	public static String escapeSingleQuote(String value) { 
		String escapedValue = null;
		if (value != null) { 
			escapedValue = value.replaceAll("'", "\\''");
		}
		return escapedValue;
	}
	
	/**
	 * Helper function to cloase jdbc resources 
	 * @param rowSet
	 * @param stmt
	 * @param conn
	 * @throws SQLException
	 */
	public static void closeJdbcResource(RowSet rowSet,Statement stmt, Connection conn) throws SQLException { 
		if (rowSet != null) { 
			rowSet.close();
		}
		if (stmt != null) { 
			stmt.close();
		}
		if (conn != null) {
			conn.close();
		}
	}
}
