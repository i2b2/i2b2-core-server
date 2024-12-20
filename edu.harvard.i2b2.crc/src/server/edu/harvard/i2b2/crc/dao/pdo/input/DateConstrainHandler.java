/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.input;

import java.text.SimpleDateFormat;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.query.InclusiveType;

/**
 * Class to generate date constrains sqls clause. $Id:
 * DateConstrainHandler.java,v 1.4 2008/06/26 03:58:56 rk903 Exp $
 * 
 * @author rkuttan
 */
public class DateConstrainHandler {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd-MMM-yyyy HH:mm:ss");
	private DataSourceLookup dataSourceLookup = null;

	public DateConstrainHandler(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = dataSourceLookup;
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE)) {
			dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER) ||dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRESQL)) {
			// ISO 8601
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		}	else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SNOWFLAKE)) {
			// ISO 8601
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		}
	}

	public String constructDateConstrainClause(String fromDateField,
			String toDateField, InclusiveType fromInclusiveType,
			InclusiveType toInclusiveType, XMLGregorianCalendar fromDate,
			XMLGregorianCalendar toDate) throws I2B2Exception {
		String dateConstrainSql = null;
		String sqlOperator = null;

		if (fromDate != null) {
			String fromDateString = dateFormat.format(fromDate
					.toGregorianCalendar().getTime());

			if (fromInclusiveType != null
					&& fromInclusiveType.name().equals(InclusiveType.NO.name())) {
				sqlOperator = " > ";
			} else {
				sqlOperator = " >= ";
			}

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				dateConstrainSql = fromDateField + sqlOperator + "to_date('"
						+ fromDateString + "','DD-MON-YYYY HH24:MI:SS')";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL)) {
				// {ts '2005-06-27 00:00:00'}
				dateConstrainSql = fromDateField + sqlOperator + " '"
						+ fromDateString + "'";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.SNOWFLAKE)) {
				// {ts '2005-06-27 00:00:00'}
				dateConstrainSql = fromDateField + sqlOperator + " '"
						+ fromDateString + "'";
			}
		}

		if (toDate != null) {
			sqlOperator = null;

			String toDateString = dateFormat.format(toDate
					.toGregorianCalendar().getTime());

			if (toInclusiveType != null
					&& toInclusiveType.name().equals(InclusiveType.NO.name())) {
				sqlOperator = " < ";
			} else {
				sqlOperator = " <= ";
			}

			if (dateConstrainSql != null) {
				dateConstrainSql += " AND ";
			}

			if (dateConstrainSql == null) {
				dateConstrainSql = " ";
			}
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				dateConstrainSql += (toDateField + sqlOperator + "to_date('"
						+ toDateString + "','DD-MON-YYYY HH24:MI:SS')");
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL)) {
				dateConstrainSql += (toDateField + sqlOperator + " '"
						+ toDateString + "'");
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SNOWFLAKE)) {
				dateConstrainSql += (toDateField + sqlOperator + " '"
						+ toDateString + "'");
			}
		}

		return dateConstrainSql;
	}

}
