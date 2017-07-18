/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.filter;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;

/**
 * Class builds "from" and "where" clause of pdo query based on given provider
 * filter $Id: DimensionFilter.java,v 1.3 2009/01/08 19:38:57 rk903 Exp $
 * 
 * @author rkuttan
 */
public class DimensionFilter {
	private ItemType item = null;
	private String dimensionColumnName = null;
	private String factTableColumn = null;
	private String schemaName = null;
	private DataSourceLookup dataSourceLookup = null;
	private boolean derivedFactTable = QueryProcessorUtil.getInstance().getDerivedFactTable();
	
	/**
	 * Parameter constructor
	 * 
	 * @param filterListType
	 */
	public DimensionFilter(ItemType item, String schemaName, DataSourceLookup dataSourceLookup) {
		this.item = item;
		this.schemaName = schemaName;
		this.dataSourceLookup = dataSourceLookup; 
	}

	/**
	 * Function generates "from" clause of PDO query, by iterating filter list
	 * 
	 * @return sql string
	 */
	public String getFromSqlString() throws I2B2Exception {
		String conceptFromString = "";

		if (item != null) {
			conceptFromString = " ( ";

			int i = 0;

			String conceptPathValue = null;
			String conceptPathFilterName = null;

			String dimCode = item.getDimDimcode();
			String dimColumnDataType = item.getDimColumndatatype();
			String dimOperator = item.getDimOperator();
			
			if (dimOperator.equalsIgnoreCase("LIKE")
					&& dimCode.trim().length() > 0) {
				if (!SqlClauseUtil.isEnclosedinSingleQuote(dimCode)) { 
					
					// check if the dim code ends with "\" other wise add it,
					// so that it matches concept_dimension's concept_path or
					// provider_dimension's provider_path
					if (dimCode.lastIndexOf('\\') == dimCode.length() - 1) {
						dimCode = dimCode + "%";
					} else {
						dimCode = dimCode + "\\%";
					}
					if (dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL))
						dimCode = dimCode.replaceAll("\\\\", "\\\\\\\\");

				}
			}
			
			if (dimOperator.equalsIgnoreCase("IN")) {
				if (dimColumnDataType.equalsIgnoreCase("T")) {
					dimCode = SqlClauseUtil.buildINClause(dimCode, true);
				} else if (dimColumnDataType.equalsIgnoreCase("N")) {
					dimCode = SqlClauseUtil.buildINClause(dimCode, false);
				}
				dimCode = "(" + dimCode + ")";
			} else {
				if (dimColumnDataType.equalsIgnoreCase("T")) {
					if (!SqlClauseUtil.isEnclosedinSingleQuote(dimCode)) {
						dimCode = JDBCUtil.escapeSingleQuote(dimCode);
						dimCode = "'" + dimCode + "'";
					}
				}
			}

			String facttablecolumn = item.getFacttablecolumn();
			if(derivedFactTable == true){
				if(item.getFacttablecolumn().contains(".")){

					int lastIndex = facttablecolumn.lastIndexOf(".");

					facttablecolumn = (facttablecolumn.substring(lastIndex+1));
				}
			}
			//OMOP:  WAS item.getFacttablecolumn();
			/*	conceptFromString += ("SELECT " + item.getFacttablecolumn()
			 					+ " FROM " + this.schemaName + item.getDimTablename()
			 					+ " WHERE " + item.getDimColumnname() + " " + dimOperator
			 					+ " " + dimCode + "\n");
			 */

			conceptFromString += ("SELECT " + facttablecolumn
					+ " FROM " + this.schemaName + item.getDimTablename()
					+ " WHERE " + item.getDimColumnname() + " " + dimOperator
					+ " " + dimCode + "\n");
			// check if it is exactly one concept, then add group by clause,
			// other wise union will take care of
			// removing duplicate concept_cd
			//OMOP WAS item.getFacttablecolumn();
			conceptFromString += " group by " + facttablecolumn;
			conceptFromString += "    ) dimension \n";
		}

		return conceptFromString;
	}
}
