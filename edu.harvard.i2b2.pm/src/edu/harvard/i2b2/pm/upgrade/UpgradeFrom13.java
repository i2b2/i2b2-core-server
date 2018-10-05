/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.upgrade;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class UpgradeFrom13 {

	static ArrayList admin = new ArrayList();

	static String date = "";
	static File f = null;
	static FileWriter fw = null;
	Connection conn;                                                //our connnection to the db - presist for life of program

	// we dont want this garbage collected until we are done
	public UpgradeFrom13(String db_file_name_prefix) throws Exception {    // note more general exception

		// Load the HSQL Database Engine JDBC driver
		// hsqldb.jar should be in the class path or made part of the current jar
		Class.forName("org.hsqldb.jdbcDriver");

		// connect to the database.   This will load the db files and start the
		// database if it is not alread running.
		// db_file_name_prefix is used to open or create files that hold the state
		// of the db.
		// It can contain directory names relative to the
		// current working directory
		conn = DriverManager.getConnection("jdbc:hsqldb:"
				+ db_file_name_prefix,    // filenames
				"sa",                     // username
		"");                      // password
	}

	public void shutdown() throws SQLException {

		Statement st = conn.createStatement();

		// db writes out to files and performs clean shuts down
		// otherwise there will be an unclean shutdown
		// when program ends
		st.execute("SHUTDOWN");
		conn.close();    // if there are no other open connection
	}

	//use for SQL command SELECT
	public synchronized void query(String expression, String target) throws SQLException {

		Statement st = null;
		ResultSet rs = null;

		st = conn.createStatement();         // statement objects can be reused with

		// repeated calls to execute but we
		// choose to make a new one each time
		rs = st.executeQuery(expression);    // run the query

		// do something with the result set.
		try {
			dump(rs, target);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		st.close();    // NOTE!! if you close a statement the associated ResultSet is

		// closed too
		// so you should copy the contents to some other object.
		// the result set is invalidated also  if you recycle an Statement
		// and try to execute some other query before the result set has been
		// completely examined.
	}


	public static void dump(ResultSet rs, String target) throws SQLException, IOException {

		// the order of the rows in a cursor
		// are implementation dependent unless you use the SQL ORDER statement
		ResultSetMetaData meta   = rs.getMetaData();
		int               colmax = meta.getColumnCount();
		int               i;
		Object            o = null;

		// the result set is a cursor into the data.  You can only
		// point to one row at a time
		// assume we are pointing to BEFORE the first row
		// rs.next() points to next row and returns true
		// or false if there is no next row, which breaks the loop

		String sql = "";
		for (; rs.next(); ) 
		{
			if (target.equals("PM_USER_DATA"))
			{
				sql = "INSERT INTO PM_USER_DATA (USER_ID, FULL_NAME, PASSWORD, EMAIL, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("USERID") + "'," +
				"'" + rs.getString("FULLNAME") + "'," +
				"'" + rs.getString("VALUE")+ "'," +
				"'" + rs.getString("EMAILADDRESS")+ "'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";

			} 
			else if (target.equals("PM_CELL_DATA"))
			{
				sql = "INSERT INTO PM_CELL_DATA (CELL_ID, PROJECT_PATH, NAME, URL, METHOD_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("id") + "'," +
				"'/'," +
				"'" + rs.getString("NAME") + "'," +
				"'" + rs.getString("URL")+ "'," +
				"'" + rs.getString("WEBSERVICE")+ "'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";
			} 
			else if (target.equals("PM_CELL_PARAM"))
			{
				sql = "INSERT INTO PM_CELL_PARAMS (CELL_ID, PROJECT_PATH, PARAM_NAME_CD, VALUE, CAN_OVERRIDE, DATATYPE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("CELLID") + "'," +
				"'/'," +
				"'" + rs.getString("NAME") + "'," +
				"'" + rs.getString("VALUE")+ "'," +
				"1," +
				"'T'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";
			} 
			else if (target.equals("PM_HIVE_DATA"))
			{
				sql = "INSERT INTO PM_HIVE_DATA (DOMAIN_ID, ENVIRONMENT_CD, DOMAIN_NAME, HELPURL, ACTIVE, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("GSOID") + "'," +
				"'" + rs.getString("ENVIRONMENT") + "'," +
				"'" + rs.getString("DOMAIN")+ "'," +
				"'" + rs.getString("URL")+ "'," +
				"1," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";

			}
			else if (target.equals("PM_PROJECT_DATA"))
			{
				sql = "INSERT INTO PM_PROJECT_DATA (PROJECT_ID, PROJECT_NAME, PROJECT_KEY, PROJECT_WIKI, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("GSOID") + "'," +
				"'" + rs.getString("NAME") + "'," +
				"'" + rs.getString("KEY")+ "'," +
				"'" + rs.getString("WIKI")+ "'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";

				if (f != null)
				{
					fw.write(sql);
					fw.write("\n");

				}

				//create the admin accounts
				for(int t=0; t<admin.size(); t++)
				{
					sql = "INSERT INTO PM_PROJECT_USER_ROLES ( PROJECT_ID, USER_ID, USER_ROLE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
					"(" +
					"'" + rs.getString("GSOID") + "'," +
					"'" + admin.get(t) + "'," +
					"'ADMIN'," +
					date +						
					date +						
					"'Upgrade From 1.3'," +
					"'A');";
					if (f != null)
					{
						fw.write(sql);
						fw.write("\n");
					}
					sql = "INSERT INTO PM_PROJECT_USER_ROLES ( PROJECT_ID, USER_ID, USER_ROLE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
					"(" +
					"'" + rs.getString("GSOID") + "'," +
					"'" + admin.get(t) + "'," +
					"'MANAGER'," +
					date +						
					date +						
					"'Upgrade From 1.3'," +
					"'A');";
					if (f != null)
					{
						fw.write(sql);
						fw.write("\n");
					}

					sql = "INSERT INTO PM_PROJECT_USER_ROLES ( PROJECT_ID, USER_ID, USER_ROLE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
					"(" +
					"'" + rs.getString("GSOID") + "'," +
					"'" + admin.get(t) + "'," +
					"'USER'," +
					date +						
					date +						
					"'Upgrade From 1.3'," +
					"'A');";
					if (f != null)
					{
						fw.write(sql);
						fw.write("\n");
					}

					sql = "INSERT INTO PM_PROJECT_USER_ROLES ( PROJECT_ID, USER_ID, USER_ROLE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
					"(" +
					"'" + rs.getString("GSOID") + "'," +
					"'" + admin.get(t) + "'," +
					"'DATA_OBFSC'," +
					date +						
					date +						
					"'Upgrade From 1.3'," +
					"'A');";
					if (f != null)
					{
						fw.write(sql);
						fw.write("\n");
					}

					
				} 

				//Create the obfsc system account
				sql = "INSERT INTO PM_PROJECT_USER_ROLES ( PROJECT_ID, USER_ID, USER_ROLE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("GSOID") + "'," +
				"'OBFSC_SERVICE_ACCOUNT'," +
				"'USER'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";
				if (f != null)
				{
					fw.write(sql);
					fw.write("\n");

				}


				sql = "INSERT INTO PM_PROJECT_USER_ROLES ( PROJECT_ID, USER_ID, USER_ROLE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("GSOID") + "'," +
				"'OBFSC_SERVICE_ACCOUNT'," +
				"'DATA_OBFSC'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";

			}
			else if (target.equals("PM_PROJECT_PARAMS"))
			{
				sql = "INSERT INTO PM_PROJECT_PARAMS (PROJECT_ID, PARAM_NAME_CD, VALUE, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("PROJECT") + "'," +
				"'" + rs.getString("NAME") + "'," +
				"'" + rs.getString("VALUE")+ "'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";

			}
			else if (target.equals("PM_GLOBAL_DATA"))
			{
				sql = "INSERT INTO PM_GLOBAL_DATA ( PARAM_NAME_CD, PROJECT_PATH, VALUE, CAN_OVERRIDE, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("NAME") + "'," +
				"'/'," +
				"'" + rs.getString("VALUE")+ "'," +
				"1," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";

			}
			else if (target.equals("PM_PROJECT_USER_ROLES"))
			{
				sql = "INSERT INTO PM_PROJECT_USER_ROLES ( PROJECT_ID, USER_ID, USER_ROLE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("PROJECT") + "'," +
				"'" + rs.getString("USER") + "'," +
				"'" + rs.getString("ROLE") + "'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";

			}
			else if (target.equals("PM_ADMIN"))
			{

				System.out.println("The Following user will be a Administrator: " + rs.getString("USERID"));
				admin.add(rs.getString("USERID"));

			}
			else if (target.equals("PM_PROJECT_USER_ROLES_OBFSC"))
			{
				sql = "INSERT INTO PM_PROJECT_USER_ROLES ( PROJECT_ID, USER_ID, USER_ROLE_CD, ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) VALUES " +
				"(" +
				"'" + rs.getString("PROJECT") + "'," +
				"'" + rs.getString("USER") + "'," +
				"'DATA_OBFSC'," +
				date +						
				date +						
				"'Upgrade From 1.3'," +
				"'A');";

			} else {
				//		for (; rs.next(); ) {
				for (i = 0; i < colmax; ++i) {
					o = rs.getObject(i + 1);    // Is SQL the first column is indexed
					String cname = meta.getColumnName(i + 1);
					// with 1 not 0
					System.out.print(cname + " = " + o.toString() + ", ");
				}

				System.out.println(" ");
			}
			if (f != null)
			{
				fw.write(sql);
				fw.write("\n");

			}
		}
	}                                       //void dump( ResultSet rs )

	public static void main(String[] args) {

		UpgradeFrom13 db = null;
		UpgradeFrom13 dbUser = null;
		String userDb = "/opt/apache-tomcat-5.5.26/webapps/gridsphere/WEB-INF/CustomPortal/database";
		String i2b2Db = "/opt/apache-tomcat-5.5.26/webapps/default/WEB-INF/database";

		SimpleDateFormat dateformatter = new SimpleDateFormat
		("yyyy-MM-dd HH:mm:ss");
		if (args[0].equalsIgnoreCase("ORACLE"))
		{
			date = "TO_DATE('" + dateformatter.format(Calendar.getInstance().getTime()) + "', 'YYYY-MM-DD HH24:MI:SS'), "; 
		} else
		{
			date = "'" + dateformatter.format(Calendar.getInstance().getTime()) + "', ";
		}

		if (args.length > 1)
		{
			f = new File(args[1]);
			try {
				fw = new FileWriter(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (args.length > 2)
		{
			userDb = args[2];
			i2b2Db = args[3];
		}
		try {
			db = new UpgradeFrom13(i2b2Db + "/gridsphere");
			dbUser = new UpgradeFrom13(userDb + "/gridsphere");
		} catch (Exception ex1) {
			ex1.printStackTrace();    // could not start db

			return;                   // bye bye
		}

		try {

			// do a query
			dbUser.query("SELECT u.userid  FROM SPORTLETUSERIMPL u, PORTLETROLE r, USERROLES ur where UR.SPORTLETUSER = u.gsoid AND UR.ROLE = R.GSOID AND r.ROLENAME='SUPER'", "PM_ADMIN");
			dbUser.query("SELECT u.userid, p.value, u.fullname, u.emailaddress  FROM SPORTLETUSERIMPL u, GSPASSWORD p where p.SPORTLETUSER = u.gsoid", "PM_USER_DATA");
			db.query("SELECT * FROM PM_REGISTERED_CELL", "PM_CELL_DATA");
			db.query("SELECT * FROM PM_REGISTERED_CELL_PARAM", "PM_CELL_PARAM");
			db.query("SELECT * FROM PM_ENVIRONMENT_DATA", "PM_HIVE_DATA");
			db.query("SELECT * FROM PM_USER_DATA", "PM_USER_DATA");
			db.query("SELECT * FROM PM_PROJECT_DATA", "PM_PROJECT_DATA");
			db.query("SELECT * FROM PM_VARIABLE_DATA", "PM_PROJECT_PARAMS");
			db.query("SELECT * FROM PM_GLOBAL_DATA", "PM_GLOBAL_PARAMS");
			db.query("SELECT * FROM PM_ROLE_DATA", "PM_PROJECT_USER_ROLES");
			db.query("SELECT DISTINCT PROJECT, USER FROM PM_ROLE_DATA", "PM_PROJECT_USER_ROLES_OBFSC");

			//Add in seervice account
			fw.write("INSERT INTO PM_USER_DATA (USER_ID, FULL_NAME, PASSWORD,  ENTRY_DATE, CHANGE_DATE, CHANGEBY_CHAR, STATUS_CD) "+ 
					"VALUES('OBFSC_SERVICE_ACCOUNT', 'OBFSC_SERVICE_ACCOUNT', '9117d59a69dc49807671a51f10ab7f', " + date + date +"'Upgrade From 1.3','A');\n");

			//Add in role requirement 
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_HIVE_DATA', '@', '@', 'ADMIN', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_HIVE_PARAMS', '@', '@', 'ADMIN', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_PROJECT_DATA', '@', '@', 'MANAGER', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_PROJECT_USER_ROLES', '@', '@', 'MANAGER', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_USER_DATA', '@', '@', 'ADMIN', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_PROJECT_PARAMS', '@', '@', 'MANAGER', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_PROJECT_USER_PARAMS', '@', '@', 'MANAGER', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_USER_PARAMS', '@', '@', 'ADMIN', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_CELL_DATA', '@', '@', 'MANAGER', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_CELL_PARAMS', '@', '@', 'MANAGER', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			fw.write("INSERT INTO PM_ROLE_REQUIREMENT(TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD, NAME_CHAR, CHANGE_DATE, ENTRY_DATE, CHANGEBY_CHAR, STATUS_CD) " +
					"VALUES('PM_GLOBAL_PARAMS', '@', '@', 'ADMIN', NULL, " + date + date +"'Upgrade From 1.3','A');\n");
			// at end of program
			db.shutdown();
			if (f != null)
			{
				fw.close();
			}
		} catch (Exception ex3) {
			ex3.printStackTrace();
		}
	}    // main()
}    // class Testdb
