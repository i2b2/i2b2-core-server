/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 *     Christopher Herrick
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainValueType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.util.ContainsUtil;
import edu.harvard.i2b2.crc.util.RegExUtil;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;

/**
 * Class to handle value constrains. Generates sql where clause based on the
 * list of value constrains.
 * 
 * @author rkuttan
 */
public class ValueConstrainsHandler {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	private boolean unitCdConverstionFlag = false;
	private String unitCdInClause = "", unitCdSwitchClause = "";

	// ch@0013012 - The decision was made that modifier_cd needs to have a
	// default value when querying for concept value (numeric, text, large text) - 
	// if a default value is not specified for modifier_cd, a concept value 
	// constraint could bring back rows that match the values on modifier and not 
	// on the main concept value - this would be an error. To get around this, 
	// we need to include a default modifier_cd constraint on all non-modifier
	// value constraints. After careful consideration, it was determined that
	// the default value for modifier_cd should be '@'. This could present 
	// problems at sites where no non-modifier row was created for a concept 
	// or where an alternative modifier_cd was specified as the default. To make 
	// customization easier in these cases, the modifier constraint is
	// defined here and included in the appropriate sql statements below. Additionally,
	// I've maintained the old constructValueConstainClause so 1.6 queries work the same
	private String defaultModifierConstraint = " modifier_cd = '@' ";
	

	public void setUnitCdConversionFlag(boolean unitCdConverstionFlag,
			String unitCdInClause, String unitCdSwitchClause) {
		this.unitCdConverstionFlag = unitCdConverstionFlag;
		this.unitCdInClause = unitCdInClause;
		this.unitCdSwitchClause = unitCdSwitchClause;
	}

	public String[] constructValueConstainClause(
			List<ItemType.ConstrainByValue> valueConstrainList,
			String dbServerType, String dbSchemaName, int panelAccuracyScale) throws I2B2DAOException{
		//used for backward compatibility
		defaultModifierConstraint = "";
		return constructValueConstainClause(valueConstrainList, dbServerType, dbSchemaName, panelAccuracyScale, false);
	}

	
	public String[] constructValueConstainClause(
			List<ItemType.ConstrainByValue> valueConstrainList,
			String dbServerType, String dbSchemaName, int panelAccuracyScale,
			boolean useDefaultModifier)
			throws I2B2DAOException {
		String fullConstrainSql = "", containsJoinSql = "";
		System.out.println("panel accuracy scale" + panelAccuracyScale);
		panelAccuracyScale = 0;

		boolean oracleFlag = false;
		if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
			oracleFlag = true;
		}

