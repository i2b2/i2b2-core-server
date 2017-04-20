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
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType.PatientId;

/**
 * Handler class for patient list type to generate "where" clause for pdo
 * request $Id: PatientListTypeHandler.java,v 1.8 2008/06/10 14:59:04 rk903 Exp
 * $
 * 
 * @author rkuttan
 */
public class PatientListTypeHandler extends CRCDAO implements
		IInputOptionListHandler {
	private PatientListType patientListType = null;
	private int minIndex = 0;
	private int maxIndex = 0;
	private String patientSetCollId = "";
	private List<String> patientNumList = null;
	private DataSourceLookup dataSourceLookup = null;
	private boolean deleteTempTableFlag = false;

	/**
	 * Constructor accepts {@link PatientListType}
	 * 
	 * @param patientListType
	 * @throws I2B2DAOException
	 */
	public PatientListTypeHandler(DataSourceLookup dataSourceLookup,
			final PatientListType patientListType) throws I2B2DAOException {
		if (patientListType == null) {
			throw new I2B2DAOException("Patient List Type is null");
		}
		this.dataSourceLookup = dataSourceLookup;
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.patientListType = patientListType;

		if (patientListType.getMin() != null) {
			minIndex = patientListType.getMin();
		}

		if (patientListType.getMax() != null) {
			maxIndex = patientListType.getMax();
		}

	}

	public int getMinIndex() {
		return minIndex;
	}

	public int getMaxIndex() {
		return maxIndex;
	}

	public void setMaxIndex(int maxIndex) {
		patientListType.setMax(maxIndex);
	}

	/**
	 * Function to generate "where" clause for patient list
	 */
	public String generateMinIndexSql(String panelSql) {
		String sqlString = null;

		if (patientListType.getPatientSetCollId() != null) {
			// set patient set coll id
			this.patientSetCollId = this.getCollectionId();

			String asClause = "as";
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				asClause = " ";
			}

			// set sql string
			sqlString = "select min(set_index) ,count(*)  from "
					+ this.getDbSchemaName()
					+ "qt_patient_set_collection pset where pset.result_instance_id =  ?  ";

			if (minIndex <= maxIndex) {
				sqlString += (" and pset.set_index between " + minIndex
						+ " and " + maxIndex);
			}
			sqlString += " and pset.patient_num in (select obs_patient_num from ( "
					+ panelSql + " ) " + asClause + " panelPatientSubQuery ) ";
		} else if ((patientListType.getPatientId() != null)
				&& (patientListType.getPatientId().size() > 0)) {

			this.getEnumerationList();

			String tempTableName = "";
			String asClause = "as";
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				tempTableName = FactRelatedQueryHandler.TEMP_PARAM_TABLE;
				asClause = " ";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL)) {
				tempTableName = SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
			}
			sqlString = " select min(set_index), count(*) from "
					+ this.getDbSchemaName() + tempTableName;
			sqlString += " where ";
			if (minIndex <= maxIndex) {
				if (maxIndex == 1) {
					minIndex = 0;
				}
				sqlString += "  set_index between  " + minIndex + " and "
						+ maxIndex;
			}
			sqlString += " and char_param1 in (select obs_patient_num from ( "
					+ panelSql + " )" + asClause + " panelPatientSubQuery )";

		} else if (patientListType.getEntirePatientSet() != null) {
			// by default get first 100 rows
			if ((minIndex == 0) && (maxIndex == 0)) {
				minIndex = 0;
				maxIndex = 100;
			}

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				// do nothing
				sqlString = " select patient_num from (select p.*, ROWNUM rnum from ( select patient_num from "
						+ this.getDbSchemaName()
						+ "patient_dimension  order by patient_num) p "
						+ "	where ROWNUM<="
						+ maxIndex
						+ " ) where  rnum>="
						+ minIndex;
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL)) {
				sqlString = "	select patient_num from (select *, ROW_number() over (order by patient_num asc) as  rnum "
						+ " from "
						+ this.getDbSchemaName()
						+ "patient_dimension p) as p1  where rnum between  "
						+ minIndex + "  and  " + maxIndex;
			}
		}

		return sqlString;
	}

	/**
	 * Function to generate "where" clause for patient list
	 */
	public String generateWhereClauseSql() {
		String sqlString = null;

		if (patientListType.getPatientSetCollId() != null) {
			// set patient set coll id
			this.patientSetCollId = this.getCollectionId();

			// set sql string
			sqlString = "select pset.patient_num from "
					+ this.getDbSchemaName()
					+ "qt_patient_set_collection pset where pset.result_instance_id =  ?  ";

			if (minIndex <= maxIndex) {
				sqlString += (" and pset.set_index between " + minIndex
						+ " and " + maxIndex);
			}
		} else if ((patientListType.getPatientId() != null)
				&& (patientListType.getPatientId().size() > 0)) {

			// this.getEnumerationList();
			String tempTableName = this.getTempTableName();

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL))
			{
			sqlString = " select cast(char_param1 as integer) from " + tempTableName + "  ";
			}
			else {
				sqlString = " select char_param1 from " + tempTableName + "  ";

			}
		} else if (patientListType.getEntirePatientSet() != null) {
			// by default get first 100 rows
			if ((minIndex == 0) && (maxIndex == 0)) {
				minIndex = 0;
				maxIndex = 100;
			}

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				// do nothing
				sqlString = " select patient_num from (select p.*, ROWNUM rnum from ( select patient_num from "
						+ this.getDbSchemaName()
						+ "patient_dimension  order by patient_num) p "
						+ "	where ROWNUM<="
						+ maxIndex
						+ " ) where  rnum>="
						+ minIndex;
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL)) {
				sqlString = "	select patient_num from (select *, ROW_number() over (order by patient_num asc) as  rnum "
						+ " from "
						+ this.getDbSchemaName()
						+ "patient_dimension p) as p1  where rnum between  "
						+ minIndex + "  and  " + maxIndex;
			}
		}

		return sqlString;
	}

	public List<String> getIntListFromPatientNumList() {
		return this.patientNumList;
	}

	public String getCollectionId() {
		if (isCollectionId()) {
			return patientListType.getPatientSetCollId();
		} else {
			return "";
		}

	}

	public List<String> getEnumerationList() {
		ArrayList<String> patientNumArrayList = new ArrayList<String>(
				patientListType.getPatientId().size() + 1);
		patientNumArrayList.add("");

		// index 0
		// patientNumArrayList.add("");
		for (PatientListType.PatientId patientNum : patientListType
				.getPatientId()) {

			// patientNum.getIndex()
			// TODO see if we can use index value from patientNum
			patientNumArrayList.add(patientNum.getValue());

		}

		if (maxIndex >= patientListType.getPatientId().size() + 1) {
			maxIndex = patientListType.getPatientId().size() + 1;
		} else {
			maxIndex += 1;
		}

		// set int List
		if (minIndex < maxIndex) {
			this.patientNumList = patientNumArrayList.subList(minIndex,
					maxIndex);
		} else if (minIndex == maxIndex && minIndex > 0) {
			// check if maxIndex is equal to last index
			if (maxIndex == patientListType.getPatientId().size() - 1) {
				this.patientNumList = new ArrayList();
				this.patientNumList.add(patientNumArrayList.get(maxIndex));
			} else {
				this.patientNumList = patientNumArrayList.subList(minIndex,
						maxIndex);
			}

		} else {
			maxIndex = patientNumArrayList.size();
			this.patientNumList = patientNumArrayList.subList(minIndex,
					maxIndex);
		}
		System.out.println(" MAX INDEX *** " + maxIndex);
		return this.patientNumList;
	}

	public boolean isCollectionId() {
		if (patientListType.getPatientSetCollId() != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isEntireSet() {
		if (patientListType.getEntirePatientSet() != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isEnumerationSet() {
		if ((patientListType.getPatientId() != null)
				&& (patientListType.getPatientId().size() > 0)) {
			return true;
		} else {
			return false;
		}
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
		if (this.isEnumerationSet()) {
			return patientListType.getPatientId().size();
		} else if (this.isCollectionId()) {
			DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup
					.getDomainId(), dataSourceLookup.getProjectPath(),
					dataSourceLookup.getOwnerId());
			IQueryResultInstanceDao resultInstanceDao = helper.getDAOFactory()
					.getSetFinderDAOFactory().getPatientSetResultDAO();

			QtQueryResultInstance resultInstance = resultInstanceDao
					.getResultInstanceById(this.getCollectionId());
			return resultInstance.getSetSize();
		} else if (this.isEntireSet()) {
			return 1000;
		} else {
			return 0;
		}
	}

	public void uploadEnumerationValueToTempTable(Connection conn)
			throws SQLException {

		String tempTableName = this.getTempTableName();
		deleteTempTableFlag = true;
		deleteTempTable(conn);
		// create temp table
		java.sql.Statement tempStmt = conn.createStatement();
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			String createTempInputListTable = "create table "
					+ getTempTableName()
					+ " (set_index int, char_param1 varchar(100) )";
			tempStmt.executeUpdate(createTempInputListTable);
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL))
		{
			String createTempInputListTable = "create temp table "
					+ getTempTableName()
					+ " (set_index int, char_param1 varchar(100) )";
			tempStmt.executeUpdate(createTempInputListTable);
		}
		int i = 0, j = 1;

		List<PatientId> pidList = patientListType.getPatientId();
		List<PatientId> finalPidList = new ArrayList<PatientId>();
		if (maxIndex > patientListType.getPatientId().size()) {
			// log.warn("max size is more than list size");
			maxIndex = patientListType.getPatientId().size();
		}
		if (minIndex < maxIndex) {
			finalPidList = pidList.subList(minIndex, maxIndex);
		} else if (minIndex == maxIndex && minIndex > 0) {
			// check if maxIndex is equal to last index
			if (maxIndex == patientListType.getPatientId().size() - 1) {
				finalPidList.add(pidList.get(maxIndex));
			} else {
				finalPidList = pidList.subList(minIndex, maxIndex);
			}

		} else {
			maxIndex = pidList.size();
			finalPidList = pidList.subList(minIndex, maxIndex);
		}
		PreparedStatement preparedStmt = conn.prepareStatement("insert into "
				+ tempTableName + "(set_index,char_param1)  values (?,?)");
		for (PatientId pid : finalPidList) {
			preparedStmt.setInt(1, j++);
			preparedStmt.setString(2, pid.getValue());
			preparedStmt.addBatch();
			i++;
			if (i % 100 == 0) {
				preparedStmt.executeBatch();

			}

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
			//	conn.createStatement().executeUpdate(
				//		"drop table " + getTempTableName());
				deleteStmt.executeUpdate(
						"drop table " + getTempTableName());
			
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				//conn.createStatement().executeUpdate(
				//		"delete " + getTempTableName());
				deleteStmt.executeUpdate(
						"delete " + getTempTableName());
			}
		} catch (SQLException sqle) {
			//throw sqle;
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

	private String getTempTableName() {
		String tempTableName = "";
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE)) {
			tempTableName = this.getDbSchemaName()
					+ FactRelatedQueryHandler.TEMP_PARAM_TABLE;
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.POSTGRESQL)) {
			tempTableName = SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE.substring(1);
			
		} else {
			tempTableName = this.getDbSchemaName()
					+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE;
		}
		return tempTableName;
	}

}
