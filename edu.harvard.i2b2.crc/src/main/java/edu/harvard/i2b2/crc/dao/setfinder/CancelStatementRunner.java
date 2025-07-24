/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Thread class to cancel the long running sql.
 */
public class CancelStatementRunner implements Runnable {

	// default timeout three minutes
	int transactionTimeout = 0;
	Statement stmt = null;
	boolean sqlFinishedFlag = false;

	/**
	 * Constructor
	 * 
	 * @param stmt
	 * @param transactionTimeout
	 */
	public CancelStatementRunner(Statement stmt, int transactionTimeout) {
		this.stmt = stmt;
		this.transactionTimeout = transactionTimeout;

	}

	public boolean getSqlFinishedFlag() {
		return this.sqlFinishedFlag;
	}

	public void setSqlFinishedFlag() {
		this.sqlFinishedFlag = true;
	}

	@Override
	public void run() {
		long currentTime = System.currentTimeMillis();
		long finalTime = currentTime + transactionTimeout * 1000L;
		while (currentTime < finalTime) {
			currentTime = System.currentTimeMillis();
			// t.wait(transactionTimeout);

			try {
				// wait for sec before checking
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore this interrupted message
				;
			}
		}
		try {
			// cancel the stmt if the sql did not complete
			if (this.sqlFinishedFlag == false && stmt != null) {
				stmt.cancel();
			}
		} catch (SQLException e) {

			// if (e.getMessage().indexOf("The query was canceled.") > -1) {
			// timeoutFlag = true;
			// }
		}

	}

}