		int j = 0;
		for (ItemType.ConstrainByValue valueConstrain : valueConstrainList) {
			ConstrainValueType valueType = valueConstrain.getValueType();
			ConstrainOperatorType operatorType = valueConstrain
					.getValueOperator();
			String value = valueConstrain.getValueConstraint();
			String unitCd = valueConstrain.getValueUnitOfMeasure();

			String constraintSql = "";
			// check if value type is not null
			if (valueType == null || operatorType == null) {
				continue;
			}
			if (valueType.equals(ConstrainValueType.LARGETEXT)) {
				ContainsUtil containsUtil = new ContainsUtil();
				String containsSql = "";
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.CONTAINS.value())) {
					containsSql = containsUtil.formatValue(value, dbServerType);
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.CONTAINS_DATABASE.value())) {
					containsSql = containsUtil.formatValue("[" + value + "]",
							dbServerType);
				} else {
					log.debug("LARGETEXT : Invalid operator skipped ["
							+ operatorType.value() + "]");
					continue;
				}
				// panelAccuracyScale = 100 - panelAccuracyScale;
				if (useDefaultModifier 
						&& defaultModifierConstraint != null
						&& defaultModifierConstraint.trim().length() > 0)
					constraintSql += defaultModifierConstraint + " AND ";

				// constraintSql += " AND valtype_cd = 'B' AND " ;
				constraintSql += " valtype_cd = 'B' AND ";
				// constrainSql = " valtype_cd = 'B' AND " ;
				if (oracleFlag == true) {
					constraintSql += " contains(observation_blob,'"
							+ containsSql + "') ";

					if (panelAccuracyScale > 0) {
						constraintSql += " >= " + panelAccuracyScale + " ";
					} else {
						constraintSql += " > 0 ";
					}

				} else {
					if (panelAccuracyScale > 0) {
						panelAccuracyScale = panelAccuracyScale * 10;
						j++;
						containsJoinSql += " INNER JOIN freetexttable("
								+ dbSchemaName
								+ "observation_fact,observation_blob,'"
								+ containsSql + "') " + " AS ft" + j
								+ " ON text_search_index = ft" + j + ".[KEY] ";

						constraintSql += " ft" + j + ".[RANK] >= "
								+ panelAccuracyScale + " ";

					} else {
						if (dbServerType.equalsIgnoreCase(
								DAOFactoryHelper.POSTGRESQL))
							constraintSql += " observation_blob @@  to_tsquery('english', '"
									+ containsSql + "') ";						
						else
						constraintSql += " CONTAINS(observation_blob,'"
								+ containsSql + "') ";
					}
				}
				log.debug("LARGETEXT where clause " + constraintSql);

			} else if (valueType.equals(ConstrainValueType.TEXT)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}

				boolean notLikeFlag = false;

				if (useDefaultModifier 
						&& defaultModifierConstraint != null
						&& defaultModifierConstraint.trim().length() > 0)
					constraintSql += defaultModifierConstraint + " AND ";

				if (operatorType.value().startsWith(
						ConstrainOperatorType.LIKE.value())) {
					// call the utility to find the like operation
					String operatorOption = RegExUtil
							.getOperatorOption(operatorType.value());
					if (operatorOption == null) {
						operatorOption = "[begin]";
					}
					String likeValueFormat = "";
					if (operatorOption.equalsIgnoreCase("[begin]")) {
						likeValueFormat = "'" + value.replaceAll("'", "''")
								+ "%'";
					} else if (operatorOption.equalsIgnoreCase("[end]")) {
						likeValueFormat = "'%" + value.replaceAll("'", "''")
								+ "'";
					} else if (operatorOption.equalsIgnoreCase("[contains]")) {
						likeValueFormat = "'%" + value.replaceAll("'", "''")
								+ "%'";
					} else if (operatorOption.equalsIgnoreCase("[exact]")) {
						likeValueFormat = "'" + value.replaceAll("'", "''")
								+ "'";
						if (oracleFlag) {
							constraintSql += " valtype_cd = 'T' AND upper(tval_char) = "
									+ " upper(" + likeValueFormat + ")";
						} else {
							constraintSql += " valtype_cd = 'T' AND tval_char = "
									+ likeValueFormat;
						}
						notLikeFlag = true;
					}
					if (notLikeFlag == false) {
						if (oracleFlag) {
							constraintSql += " valtype_cd = 'T' AND upper(tval_char) LIKE "
									+ " upper(" + likeValueFormat + ")";
						} else {
							constraintSql += " valtype_cd = 'T' AND tval_char LIKE "
									+ likeValueFormat;
						}
					}
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constraintSql += " valtype_cd = 'T' AND tval_char   = '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.IN.value())) {
					value = SqlClauseUtil.buildINClause(value, true);
					constraintSql += " valtype_cd = 'T' AND tval_char   IN ("
							+ value + ")";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.BETWEEN.value())) {
					throw new I2B2DAOException(
							"Error in value constrain, BETWEEN operator not supported in TEXT value type ["
									+ value + "]");
					/*
					 * try { value = SqlClauseUtil.buildBetweenClause(value); }
					 * catch (I2B2Exception e) { throw new
					 * I2B2DAOException("Error in BETWEEN Clause" +
					 * e.getMessage() + StackTraceUtil.getStackTrace(e)); }
					 * constrainSql =
					 * " valtype_cd = 'T' AND tval_char   BETWEEN " + value;
					 */
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					String emptyStringCheck = " ";
					if (oracleFlag == false) {
						emptyStringCheck = " AND tval_char <> '' ";
					}
					constraintSql += "  valtype_cd = 'T' AND  tval_char   <> '"
							+ value.replaceAll("'", "''") + "' "
							+ emptyStringCheck;
				} else {
					throw new I2B2DAOException(
							"Error TEXT value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.NUMBER)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				value.replaceAll("'", "''");

				String nvalNum = " nval_num ", unitsCdInClause = " ";
				if (this.unitCdConverstionFlag) {
					nvalNum = unitCdSwitchClause;
					// unitsCdInClause = this.unitCdInClause + " AND ";

					// commented not needed
					// if (unitCd != null) {
					// unitCd = unitCd.replace("'", "''");
					// unitsCdInClause = " case when '" + unitCd + "' in " +
					// this.unitCdInClause + " then 1 else 0 end  =1  AND ";
					// }
					unitsCdInClause = " ";
				}

				if (useDefaultModifier 
						&& defaultModifierConstraint != null
						&& defaultModifierConstraint.trim().length() > 0)
					constraintSql += defaultModifierConstraint + " AND ";

				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.GT.value())) {
					constraintSql += unitsCdInClause
							+ " (( valtype_cd = 'N' AND  "
							+ nvalNum
							+ " > "
							+ value
							+ " AND  tval_char IN ('E','GE')) OR ( valtype_cd = 'N' AND  "
							+ nvalNum + " >= " + value
							+ " AND  tval_char = 'G' )) ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.GE.value())) {
					constraintSql += unitsCdInClause
							+ "   valtype_cd = 'N' AND  " + nvalNum + " >= "
							+ value + " AND  tval_char IN ('E','GE','G') ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LT.value())) {
					constraintSql += unitsCdInClause
							+ "  (( valtype_cd = 'N' AND  "
							+ nvalNum
							+ " < "
							+ value
							+ " AND  tval_char IN ('E','LE')) OR ( valtype_cd = 'N' AND  "
							+ nvalNum + " <= " + value
							+ " AND  tval_char = 'L' )) ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LE.value())) {
					constraintSql += unitsCdInClause
							+ "   valtype_cd = 'N' AND  " + nvalNum + " <= "
							+ value + " AND  tval_char IN ('E','LE','L') ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constraintSql += unitsCdInClause
							+ "   valtype_cd = 'N' AND  " + nvalNum + " = "
							+ value + " AND  tval_char='E' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.BETWEEN.value())) {
					try {
						value = SqlClauseUtil.buildBetweenClause(value);
					} catch (I2B2Exception e) {
						throw new I2B2DAOException("Error in BETWEEN Clause"
								+ e.getMessage()
								+ StackTraceUtil.getStackTrace(e));
					}
					constraintSql += unitsCdInClause
							+ "   valtype_cd = 'N' AND  " + nvalNum
							+ " BETWEEN  " + value + " AND  tval_char ='E' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					constraintSql += unitsCdInClause
							+ "  (( valtype_cd = 'N' AND  "
							+ nvalNum
							+ " <> "
							+ value
							+ " AND  tval_char <> 'NE') OR ( valtype_cd = 'N' AND  "
							+ nvalNum + " = " + value
							+ " AND  tval_char ='NE' )) ";
				} else {
					throw new I2B2DAOException(
							"Error NUMBER value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.FLAG)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}

				if (useDefaultModifier 
						&& defaultModifierConstraint != null
						&& defaultModifierConstraint.trim().length() > 0)
					constraintSql += defaultModifierConstraint + " AND ";

				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constraintSql += " valueflag_cd = '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					String emptyStringCheck = " ";
					if (oracleFlag == false) {
						emptyStringCheck = " AND valueflag_cd <> '' ";
					}
					constraintSql += " valueflag_cd <> '"
							+ value.replaceAll("'", "''") + "' "
							+ emptyStringCheck;
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.IN.value())) {
					value = SqlClauseUtil.buildINClause(value, true);
					constraintSql += " valueflag_cd IN (" + value + ")";
				} else {
					throw new I2B2DAOException(
							"Error FLAG value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.MODIFIER)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				if (value != null) {
					if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.EQ.value())) {
						constraintSql = "  valtype_cd = 'M' and  tval_char = '"
								+ value.replaceAll("'", "''") + "' ";
					} else if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.NE.value())) {
						constraintSql = "  valtype_cd = 'M' and  tval_char <> '"
								+ value.replaceAll("'", "''")
								+ "' and tval_char <> ''";
					} else if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.IN.value())) {
						value = SqlClauseUtil.buildINClause(value, true);
						constraintSql = "  valtype_cd = 'M' and  tval_char IN ("
								+ value + ") ";
					}

				}

			} else {
				throw new I2B2DAOException(
						"Error value constrain, invalid value type ("
								+ valueType.toString() + ")");
			}

			if (constraintSql != null && constraintSql.trim().length() > 0) {
				if (fullConstrainSql.length() > 0) {
					fullConstrainSql += " AND ";
				}

				fullConstrainSql += constraintSql;
			}
		}

		return new String[] { fullConstrainSql, containsJoinSql };
	}

}
