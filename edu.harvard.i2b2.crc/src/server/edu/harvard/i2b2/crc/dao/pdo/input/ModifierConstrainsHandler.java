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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;

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
			ItemType.ConstrainByModifier  modifierConstrain)
			throws I2B2DAOException {
		
		String dimPath = "", dimColumnName = "", dimCode = "", dimOperator = "", dimTableName = "", factTableColumn = "";

		String constrainSql = null;
		if (modifierConstrain != null) {
			dimPath = modifierConstrain.getDimDimcode();
			dimColumnName = modifierConstrain.getDimColumnname();
			dimCode = modifierConstrain.getDimDimcode();
			dimOperator = modifierConstrain.getDimOperator();
			dimTableName = modifierConstrain.getDimTablename();
			factTableColumn = modifierConstrain.getFacttablecolumn();

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

		dimPath.replaceAll("'", "''");

		constrainSql = "  (" + factTableColumn + " IN ( select "
				+ factTableColumn + " from " + this.schemaName + dimTableName + " where "
				+ dimColumnName + " " + dimOperator + " " + dimCode 
				+ " )) ";


		

		return constrainSql;
	}
	
	
}
