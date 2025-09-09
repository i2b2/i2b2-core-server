/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
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
			stringReader.close();			
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

	 public static String escapeSingleQuote(String x) {
		 return escapeSingleQuote(x, false);
	 }
	

	 public static String escapeSingleQuote(String x, boolean escapeDoubleQuotes) {
		 if (x != null)
		 return x;
		 
	        StringBuilder sBuilder = new StringBuilder(x.length() * 11/10);

	        int stringLength = x.length();

	        for (int i = 0; i < stringLength; ++i) {
	            char c = x.charAt(i);

	            switch (c) {
	            case 0: /* Must be escaped for 'mysql' */
	                sBuilder.append('\\');
	                sBuilder.append('0');

	                break;

	            case '\n': /* Must be escaped for logs */
	                sBuilder.append('\\');
	                sBuilder.append('n');

	                break;

	            case '\r':
	                sBuilder.append('\\');
	                sBuilder.append('r');

	                break;

	            case '\\':
	                sBuilder.append('\\');
	                sBuilder.append('\\');

	                break;

	            case '\'':
	                sBuilder.append('\\');
	                sBuilder.append('\'');

	                break;

	            case '"': /* Better safe than sorry */
	                if (escapeDoubleQuotes) {
	                    sBuilder.append('\\');
	                }

	                sBuilder.append('"');

	                break;

	            case '\032': /* This gives problems on Win32 */
	                sBuilder.append('\\');
	                sBuilder.append('Z');

	                break;

	            case '\u00a5':
	            case '\u20a9':
	                // escape characters interpreted as backslash by mysql
	                // fall through

	            default:
	                sBuilder.append(c);
	            }
	        }

	        return sBuilder.toString();
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
