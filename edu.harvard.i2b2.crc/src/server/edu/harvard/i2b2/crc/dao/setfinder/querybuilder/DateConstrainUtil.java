/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainDateTimeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainDateType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InclusiveType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType.ConstrainByDate;

public class DateConstrainUtil {

	DateConstrainHandler dateConstrainHandler = null;

	public DateConstrainUtil(DataSourceLookup dataSourceLookup) {
		dateConstrainHandler = new DateConstrainHandler(dataSourceLookup);

	}

	public String buildPanelDateSql(PanelType panelType){
		return buildPanelDateSql(panelType, "");
	}
	
	public String buildPanelDateSql(PanelType panelType, String tableAlias) {
		String panelDateConstrain = " ";
		if (panelType.getPanelDateFrom() != null
				|| panelType.getPanelDateTo() != null) {
			panelDateConstrain = generatePanelDateConstrain(
					dateConstrainHandler, panelType.getPanelDateFrom(),
					panelType.getPanelDateTo(), tableAlias);
		}
		return panelDateConstrain;
	}
	
	public String buildItemDateSql(ItemType item){
		return buildItemDateSql(item, "");
	}

	public String buildItemDateSql(ItemType item, String tableAlias) {
		if (tableAlias==null)
			tableAlias = "";
		if (tableAlias.trim().length()>0&&!tableAlias.endsWith("."))
			tableAlias = tableAlias + ".";
		String dateConstrainSql = null;
		String fullItemDateSql = "";
		List<ConstrainByDate> constrainByDateList = item.getConstrainByDate();
		boolean firstFlag = true;
		for (ConstrainByDate constrainByDate : constrainByDateList) {
			ConstrainDateType dateFrom = constrainByDate.getDateFrom();
			ConstrainDateType dateTo = constrainByDate.getDateTo();

			String dateFromColumn = null, dateToColumn = null;
			InclusiveType dateFromInclusive = null, dateToInclusive = null;
			XMLGregorianCalendar dateFromValue = null, dateToValue = null;

			if (dateFrom != null || dateTo != null) {

				if (dateFrom != null) {
					dateFromInclusive = dateFrom.getInclusive();
					dateFromValue = dateFrom.getValue();
					dateFrom.getTime();
					if (dateFrom.getTime() != null
							&& dateFrom.getTime().name() != null
							&& dateFrom.getTime().name().equalsIgnoreCase(
									ConstrainDateTimeType.END_DATE.name())) {
						dateFromColumn = tableAlias + "end_date";
					} else {
						dateFromColumn = tableAlias + "start_date";
					}

				}

				if (dateTo != null) {
					dateToInclusive = dateTo.getInclusive();
					dateToValue = dateTo.getValue();
					dateTo.getTime();
					if (dateTo.getTime() != null
							&& dateTo.getTime().name() != null
							&& dateTo.getTime().name().equalsIgnoreCase(
									ConstrainDateTimeType.END_DATE.name())) {
						dateToColumn = tableAlias + "end_date";
					} else {
						dateToColumn = tableAlias + "start_date";
					}
				}

				dateConstrainSql = dateConstrainHandler
						.constructDateConstrainClause(dateFromColumn,
								dateToColumn, dateFromInclusive,
								dateToInclusive, dateFromValue, dateToValue);
				
				if (dateConstrainSql != null) {
					if (!firstFlag) { 
						fullItemDateSql += " AND ";
					} else { 
						firstFlag = false;
					}
					
					fullItemDateSql += "  " + dateConstrainSql + "\n";
				}

			}
		}
		
		return fullItemDateSql;
	}

	private String generatePanelDateConstrain(
			DateConstrainHandler dateConstrainHandler,
			ConstrainDateType dateFrom, ConstrainDateType dateTo,
			String tableAlias) {
		if (tableAlias==null)
			tableAlias = "";
		if (tableAlias.trim().length()>0&&!tableAlias.endsWith("."))
			tableAlias = tableAlias + ".";
		
		String dateFromColumn = null, dateToColumn = null;
		InclusiveType dateFromInclusive = null, dateToInclusive = null;
		XMLGregorianCalendar dateFromValue = null, dateToValue = null;
		String dateConstrainSql = null;

		if (dateFrom != null || dateTo != null) {

			if (dateFrom != null) {
				dateFromInclusive = dateFrom.getInclusive();
				dateFromValue = dateFrom.getValue();

				dateFrom.getTime();
				if (dateFrom.getTime() != null
						&& dateFrom.getTime().name() != null
						&& dateFrom.getTime().name().equalsIgnoreCase(
								ConstrainDateTimeType.END_DATE.name())) {
					dateFromColumn = tableAlias + "end_date";
				} else {

					dateFromColumn = tableAlias + "start_date";
				}

			}

			if (dateTo != null) {
				dateToInclusive = dateTo.getInclusive();
				dateToValue = dateTo.getValue();

				dateTo.getTime();
				if (dateTo.getTime() != null
						&& dateTo.getTime().name() != null
						&& dateTo.getTime().name().equalsIgnoreCase(
								ConstrainDateTimeType.END_DATE.name())) {
					dateToColumn = tableAlias + "end_date";
				} else {

					dateToColumn = tableAlias + "start_date";
				}

			}

			dateConstrainSql = dateConstrainHandler
					.constructDateConstrainClause(dateFromColumn, dateToColumn,
							dateFromInclusive, dateToInclusive, dateFromValue,
							dateToValue);
		}
		return dateConstrainSql;
	}
}
