package edu.harvard.i2b2.crc.util;

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;

/**
 * Class to build sql clause from the input, to catch sql injection attack.
 * 
 * 
 */
public class SqlClauseUtil {
	protected final static Log log = LogFactory.getLog(SqlClauseUtil.class);

	public final static String REGEXP_IN_CLAUSE = ",(?!(?:[^',]|[^'],[^'])+')";

	/**
	 * Rebuild the sql IN clause from the input value constrain
	 * 
	 * @param theValueCons
	 * @param encloseSingleQuote
	 * @return
	 */
	public static String buildINClause(String theValueCons,
			boolean encloseSingleQuote) {
		// add '' for each inValues
		int i = 0;
		String singleInValue = "", inConstrainValue = "", singleQuote = "";

		if (encloseSingleQuote) {
			singleQuote = "'";
		}
		theValueCons = theValueCons.trim();
		if (theValueCons.startsWith("(")) {
			theValueCons = theValueCons.substring(1, theValueCons.length() - 1);
		}

		String[] inValues = null;
		if (encloseSingleQuote) {
			inValues = theValueCons.split(REGEXP_IN_CLAUSE);
		} else {
			inValues = theValueCons.split(",");
		}
		while (i < inValues.length) {
			if (encloseSingleQuote) {
				singleInValue = inValues[i].substring(1,
						inValues[i].length() - 1);
			} else {
				singleInValue = inValues[i];
			}
			inConstrainValue += singleQuote
					+ singleInValue.replaceAll("'", "''") + singleQuote;
			if (i + 1 < inValues.length) {
				inConstrainValue += ",";
			}
			i++;
			log.debug("Rebuilding the IN Clause with regex ["
					+ REGEXP_IN_CLAUSE + "], element value [" + singleInValue
					+ "] and the built value [" + inConstrainValue + "]");
		}

		return inConstrainValue;
	}

	/**
	 * Rebuild the sql BETWEEN clause from the input value constrain
	 * 
	 * @param betweenConstraint
	 * @return
	 * @throws I2B2Exception
	 */
	public static String buildBetweenClause(String betweenConstraint)
			throws I2B2Exception {
		StringTokenizer st = new StringTokenizer(betweenConstraint);
		String firstElement = "", andElement = "", thirdElement = "";
		if (st.countTokens() == 3) {
			firstElement = st.nextToken();
			andElement = st.nextToken();
			thirdElement = st.nextToken();
			if (!andElement.equalsIgnoreCase("and")) {
				throw new I2B2Exception("Invalid between clause ["
						+ betweenConstraint + "]");
			}
		} else {
			throw new I2B2Exception("Invalid between clause ["
					+ betweenConstraint + "]");
		}
		return firstElement.replaceAll("'", "''") + " and "
				+ thirdElement.replaceAll("'", "''");
	}
	
	
	public static boolean isEnclosedinSingleQuote(String value) {
		if (value.startsWith("'") && value.endsWith("'")) {
			return true;
		} else { 
			return false;
		}
	}
	public static boolean isEnclosedinBraces(String value) {
		if (value.startsWith("(") && value.endsWith(")")) {
			return true;
		} else { 
			return false;
		}
	}
	
	public static String handleMetaDataTextValue(String operator,String value) { 
		String  formattedValue = value;
		if ((operator != null)
				&& (operator.toUpperCase().equals("LIKE"))) {
			boolean needPercentFlag = false, needSlashFlag = false;
			//if not enclosed in single quote
			if (!SqlClauseUtil.isEnclosedinSingleQuote(formattedValue)) { 
				//escape the single quote
				formattedValue = JDBCUtil.escapeSingleQuote(formattedValue);
				
				// if missing \
				if (formattedValue.lastIndexOf('%') != formattedValue.length() - 1) {
					needPercentFlag = true; 
				} 
				
				//else if missing %
				if (needPercentFlag) { 
					if (formattedValue.lastIndexOf('\\') != formattedValue.length() - 1) {
						log.debug("Adding \\ at the end of the Concept path ");
						needSlashFlag = true;
					}	
				} else { 
					if (formattedValue.lastIndexOf('\\') != formattedValue.length() - 2) {
						log.debug("Adding \\ at the end of the Concept path ");
						needSlashFlag = true;
					}
				}
				
				if (needSlashFlag) {
					if (needPercentFlag) {
						formattedValue=formattedValue+"\\%";
					} else {
						formattedValue = formattedValue + "\\";
					}
				
				} else if (needPercentFlag) { 
					formattedValue = formattedValue + "%";
				}
				formattedValue = "'" + formattedValue + "'";

			}
		} else if (operator.toUpperCase().equals("IN")) {
			formattedValue = value;
			formattedValue = SqlClauseUtil.buildINClause(formattedValue, true);
			formattedValue = "(" + formattedValue  + ")";
			
		} else { 
			boolean needSingleQuoteFlag = false;
			
			formattedValue = value;
			//escape the single quote
			formattedValue = JDBCUtil.escapeSingleQuote(formattedValue);
			
			
			// if not enclosed in '', add it
			if (!SqlClauseUtil.isEnclosedinSingleQuote(value)) { 
					needSingleQuoteFlag = true;
			}
			if (needSingleQuoteFlag) { 
				formattedValue = "'" + formattedValue + "'";
			}
		}
		return formattedValue;
	}

	public static String handleMetaDataNumericValue(String operator, String value) { 
		String formattedValue = "";
		boolean needBracesFlag = false;
		//if operator is IN, then add open and close braces if it is missing
		if (operator.toUpperCase().equals("IN")) { 
			if (!SqlClauseUtil.isEnclosedinBraces(value)) { 
				needBracesFlag = true;
			}
		}
		if (needBracesFlag) { 
			formattedValue = "(" + value + ")";
		} else { 
			formattedValue = value;
		}
		return formattedValue;
	}
	
	public static String handleMetaDataDateValue(String operator, String value) { 
		String formattedValue = "";
		boolean needBracesFlag = false;
		//if operator is IN, then add open and close braces if it is missing
		if (operator.toUpperCase().equals("IN")) { 
			if (!SqlClauseUtil.isEnclosedinBraces(value)) { 
				needBracesFlag = true;
			}
		}
		if (needBracesFlag) { 
			formattedValue = "(" + value + ")";
		} else { 
			formattedValue = value;
		}
		return formattedValue;
	}
	
	
	

}
