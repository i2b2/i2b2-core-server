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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainOperatorType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainValueType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
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
	
	public void setUnitCdConversionFlag(boolean unitCdConverstionFlag, String unitCdInClause, String unitCdSwitchClause) { 
		this.unitCdConverstionFlag = unitCdConverstionFlag;
		this.unitCdInClause = unitCdInClause;
		this.unitCdSwitchClause = unitCdSwitchClause;
	}
	
	public String[] constructValueConstainClause(
			List<ItemType.ConstrainByValue> valueConstrainList, String dbServerType,String dbSchemaName,int panelAccuracyScale)
			throws I2B2Exception {
		String fullConstrainSql = "",containsJoinSql = "";
		System.out.println("panel accuracy scale" + panelAccuracyScale );
		panelAccuracyScale = 0;
		
		boolean oracleFlag = false;
		if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) { 
			oracleFlag = true;
		}
		int j = 0 ;
		for (ItemType.ConstrainByValue valueConstrain : valueConstrainList) {
			ConstrainValueType valueType = valueConstrain.getValueType();
			ConstrainOperatorType operatorType = valueConstrain
					.getValueOperator();
			String value = valueConstrain.getValueConstraint();
			String unitCd = valueConstrain.getValueUnitOfMeasure();
			
			String constrainSql = null;
			// check if value type is not null
			if (valueType == null) {
				continue;
			}
			if (valueType.equals(ConstrainValueType.LARGETEXT)) { 
				String containsSql = "";
				ContainsUtil containsUtil = new ContainsUtil();
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.CONTAINS.value())) {
					containsSql = containsUtil.formatValue(value,dbServerType);
				} else if(operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.CONTAINS_DATABASE.value())) { 
					containsSql = containsUtil.formatValue("[" + value + "]",dbServerType);
				} else { 
					log.debug("LARGETEXT : Invalid operator skipped [" + operatorType.value() + "]" );
					continue;
				}
					//panelAccuracyScale = 100 - panelAccuracyScale;
					
					constrainSql = " valtype_cd = 'B' AND " ;
					if (oracleFlag == true) { 
						constrainSql +=  " contains(observation_blob,'" + containsSql + "') ";
						if (panelAccuracyScale>0) {
							constrainSql += " >= " + panelAccuracyScale + " ";
						} else { 
							constrainSql += " > 0 ";
						}
					} else { 
						if (panelAccuracyScale>0) {
							panelAccuracyScale = panelAccuracyScale * 10;
							j++;
							containsJoinSql += " INNER JOIN freetexttable(" + dbSchemaName + "observation_fact,observation_blob,'"+  containsSql  + "') " 
									+ " AS ft" + j + " ON text_search_index = ft" +j+ ".[KEY] ";
							
							constrainSql += " ft"+j+".[RANK] >= " + panelAccuracyScale + " ";
				
						} else { 
							constrainSql +=  " CONTAINS(observation_blob,'" + containsSql + "') ";	
						}
						
					}
					
					
				

			} else if (valueType.equals(ConstrainValueType.TEXT)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				boolean notLikeFlag = false;
				if (operatorType.value().startsWith(
						ConstrainOperatorType.LIKE.value())) {
					//call the utility to find the like operation
					String operatorOption = RegExUtil.getOperatorOption(operatorType.value());
					if (operatorOption ==null) { 
						operatorOption = "[begin]";
					}
					String likeValueFormat = "";
					if (operatorOption.equalsIgnoreCase("[begin]")) { 
						likeValueFormat = "'" + value.replaceAll("'", "''") + "%'";
					} else if (operatorOption.equalsIgnoreCase("[end]")) { 
						likeValueFormat = "'%" + value.replaceAll("'", "''") + "'";
					} else if (operatorOption.equalsIgnoreCase("[contains]")) { 
						likeValueFormat = "'%" + value.replaceAll("'", "''") + "%'";
					} else if (operatorOption.equalsIgnoreCase("[exact]")) { 
						likeValueFormat = "'" + value.replaceAll("'", "''") + "'";
						if (oracleFlag) { 
							constrainSql = " obs.valtype_cd = 'T' AND upper(obs.tval_char) = " + " upper(" + likeValueFormat + ")";
						} else { 
							constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char = " + likeValueFormat;
						}
						notLikeFlag = true;
					}
					if (notLikeFlag == false) { 
						if (oracleFlag) { 
							constrainSql = " obs.valtype_cd = 'T' AND upper(obs.tval_char) LIKE " + " upper(" + likeValueFormat + ")";
						} else { 
							constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char LIKE " + likeValueFormat;
						}
					}
					
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char   = '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.IN.value())) {
					value = SqlClauseUtil.buildINClause(value, true);
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char   IN ("
							+ value + ")";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.BETWEEN.value())) {
					throw new I2B2Exception("Error in value constrain, BETWEEN operator not supported in TEXT value type [" + value + "]");
					/*
					value = SqlClauseUtil.buildBetweenClause(value);
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char   BETWEEN "
							+ value;
					*/
					
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					String emptyStringCheck = " ";
					if (oracleFlag==false) { 
						emptyStringCheck = " AND obs.tval_char <> '' ";
					}
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char   <> '"
							+ value.replaceAll("'", "''") + "' " + emptyStringCheck;
				} else {
					throw new I2B2Exception(
							"Error TEXT value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.NUMBER)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				value.replaceAll("'", "''");
				
				String nvalNum = " nval_num ", unitsCdInClause = "";
				if (this.unitCdConverstionFlag) { 
					nvalNum = unitCdSwitchClause;
					//unitsCdInClause = this.unitCdInClause + " AND ";
					//commented not needed
//					if (unitCd != null) { 
//						unitCd = unitCd.replace("'", "''");
//						unitsCdInClause = " case when '" + unitCd + "' in " +  this.unitCdInClause + " then 1 else 0 end  =1  AND ";	
//					}
					unitsCdInClause = " ";
					
				}
				
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.GT.value())) {
					constrainSql = unitsCdInClause + "  ((obs.valtype_cd = 'N' AND "+ nvalNum + " > "
							+ value
							+ " AND obs.tval_char IN ('E','GE')) OR (obs.valtype_cd = 'N' AND "+ nvalNum +" >= "
							+ value + " AND obs.tval_char = 'G' )) ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.GE.value())) {
					constrainSql =  unitsCdInClause + "  obs.valtype_cd = 'N' AND " + nvalNum + " >= "
							+ value + " AND obs.tval_char IN ('E','GE','G') ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LT.value())) {
					constrainSql = unitsCdInClause + "  ((obs.valtype_cd = 'N' AND " + nvalNum + " < "
							+ value
							+ " AND obs.tval_char IN ('E','LE')) OR (obs.valtype_cd = 'N' AND " + nvalNum + " <= "
							+ value + " AND obs.tval_char = 'L' )) ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LE.value())) {
					constrainSql = unitsCdInClause + "  obs.valtype_cd = 'N' AND " + nvalNum + " <= "
							+ value + " AND obs.tval_char IN ('E','LE','L') ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = unitsCdInClause + "  obs.valtype_cd = 'N' AND " + nvalNum + " = "
							+ value + " AND obs.tval_char='E' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.BETWEEN.value())) {
					value = SqlClauseUtil.buildBetweenClause(value);
					constrainSql = unitsCdInClause + "  obs.valtype_cd = 'N' AND " + nvalNum + " BETWEEN  "
							+ value + " AND obs.tval_char ='E' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					constrainSql = unitsCdInClause + "  ((obs.valtype_cd = 'N' AND " + nvalNum + " <> "
							+ value
							+ " AND obs.tval_char <> 'NE') OR (obs.valtype_cd = 'N' AND " + nvalNum + " = "
							+ value + " AND obs.tval_char ='NE' )) ";
				} else {
					throw new I2B2Exception(
							"Error NUMBER value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.FLAG)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = " obs.valueflag_cd = '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					String emptyStringCheck = " ";
					if (oracleFlag==false) { 
						emptyStringCheck = " AND obs.valueflag_cd <> '' ";
					}
					constrainSql = "  obs.valueflag_cd <> '"
							+ value.replaceAll("'", "''") + "' " + emptyStringCheck;
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.IN.value())) {
					value = SqlClauseUtil.buildINClause(value, true);
					constrainSql = " obs.valueflag_cd IN (" + value +")";
				} else {
					throw new I2B2Exception(
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
						constrainSql = " obs.valtype_cd = 'M' and obs.tval_char = '"
								+ value.replaceAll("'", "''") + "' ";
					} else if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.NE.value())) {
						constrainSql = " obs.valtype_cd = 'M' and obs.tval_char <> '"
								+ value.replaceAll("'", "''") + "' AND tval_char <> ''";
					} else if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.IN.value())) {
						value = SqlClauseUtil.buildINClause(value, true);
						constrainSql = " obs.valtype_cd = 'M' and obs.tval_char IN ("
								+ value + ") ";
					}

				}

			} else {
				throw new I2B2Exception(
						"Error value constrain, invalid value type ("
								+ valueType.toString() + ")");
			}

			if (constrainSql != null) {
				if (fullConstrainSql.length() > 0) {
					fullConstrainSql += " AND ";
				}

				fullConstrainSql += constrainSql;
			}
		}

		return new String[] { fullConstrainSql, containsJoinSql};
	}

}
