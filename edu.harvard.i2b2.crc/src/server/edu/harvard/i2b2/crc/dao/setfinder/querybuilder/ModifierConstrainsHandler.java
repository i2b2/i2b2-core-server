/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.crc.datavo.ontology.ModifierType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainValueType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;
import edu.harvard.i2b2.crc.util.StringUtil;

/**
 * Class to handle modifier constrains. Generates sql where clause based on the
 * list of modifier constrains.
 * 
 * @author rkuttan
 */
public class ModifierConstrainsHandler {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());
	String schemaName = null;

	public ModifierConstrainsHandler(String schemaName) { 
		this.schemaName = schemaName;
	}
	
	public String constructModifierConstainClause(
			ModifierType modifierType)
			throws I2B2DAOException {
		
		String dimPath = "", dimColumnName = "", dimCode = "", dimOperator = "", dimTableName = "", factTableColumn = "";

		String constrainSql = null;
		if (modifierType != null) {
			dimPath = modifierType.getFullname();
			dimColumnName = modifierType.getColumnname();
			dimCode = modifierType.getDimcode();
			dimOperator = modifierType.getOperator();
			dimTableName = modifierType.getTablename();
			factTableColumn = modifierType.getFacttablecolumn();

			if (dimPath == null || dimColumnName == null || dimCode == null
					|| dimOperator == null || dimTableName == null
					|| factTableColumn == null) {
				throw new I2B2DAOException(
						"Error modifier constrain information has null value path ["
								+ dimPath + "] column name [" + dimColumnName
								+ "] dim code [" + dimCode
								+ "] dim operator [ " + dimOperator
								+ "] dim table name [" + dimTableName
								+ "] fact table column [" + factTableColumn
								+ "]");
			}
		}

		//dimPath.replaceAll("'", "''");
		//dimCode = dimCode.replaceAll("'", "''");
		
		constrainSql = "  (" + factTableColumn + " IN ( select "
				+ factTableColumn + " from " + this.schemaName + dimTableName + " where "
				+ dimColumnName + " " + dimOperator + " " + dimCode ;
		

		if ((dimOperator.toUpperCase() != null) && (dimOperator.toUpperCase().equals("LIKE")))
		{
			
			constrainSql += " {ESCAPE '?'} ";
		}
		constrainSql += "))";


		

		return constrainSql;
	}
	
	
}
