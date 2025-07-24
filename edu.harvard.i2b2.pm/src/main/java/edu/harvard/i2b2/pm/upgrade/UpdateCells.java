/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.upgrade;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;


public class UpdateCells {

	Connection conn;                                                //our connnection to the db - presist for life of program


	public UpdateCells() throws Exception {    // note more general exception

/*

		Class.forName("oracle.jdbc.driver.OracleDriver");

		conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
				"i2b2pm",                     // username
		"demouser");                      // password
		*/
	}


	//use for SQL command SELECT
	public synchronized void query(String expression) throws SQLException {

		Statement st = null;
		ResultSet rs = null;

		st = conn.createStatement();         // statement objects can be reused with

		// repeated calls to execute but we
		// choose to make a new one each time
		rs = st.executeQuery(expression);    // run the query

		// do something with the result set.
		st.close();    // NOTE!! if you close a statement the associated ResultSet is

		// closed too
		// so you should copy the contents to some other object.
		// the result set is invalidated also  if you recycle an Statement
		// and try to execute some other query before the result set has been
		// completely examined.
	}


	/**
	 * Returns an InetAddress representing the address 
of the localhost.  
	 * Every attempt is made to find an address for this 
host that is not 
	 * the loopback address.  If no other address can 
be found, the 
	 * loopback will be returned.
	 * 
	 * @return InetAddress - the address of localhost
	 * @throws UnknownHostException - if there is a 
problem determing the address
	 */
	public static InetAddress getLocalHost() throws 
UnknownHostException {
		InetAddress localHost = 
InetAddress.getLocalHost();
		if(!localHost.isLoopbackAddress()) return 
localHost;
		InetAddress[] addrs = 
getAllLocalUsingNetworkInterface();
		for(int i=0; i<addrs.length; i++) {
			if(!addrs[i].isLoopbackAddress()) 
return addrs[i];
		}
		return localHost;	
	}
	
	/**
	 * This method attempts to find all InetAddresses for this machine in a 
	 * conventional way (via InetAddress).  If only one address is found 
	 * and it is the loopback, an attempt is made to determine the addresses 
	 * for this machine using NetworkInterface.
	 * 
	 * @return InetAddress[] - all addresses assigned to the local machine
	 * @throws UnknownHostException - if there is a problem determining addresses
	 */
	public static InetAddress[] getAllLocal() throws 
UnknownHostException {
		InetAddress[] iAddresses = 
InetAddress.getAllByName("127.0.0.1");
		if(iAddresses.length != 1) return 
iAddresses;
		if(!iAddresses[0].isLoopbackAddress()) 
return iAddresses;
		return getAllLocalUsingNetworkInterface();
	
	}
	
	/**
	 * Utility method that delegates to the methods of NetworkInterface to 
	 * determine addresses for this machine.
	 * 
	 * @return InetAddress[] - all addresses found from the NetworkInterfaces
	 * @throws UnknownHostException - if there is a problem determining addresses
	 */
	private static InetAddress[] 
getAllLocalUsingNetworkInterface() throws 
UnknownHostException {
		ArrayList addresses = new ArrayList();
		Enumeration e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException ex) {
			throw new UnknownHostException
("127.0.0.1");
		}
		while(e.hasMoreElements()) {
			NetworkInterface ni = 
(NetworkInterface)e.nextElement();
			for(Enumeration e2 = 
ni.getInetAddresses(); e2.hasMoreElements();) {
				addresses.add
(e2.nextElement());
			}	
		}
		InetAddress[] iAddresses = new 
InetAddress[addresses.size()];
		for(int i=0; i<iAddresses.length; i++) {
			iAddresses[i] = (InetAddress)
addresses.get(i);
		}
		return iAddresses;
	}


	public static void main(String[] args) {
		UpdateCells db = null;



		try {

			db = new UpdateCells();
			InetAddress addr = db.getLocalHost();

			// Get IP Address

			String ipAddr = addr.getHostAddress();

			// Get hostname
			String hostname = addr.getHostName();

			db.query("UPDATE PM_REGISTERED_CELL SET URL = '" + ipAddr + "/i2b2/rest/QueryToolService/' WHERE CELL_ID = 'CRC'");
			db.query("UPDATE PM_REGISTERED_CELL SET URL = '" + ipAddr + "/i2b2/rest/OntologyService/' WHERE CELL_ID = 'ONT'");
			db.query("UPDATE PM_REGISTERED_CELL SET URL = '" + ipAddr + "/i2b2/rest/WorkplaceService/' WHERE CELL_ID = 'WORK'");
			db.query("UPDATE PM_REGISTERED_CELL SET URL = '" + ipAddr + "/i2b2/services/FRService/' WHERE CELL_ID = 'FRC'");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
