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
import java.sql.SQLException;
import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

/**
 * Interface for handler of InputOptionListType $Id:
 * IInputOptionListHandler.java,v 1.5 2007/08/31 14:42:36 rk903 Exp $
 * 
 * @author rkuttan
 * @see {@link InputOptionListType}
 */
public interface IInputOptionListHandler {
	/**
	 * Get min index in enumeration list
	 * 
	 * @return
	 */
	public int getMinIndex();

	/**
	 * Get max index in enumeration list
	 * 
	 * @return
	 */
	public int getMaxIndex();

	/**
	 * Function to generate where clause of sql from input option list
	 * 
	 * @return
	 */
	public String generateWhereClauseSql();

	/**
	 * Function to generate sql string to fine min index of inputist
	 * 
	 * @param panelSql
	 * @return
	 */
	public String generateMinIndexSql(String panelSql);

	/**
	 * Return true if list type is entire set
	 * 
	 * @return
	 */
	public boolean isEntireSet();

	/**
	 * Return true if list type is collection
	 * 
	 * @return
	 */
	public boolean isCollectionId();

	/**
	 * Return true if list type is enumeration
	 * 
	 * @return
	 */
	public boolean isEnumerationSet();

	/**
	 * Return collection id
	 * 
	 * @return
	 */
	public String getCollectionId();

	/**
	 * Return enumneration list
	 * 
	 * @return
	 */
	public List<String> getEnumerationList();

	/**
	 * Upload the input enum values to temp table
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public void uploadEnumerationValueToTempTable(Connection conn)
			throws SQLException;

	/**
	 * Delete temp table data
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public void deleteTempTable(Connection conn) throws SQLException;

	/**
	 * Returns input list's size. if the list is collection id, then collection
	 * set size, if the list is entire set, then total rows in dimension table
	 * if the list is enumeration, then size of enumeration set
	 * 
	 * @return
	 * @throws I2B2DAOException
	 */
	public int getInputSize() throws I2B2DAOException;

	/**
	 * Set max index value
	 * 
	 * @param maxIndex
	 */
	public void setMaxIndex(int maxIndex);

}
