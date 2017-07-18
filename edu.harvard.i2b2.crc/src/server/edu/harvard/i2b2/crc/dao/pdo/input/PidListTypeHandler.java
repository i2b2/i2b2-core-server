/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.input;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PidListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PidListType.Pid;

/**
 * Handler class for pid list type to generate "where" clause for pdo request
 * $Id: PidListTypeHandler.java,v 1.5 2009/10/23 19:54:02 rk903 Exp $
 * 
 * @author rkuttan
 */
public class PidListTypeHandler extends CRCDAO implements
		IInputOptionListHandler {
	private PidListType pidListType = null;
	private int minIndex = 0;
	private int maxIndex = 0;
	private String encounterSetCollId = "";
	private List<String> encounterNumList = null;
	private DataSourceLookup dataSourceLookup = null;
	private boolean deleteTempTableFlag = false;

	/**
	 * Constructor accepts {@link EventListType}
	 * 
	 * @param visitListType
	 * @throws I2B2DAOException
	 */
	public PidListTypeHandler(DataSourceLookup dataSourceLookup,
			PidListType pidListType) throws I2B2DAOException {
		if (pidListType == null) {
			throw new I2B2DAOException("Pid List Type is null");
		}

		this.dataSourceLookup = dataSourceLookup;
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.pidListType = pidListType;

		if (pidListType.getMin() != null) {
			minIndex = pidListType.getMin();
		}

		if (pidListType.getMax() != null) {
			maxIndex = pidListType.getMax();
		}
	}

	public int getMinIndex() {
		return minIndex;
	}

	public int getMaxIndex() {
		return maxIndex;
	}

	public boolean isCollectionId() {
		return false;
	}

	public boolean isEnumerationSet() {
		if ((pidListType.getPid() != null) && (pidListType.getPid().size() > 0)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isEntireSet() {
		return false;
	}

	/**
	 * Function to generate "where" clause for visit/event list
	 */
	public String generateWhereClauseSql() {

		String tempTableName = getTempTableName();

		String sqlString = "SELECT "
				+ " pm.patient_num "
				+ " FROM "
				+ getDbSchemaName()
				+ "patient_mapping pm WHERE  exists (select  1 FROM "
				+ tempTableName
				+ " where char_param1 = pm.patient_ide and char_param2 = pm.patient_ide_source)  ";
		return sqlString;
	}

	public String generatePatentSql() {
		String sqlString = null;

		return sqlString;
	}

	public List<String> getEnumerationList() {
		ArrayList<String> encounterNumArrayList = new ArrayList<String>();
		return this.encounterNumList;
	}

	public void uploadEnumerationValueToTempTable(Connection conn)
			throws SQLException {

		String tempTableName = this.getTempTableName();
		deleteTempTableFlag = true;
		// create temp table
		java.sql.Statement tempStmt = conn.createStatement();
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			String createTempInputListTable = "create table "
					+ getTempTableName()
					+ " (set_index int, char_param1 varchar(100), char_param2 varchar(100) )";
			tempStmt.executeUpdate(createTempInputListTable);
		}
		int i = 0, j = 1;

		List<Pid> pidList = pidListType.getPid();
		List<Pid> finalPidList = new ArrayList<Pid>();
		if (maxIndex > pidListType.getPid().size()) {
			// log.warn("max size is more than list size");
			maxIndex = pidListType.getPid().size();
		}
		if (minIndex < maxIndex) {
			finalPidList = pidList.subList(minIndex, maxIndex);
		} else if (minIndex == maxIndex && minIndex > 0) {
			// check if maxIndex is equal to last index
			if (maxIndex == pidListType.getPid().size() - 1) {
				finalPidList.add(pidList.get(maxIndex));
			} else {
				finalPidList = pidList.subList(minIndex, maxIndex);
			}

		} else {
			maxIndex = pidList.size();
			finalPidList = pidList.subList(minIndex, maxIndex);
		}

		PreparedStatement preparedStmt = conn.prepareStatement("insert into "
				+ tempTableName
				+ "(set_index,char_param1,char_param2)  values (?,?,?)");
		for (Pid pid : finalPidList) {
			preparedStmt.setInt(1, j++);
			preparedStmt.setString(2, pid.getValue());
			preparedStmt.setString(3, pid.getSource());
			preparedStmt.addBatch();
			i++;
			if (i % 100 == 0) {
				preparedStmt.executeBatch();

			}
			log.debug("loading " + pid.getValue() + " " + pid.getSource());
		}
		preparedStmt.executeBatch();
	}

	public void deleteTempTable(Connection conn) throws SQLException {
		if (!deleteTempTableFlag) {
			return;
		}
		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL)) {
				//conn.createStatement().executeUpdate(
				//		"drop table " + getTempTableName());
				deleteStmt.executeUpdate(
						"drop table " + getTempTableName());
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				//conn.createStatement().executeUpdate(
				//		"delete  " + getTempTableName());
				deleteStmt.executeUpdate(
						"delete  " + getTempTableName());
			}
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			try {
				if(deleteStmt == null)
					deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getCollectionId() {
		return "";
	}

	public String generateMinIndexSql(String panelSql) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns input list's size. if the list is collection id, then collection
	 * set size, if the list is entire set, then total rows in dimension table
	 * if the list is enumeration, then size of enumeration set
	 * 
	 * @return
	 * @throws I2B2DAOException
	 */
	public int getInputSize() throws I2B2DAOException {

		return 0;

	}

	public void setMaxIndex(int maxIndex) {
		pidListType.setMax(maxIndex);

	}

	private String getTempTableName() {
		String tempTableName = "";
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE)) {
			tempTableName = this.getDbSchemaName()
					+ FactRelatedQueryHandler.TEMP_PARAM_TABLE;
		} else {
			tempTableName = this.getDbSchemaName()
					+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
		}
		return tempTableName;
	}
}
